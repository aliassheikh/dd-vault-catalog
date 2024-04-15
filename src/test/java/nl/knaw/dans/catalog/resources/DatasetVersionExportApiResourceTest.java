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
package nl.knaw.dans.catalog.resources;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.dans.catalog.api.VersionExportDto;
import nl.knaw.dans.catalog.core.Dataset;
import nl.knaw.dans.catalog.core.DatasetVersionExport;
import nl.knaw.dans.catalog.db.DatasetDao;
import nl.knaw.dans.catalog.db.DatasetVersionExportDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DatasetVersionExportApiResourceTest {
    private static final DatasetVersionExportDao datasetVersionExportDao = mock(DatasetVersionExportDao.class);

    private final ResourceExtension EXT = ResourceExtension.builder()
        .addResource(new DatasetVersionExportApiResource(datasetVersionExportDao))
        .build();

    private DatasetVersionExport dve;

    @BeforeEach
    public void setUp() {
        Dataset dataset = new Dataset();
        dataset.setNbn("urn:nbn:nl:ui:13-1234-5678");
        dve = new DatasetVersionExport();
        dve.setDataset(dataset);
    }

    @AfterEach
    public void tearDown() {
        Mockito.reset(datasetVersionExportDao);
    }

    @Test
    public void getDatasetVersionExportByBagId_should_return_200() {
        var bagId = URI.create("urn:uuid:ff1fc8dd-ee15-4e1c-9f73-c1a817bdb05c");
        dve.setBagId(bagId);
        when(datasetVersionExportDao.findByBagId(bagId)).thenReturn(dve);

        VersionExportDto found = EXT.target(String.format("/datasetVersionExport/%s", bagId))
            .request()
            .get(VersionExportDto.class);

        assertThat(found).isNotNull();
        assertThat(found.getDatasetNbn()).isEqualTo("urn:nbn:nl:ui:13-1234-5678");
        assertThat(found.getBagId()).isEqualTo(bagId.toString());
    }

    @Test
    public void getDatasetVersionExportByBagId_should_return_400_if_bagId_is_not_a_valid_urn_uuid() {
        var bagId = URI.create("urn:uuid:invalid-uuid");
        dve.setBagId(bagId);
        when(datasetVersionExportDao.findByBagId(bagId)).thenReturn(dve);

        var response = EXT.target(String.format("/datasetVersionExport/%s", bagId))
            .request()
            .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class)).contains("Invalid urn:uuid");
    }
}
