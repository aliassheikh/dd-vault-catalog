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
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.jersey.errors.ErrorEntityWriter;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.views.common.View;
import io.dropwizard.views.common.ViewBundle;
import nl.knaw.dans.catalog.config.DdVaultCatalogConfig;
import nl.knaw.dans.catalog.core.DefaultMediaTypeFilter;
import nl.knaw.dans.catalog.db.DatasetDao;
import nl.knaw.dans.catalog.resources.DatasetApiResource;
import nl.knaw.dans.catalog.resources.DefaultApiResource;
import nl.knaw.dans.catalog.resources.ErrorView;

import javax.ws.rs.core.MediaType;

public class DdVaultCatalogApplication extends Application<DdVaultCatalogConfig> {
    private final HibernateBundle<DdVaultCatalogConfig> hibernateBundle = new DdVaultHibernateBundle();

    public static void main(final String[] args) throws Exception {
        new DdVaultCatalogApplication().run(args);
    }

    @Override
    public String getName() {
        return "DD Vault Catalog";
    }

    @Override
    public void initialize(final Bootstrap<DdVaultCatalogConfig> bootstrap) {
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void run(final DdVaultCatalogConfig configuration, final Environment environment) {
        var datasetDao = new DatasetDao(hibernateBundle.getSessionFactory());
        environment.jersey().register(new DefaultApiResource());
        environment.jersey().register(new DatasetApiResource(datasetDao));
        environment.jersey().register(new DefaultMediaTypeFilter());
        environment.jersey().register(new ErrorEntityWriter<ErrorMessage, View>(MediaType.TEXT_HTML_TYPE, View.class) {

            @Override
            protected View getRepresentation(ErrorMessage errorMessage) {
                return new ErrorView(errorMessage);
            }
        });
    }
}

