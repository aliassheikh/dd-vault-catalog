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
import nl.knaw.dans.catalog.core.FileMeta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DatasetDaoTest {
    private final DAOTestExtension db = DAOTestExtension.newBuilder()
        .addEntityClass(Dataset.class)
        .addEntityClass(DatasetVersionExport.class)
        .addEntityClass(FileMeta.class)
        .build();
    private final DatasetDao dao = new DatasetDao(db.getSessionFactory());

    @Test
    public void testFindById() {
        var swordToken = "sword:f98c65c0-96e8-4c7e-b7a6-50f29cfa8d3f";
        db.inTransaction(() -> {
            Dataset dataset = new Dataset();
            dataset.setNbn("123");
            dataset.setDataversePid("dataversePid");
            dataset.setSwordToken(swordToken);
            dataset.setDataSupplier("dataSupplier");
            dataset.setDatastation("datastation");
            dao.save(dataset);

        });

        db.inTransaction(() -> {
            var dataset = dao.findByNbn("123");
            assertThat(dataset).isPresent();
            assertThat(dataset.get().getDataversePid()).isEqualTo("dataversePid");
            assertThat(dataset.get().getSwordToken()).isEqualTo(swordToken);
            assertThat(dataset.get().getDataSupplier()).isEqualTo("dataSupplier");
            assertThat(dataset.get().getDatastation()).isEqualTo("datastation");
        });
    }

    @Test
    public void testSave() {
        var swordToken = "sword:f98c65c0-96e8-4c7e-b7a6-50f29cfa8d3f";
        db.inTransaction(() -> {
            Dataset dataset = new Dataset();
            dataset.setNbn("123");
            dataset.setDataversePid("dataversePid");
            dataset.setSwordToken(swordToken);
            dataset.setDataSupplier("dataSupplier");
            dataset.setDatastation("datastation");
            dao.save(dataset);
            assertThat(dataset.getId()).isNotNull();
        });
    }

    @Test
    public void testUpdate() {
        var swordToken = "sword:f98c65c0-96e8-4c7e-b7a6-50f29cfa8d3f";
        db.inTransaction(() -> {
            Dataset dataset = new Dataset();
            dataset.setNbn("123");
            dataset.setDataversePid("dataversePid");
            dataset.setSwordToken(swordToken);
            dataset.setDataSupplier("dataSupplier");
            dataset.setDatastation("datastation");
            dao.save(dataset);
            assertThat(dataset.getId()).isNotNull();
            dataset.setDataSupplier("new dataSupplier");
            dao.save(dataset);
        });

        db.inTransaction(() -> {
            var dataset = dao.findByNbn("123");
            assertThat(dataset).isPresent();
            assertThat(dataset.get().getDataSupplier()).isEqualTo("new dataSupplier");
        });
    }

    @Test
    public void testSaveWithDatasetVersionExports() {
        var swordToken = "sword:f98c65c0-96e8-4c7e-b7a6-50f29cfa8d3f";
        db.inTransaction(() -> {
            Dataset dataset = new Dataset();
            dataset.setNbn("123");
            dataset.setDataversePid("dataversePid");
            dataset.setSwordToken(swordToken);
            dataset.setDataSupplier("dataSupplier");
            dataset.setDatastation("datastation");
            var bagId = URI.create("urn:uuid:" + UUID.randomUUID());

            DatasetVersionExport datasetVersionExport = new DatasetVersionExport();
            datasetVersionExport.setBagId(bagId);
            datasetVersionExport.setCreatedTimestamp(OffsetDateTime.now());
            datasetVersionExport.setOcflObjectVersionNumber(1);
            datasetVersionExport.setDataversePidVersion("dataversePidVersion");
            datasetVersionExport.setOtherId("otherId");
            dataset.addDatasetVersionExport(datasetVersionExport);
            dao.save(dataset);
            assertThat(dataset.getId()).isNotNull();
            assertThat(dataset.getDatasetVersionExports().get(0).getId()).isNotNull();
            assertThat(dataset.getDatasetVersionExports().get(0).getBagId()).isEqualTo(bagId);
        });
    }

    @Test
    public void testSaveWithFileMetas() {
        var swordToken = "sword:f98c65c0-96e8-4c7e-b7a6-50f29cfa8d3f";
        db.inTransaction(() -> {
            Dataset dataset = new Dataset();
            dataset.setNbn("123");
            dataset.setDataversePid("dataversePid");
            dataset.setSwordToken(swordToken);
            dataset.setDataSupplier("dataSupplier");
            dataset.setDatastation("datastation");
            var bagId = URI.create("urn:uuid:" + UUID.randomUUID());

            DatasetVersionExport datasetVersionExport = new DatasetVersionExport();
            datasetVersionExport.setBagId(bagId);
            datasetVersionExport.setCreatedTimestamp(OffsetDateTime.now());
            datasetVersionExport.setOcflObjectVersionNumber(1);
            datasetVersionExport.setDataversePidVersion("dataversePidVersion");
            datasetVersionExport.setOtherId("otherId");
            dataset.addDatasetVersionExport(datasetVersionExport);

            FileMeta fileMeta = new FileMeta();
            fileMeta.setFilepath("filepath");
            fileMeta.setFileUri(URI.create("http://example.com"));
            fileMeta.setByteSize(123L);
            fileMeta.setSha1sum("sha1sum");
            datasetVersionExport.addFileMeta(fileMeta);

            dao.save(dataset);
            assertThat(dataset.getId()).isNotNull();
            assertThat(dataset.getDatasetVersionExports().get(0).getId()).isNotNull();
            assertThat(dataset.getDatasetVersionExports().get(0).getBagId()).isEqualTo(bagId);
            assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas().get(0).getId()).isNotNull();
            assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas().get(0).getFilepath()).isEqualTo("filepath");
            assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas().get(0).getFileUri()).isEqualTo(URI.create("http://example.com"));
        });
    }

}
