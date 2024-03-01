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

package nl.knaw.dans.catalog;

import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.jersey.errors.ErrorEntityWriter;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.views.common.View;
import io.dropwizard.views.common.ViewBundle;
import nl.knaw.dans.catalog.core.SearchIndex;
import nl.knaw.dans.catalog.core.UseCases;
import nl.knaw.dans.catalog.core.solr.OcflObjectMetadataReader;
import nl.knaw.dans.catalog.db.OcflObjectVersionDao;
import nl.knaw.dans.catalog.db.TarDao;
import nl.knaw.dans.catalog.resources.ArchiveDetailResource;
import nl.knaw.dans.catalog.resources.DefaultApiResource;
import nl.knaw.dans.catalog.resources.ErrorView;
import nl.knaw.dans.catalog.resources.OcflObjectApiResource;
import nl.knaw.dans.catalog.resources.TarApiResource;

import javax.ws.rs.core.MediaType;

public class DdVaultCatalogApplication extends Application<DdVaultCatalogConfiguration> {
    private final HibernateBundle<DdVaultCatalogConfiguration> hibernateBundle = new DdVaultHibernateBundle();

    public static void main(final String[] args) throws Exception {
        new DdVaultCatalogApplication().run(args);
    }

    @Override
    public String getName() {
        return "DD Vault Catalog";
    }

    @Override
    public void initialize(final Bootstrap<DdVaultCatalogConfiguration> bootstrap) {
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void run(final DdVaultCatalogConfiguration configuration, final Environment environment) {
        var useCases = buildUseCases(configuration);

        environment.jersey().register(new DefaultApiResource());
        environment.jersey().register(new TarApiResource(useCases));
        environment.jersey().register(new OcflObjectApiResource(useCases));
        environment.jersey().register(new ArchiveDetailResource(useCases));
        environment.jersey().register(new ErrorEntityWriter<ErrorMessage, View>(MediaType.TEXT_HTML_TYPE, View.class) {

            @Override
            protected View getRepresentation(ErrorMessage errorMessage) {
                return new ErrorView(errorMessage);
            }
        });
    }

    private UseCases buildUseCases(DdVaultCatalogConfiguration configuration) {
        var ocflObjectMetadataReader = new OcflObjectMetadataReader();
        var ocflObjectVersionDao = new OcflObjectVersionDao(hibernateBundle.getSessionFactory());
        var tarDao = new TarDao(hibernateBundle.getSessionFactory());

        return new UnitOfWorkAwareProxyFactory(hibernateBundle)
            .create(UseCases.class,
                new Class[] {
                    OcflObjectVersionDao.class,
                    TarDao.class,
                    SearchIndex.class,
                },
                new Object[] {
                    ocflObjectVersionDao,
                    tarDao,
                    null
                }
            );
    }
}
