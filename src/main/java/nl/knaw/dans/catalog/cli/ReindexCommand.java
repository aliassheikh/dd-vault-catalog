/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.catalog.cli;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.knaw.dans.catalog.DdVaultCatalogConfiguration;
import nl.knaw.dans.catalog.core.SearchIndex;
import nl.knaw.dans.catalog.core.UseCases;
import nl.knaw.dans.catalog.core.solr.OcflObjectMetadataReader;
import nl.knaw.dans.catalog.core.solr.SolrIndex;
import nl.knaw.dans.catalog.db.OcflObjectVersionDao;
import nl.knaw.dans.catalog.db.TarDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexCommand extends ConfiguredCommand<DdVaultCatalogConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(ReindexCommand.class);
    private final HibernateBundle<DdVaultCatalogConfiguration> hibernateBundle;

    public ReindexCommand(HibernateBundle<DdVaultCatalogConfiguration> hibernateBundle) {
        super("reindex", "Reindexes all documents to Solr");
        this.hibernateBundle = hibernateBundle;
    }

    @Override
    protected void run(Bootstrap<DdVaultCatalogConfiguration> bootstrap, Namespace namespace, DdVaultCatalogConfiguration configuration) throws Exception {
        configuration.getDatabase().getProperties().put("hibernate.current_session_context_class", "org.hibernate.context.internal.ThreadLocalSessionContext");

        hibernateBundle.run(configuration,
            new Environment("dd-vault-catalog-environment",
                bootstrap.getObjectMapper(),
                bootstrap.getValidatorFactory(),
                bootstrap.getMetricRegistry(),
                bootstrap.getClassLoader(),
                bootstrap.getHealthCheckRegistry(),
                configuration)
        );

        log.info("Configured Hibernate");

        var ocflObjectMetadataReader = new OcflObjectMetadataReader();
        var searchIndex = new SolrIndex(configuration.getSolr(), ocflObjectMetadataReader);
        var ocflObjectVersionDao = new OcflObjectVersionDao(hibernateBundle.getSessionFactory());
        var tarDao = new TarDao(hibernateBundle.getSessionFactory());

        var useCases = new UnitOfWorkAwareProxyFactory(hibernateBundle)
            .create(UseCases.class,
                new Class[] {
                    OcflObjectVersionDao.class,
                    TarDao.class,
                    SearchIndex.class,
                },
                new Object[] {
                    ocflObjectVersionDao,
                    tarDao,
                    searchIndex
                }
            );

        log.info("Configured services");

        try (var session = hibernateBundle.getSessionFactory().getCurrentSession()) {
            var transaction = session.beginTransaction();
            useCases.reindexAllTars();
            transaction.rollback();
        }
    }
}
