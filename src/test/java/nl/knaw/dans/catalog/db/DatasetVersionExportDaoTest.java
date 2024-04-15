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
package nl.knaw.dans.catalog.db;

import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.dans.catalog.core.Dataset;
import nl.knaw.dans.catalog.core.DatasetVersionExport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DatasetVersionExportDaoTest {
    private final DAOTestExtension db = DAOTestExtension.newBuilder()
        .addEntityClass(Dataset.class)
        .addEntityClass(DatasetVersionExport.class)
        .build();
    private final DatasetDao datasetDao = new DatasetDao(db.getSessionFactory());
    private final DatasetVersionExportDao dveDao = new DatasetVersionExportDao(db.getSessionFactory());

    @Test
    public void testFindByBagId() {
        var swordToken = "sword:f98c65c0-96e8-4c7e-b7a6-50f29cfa8d3f";
        var bagId = URI.create("urn:uuid:" + UUID.randomUUID());
        db.inTransaction(() -> {
            var parentDataset = new Dataset();
            parentDataset.setNbn("123");
            parentDataset.setDataversePid("dataversePid");
            parentDataset.setSwordToken(swordToken);
            parentDataset.setDataSupplier("dataSupplier");
            parentDataset.setDatastation("datastation");
            datasetDao.save(parentDataset);
            DatasetVersionExport datasetVersionExport = new DatasetVersionExport();
            datasetVersionExport.setDataset(parentDataset);
            datasetVersionExport.setBagId(bagId);
            datasetVersionExport.setCreatedTimestamp(OffsetDateTime.now());
            datasetVersionExport.setOcflObjectVersionNumber(1);
            dveDao.add(datasetVersionExport);
            dveDao.add(datasetVersionExport);
        });
        var datasetVersionExportFound = dveDao.findByBagId(bagId);
        assertThat(datasetVersionExportFound).isNotNull();
    }

}
