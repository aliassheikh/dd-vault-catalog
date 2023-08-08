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

import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import nl.knaw.dans.catalog.core.solr.OcflObjectMetadataReader;
import nl.knaw.dans.catalog.core.solr.SolrIndex;
import nl.knaw.dans.catalog.core.OcflObjectVersionRepository;
import nl.knaw.dans.catalog.core.SearchIndex;
import nl.knaw.dans.catalog.core.TarRepository;
import nl.knaw.dans.catalog.core.UseCases;
import nl.knaw.dans.catalog.db.OcflObjectVersionDAO;
import nl.knaw.dans.catalog.db.TarDAO;

public class UseCasesBuilder {

    public static UseCases build(DdVaultCatalogConfiguration configuration, HibernateBundle<DdVaultCatalogConfiguration> hibernateBundle) {
        var ocflObjectMetadataReader = new OcflObjectMetadataReader();
        var searchIndex = new SolrIndex(configuration.getSolr(), ocflObjectMetadataReader);
        var ocflObjectVersionRepository = new OcflObjectVersionDAO(hibernateBundle.getSessionFactory());
        var tarRepository = new TarDAO(hibernateBundle.getSessionFactory());

        return new UnitOfWorkAwareProxyFactory(hibernateBundle)
            .create(UseCases.class,
                new Class[]{
                    OcflObjectVersionRepository.class,
                    TarRepository.class,
                    SearchIndex.class,
                },
                new Object[]{
                    ocflObjectVersionRepository,
                    tarRepository,
                    searchIndex
                }
            );
    }
}
