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

import nl.knaw.dans.catalog.api.DatasetDto;
import nl.knaw.dans.catalog.api.FileMetaDto;
import nl.knaw.dans.catalog.api.VersionExportDto;
import nl.knaw.dans.catalog.core.Dataset;
import nl.knaw.dans.catalog.core.DatasetVersionExport;
import nl.knaw.dans.catalog.core.FileMeta;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ConversionsTest {
    private static final Conversions conversions = Mappers.getMapper(Conversions.class);

    @Test
    public void convert_FileMeta_to_FileMetaDto() {
        var fileMeta = new FileMeta(1L, null, "filepath", URI.create("file:///uri"), 456L, "sha1sum");
        var fileMetaDto = conversions.convert(fileMeta);
        assertThat(fileMetaDto.getFilepath()).isEqualTo("filepath");
        assertThat(fileMetaDto.getSha1sum()).isEqualTo("sha1sum");
        assertThat(fileMetaDto.getFileUri()).isEqualTo(URI.create("file:///uri"));
        assertThat(fileMetaDto.getByteSize()).isEqualTo(456L);
    }

    @Test
    public void convert_FileMetaDto_to_FileMeta() {
        var fileMetaDto = new FileMetaDto()
            .filepath("filepath")
            .sha1sum("sha1sum")
            .fileUri(URI.create("file:///uri"))
            .byteSize(456L);
        var fileMeta = conversions.convert(fileMetaDto);
        assertThat(fileMeta.getFilepath()).isEqualTo("filepath");
        assertThat(fileMeta.getSha1sum()).isEqualTo("sha1sum");
        assertThat(fileMeta.getFileUri()).isEqualTo(URI.create("file:///uri"));
        assertThat(fileMeta.getByteSize()).isEqualTo(456L);
    }

    @Test
    public void mapFileMetaDtoListToFileMetaList() {
        var fileMetaDto1 = new FileMetaDto()
            .filepath("filepath1")
            .sha1sum("sha1sum1")
            .fileUri(URI.create("file:///uri1"))
            .byteSize(456L);
        var fileMetaDto2 = new FileMetaDto()
            .filepath("filepath2")
            .sha1sum("sha1sum2")
            .fileUri(URI.create("file:///uri2"))
            .byteSize(789L);
        var fileMetaDtos = List.of(fileMetaDto1, fileMetaDto2);
        var fileMetas = conversions.mapFileMetaDtoListToFileMetaList(fileMetaDtos);

        assertThat(fileMetas).hasSize(2);
        assertThat(fileMetas.get(0).getFilepath()).isEqualTo("filepath1");
        assertThat(fileMetas.get(0).getSha1sum()).isEqualTo("sha1sum1");
        assertThat(fileMetas.get(0).getFileUri()).isEqualTo(URI.create("file:///uri1"));
        assertThat(fileMetas.get(0).getByteSize()).isEqualTo(456L);
        assertThat(fileMetas.get(1).getFilepath()).isEqualTo("filepath2");
        assertThat(fileMetas.get(1).getSha1sum()).isEqualTo("sha1sum2");
        assertThat(fileMetas.get(1).getFileUri()).isEqualTo(URI.create("file:///uri2"));
        assertThat(fileMetas.get(1).getByteSize()).isEqualTo(789L);
    }

    @Test
    public void mapFileMetaListToFileMetaDtoList() {
        var fileMeta1 = new FileMeta(1L, null, "filepath1", URI.create("file:///uri1"), 456L, "sha1sum1");
        var fileMeta2 = new FileMeta(2L, null, "filepath2", URI.create("file:///uri2"), 789L, "sha1sum2");
        var fileMetas = List.of(fileMeta1, fileMeta2);
        var fileMetaDtos = conversions.mapFileMetaListToFileMetaDtoList(fileMetas);

        assertThat(fileMetaDtos).hasSize(2);
        assertThat(fileMetaDtos.get(0).getFilepath()).isEqualTo("filepath1");
        assertThat(fileMetaDtos.get(0).getSha1sum()).isEqualTo("sha1sum1");
        assertThat(fileMetaDtos.get(0).getFileUri()).isEqualTo(URI.create("file:///uri1"));
        assertThat(fileMetaDtos.get(0).getByteSize()).isEqualTo(456L);
        assertThat(fileMetaDtos.get(1).getFilepath()).isEqualTo("filepath2");
        assertThat(fileMetaDtos.get(1).getSha1sum()).isEqualTo("sha1sum2");
        assertThat(fileMetaDtos.get(1).getFileUri()).isEqualTo(URI.create("file:///uri2"));
        assertThat(fileMetaDtos.get(1).getByteSize()).isEqualTo(789L);
    }

    @Test
    public void convert_DatasetVersionExport_to_VersionExportDto() {
        var fileMeta1 = new FileMeta(1L, null, "filepath1", URI.create("file:///uri1"), 456L, "sha1sum1");
        var fileMeta2 = new FileMeta(2L, null, "filepath2", URI.create("file:///uri2"), 789L, "sha1sum2");
        var dve = new DatasetVersionExport();
        dve.setBagId(URI.create("urn:uuid:1234"));
        dve.setOcflObjectVersionNumber(1);
        dve.setCreatedTimestamp(null);
        dve.setArchivedTimestamp(null);
        dve.setTitle("title");
        dve.setDataversePidVersion("dataversePidVersion");
        dve.setOtherId("otherId");
        dve.setOtherIdVersion("otherIdVersion");
        dve.setMetadata("metadata");
        dve.setDeaccessioned(false);
        dve.setExporter("exporter");
        dve.setExporterVersion("exporterVersion");
        dve.setSkeletonRecord(false);
        dve.getFileMetas().add(fileMeta1);
        dve.getFileMetas().add(fileMeta2);

        var dveDto = conversions.convert(dve);

        assertThat(dveDto.getBagId()).isEqualTo("urn:uuid:1234");
        assertThat(dveDto.getOcflObjectVersionNumber()).isEqualTo(1);
        assertThat(dveDto.getCreatedTimestamp()).isNull();
        assertThat(dveDto.getArchivedTimestamp()).isNull();
        assertThat(dveDto.getTitle()).isEqualTo("title");
        assertThat(dveDto.getDataversePidVersion()).isEqualTo("dataversePidVersion");
        assertThat(dveDto.getOtherId()).isEqualTo("otherId");
        assertThat(dveDto.getOtherIdVersion()).isEqualTo("otherIdVersion");
        assertThat(dveDto.getMetadata()).isEqualTo("metadata");
        assertThat(dveDto.getDeaccessioned()).isFalse();
        assertThat(dveDto.getExporter()).isEqualTo("exporter");
        assertThat(dveDto.getExporterVersion()).isEqualTo("exporterVersion");
        assertThat(dveDto.getSkeletonRecord()).isFalse();
        assertThat(dveDto.getFileMetas()).hasSize(2);
        assertThat(dveDto.getFileMetas().get(0).getFilepath()).isEqualTo("filepath1");
        assertThat(dveDto.getFileMetas().get(0).getSha1sum()).isEqualTo("sha1sum1");
        assertThat(dveDto.getFileMetas().get(0).getFileUri()).isEqualTo(URI.create("file:///uri1"));
        assertThat(dveDto.getFileMetas().get(0).getByteSize()).isEqualTo(456L);
        assertThat(dveDto.getFileMetas().get(1).getFilepath()).isEqualTo("filepath2");
        assertThat(dveDto.getFileMetas().get(1).getSha1sum()).isEqualTo("sha1sum2");
        assertThat(dveDto.getFileMetas().get(1).getFileUri()).isEqualTo(URI.create("file:///uri2"));
        assertThat(dveDto.getFileMetas().get(1).getByteSize()).isEqualTo(789L);
    }

    @Test
    public void convert_VersionExportDto_to_DatasetVersionExport() {
        var fileMetaDto1 = new FileMetaDto()
            .filepath("filepath1")
            .sha1sum("sha1sum1")
            .fileUri(URI.create("file:///uri1"))
            .byteSize(456L);
        var fileMetaDto2 = new FileMetaDto()
            .filepath("filepath2")
            .sha1sum("sha1sum2")
            .fileUri(URI.create("file:///uri2"))
            .byteSize(789L);
        var dveDto = new VersionExportDto()
            .bagId("urn:uuid:1234")
            .ocflObjectVersionNumber(1)
            .createdTimestamp(null)
            .archivedTimestamp(null)
            .title("title")
            .dataversePidVersion("dataversePidVersion")
            .otherId("otherId")
            .otherIdVersion("otherIdVersion")
            .metadata("metadata")
            .deaccessioned(false)
            .exporter("exporter")
            .exporterVersion("exporterVersion")
            .skeletonRecord(false)
            .fileMetas(List.of(fileMetaDto1, fileMetaDto2));

        var dve = conversions.convert(dveDto);

        assertThat(dve.getBagId()).isEqualTo(URI.create("urn:uuid:1234"));
        assertThat(dve.getOcflObjectVersionNumber()).isEqualTo(1);
        assertThat(dve.getCreatedTimestamp()).isNull();
        assertThat(dve.getArchivedTimestamp()).isNull();
        assertThat(dve.getTitle()).isEqualTo("title");
        assertThat(dve.getDataversePidVersion()).isEqualTo("dataversePidVersion");
        assertThat(dve.getOtherId()).isEqualTo("otherId");
        assertThat(dve.getOtherIdVersion()).isEqualTo("otherIdVersion");
        assertThat(dve.getMetadata()).isEqualTo("metadata");
        assertThat(dve.getDeaccessioned()).isFalse();
        assertThat(dve.getExporter()).isEqualTo("exporter");
        assertThat(dve.getExporterVersion()).isEqualTo("exporterVersion");
        assertThat(dve.getSkeletonRecord()).isFalse();
        assertThat(dve.getFileMetas()).hasSize(2);
        assertThat(dve.getFileMetas().get(0).getVersionExport()).isEqualTo(dve);
        assertThat(dve.getFileMetas().get(0).getFilepath()).isEqualTo("filepath1");
        assertThat(dve.getFileMetas().get(0).getSha1sum()).isEqualTo("sha1sum1");
        assertThat(dve.getFileMetas().get(0).getFileUri()).isEqualTo(URI.create("file:///uri1"));
        assertThat(dve.getFileMetas().get(0).getByteSize()).isEqualTo(456L);
        assertThat(dve.getFileMetas().get(1).getVersionExport()).isEqualTo(dve);
        assertThat(dve.getFileMetas().get(1).getFilepath()).isEqualTo("filepath2");
        assertThat(dve.getFileMetas().get(1).getSha1sum()).isEqualTo("sha1sum2");
        assertThat(dve.getFileMetas().get(1).getFileUri()).isEqualTo(URI.create("file:///uri2"));
        assertThat(dve.getFileMetas().get(1).getByteSize()).isEqualTo(789L);
    }

    @Test
    public void updateVersionExportFromDto() {
        var fileMeta1 = new FileMeta(1L, null, "filepath1", URI.create("file:///uri1"), 456L, "sha1sum1");
        var fileMeta2 = new FileMeta(2L, null, "filepath2", URI.create("file:///uri2"), 789L, "sha1sum2");
        var dve = new DatasetVersionExport();
        dve.setBagId(URI.create("urn:uuid:1234"));
        dve.setOcflObjectVersionNumber(1);
        dve.setCreatedTimestamp(null);
        dve.setArchivedTimestamp(null);
        dve.setTitle("title");
        dve.setDataversePidVersion("dataversePidVersion");
        dve.setOtherId("otherId");
        dve.setOtherIdVersion("otherIdVersion");
        dve.setMetadata("metadata");
        dve.setDeaccessioned(false);
        dve.setExporter("exporter");
        dve.setExporterVersion("exporterVersion");
        dve.setSkeletonRecord(false);
        dve.getFileMetas().add(fileMeta1);
        dve.getFileMetas().add(fileMeta2);

        var fileMetaDto1 = new FileMetaDto()
            .filepath("filepath3")
            .sha1sum("sha1sum3")
            .fileUri(URI.create("file:///uri3"))
            .byteSize(300L);
        var fileMetaDto2 = new FileMetaDto()
            .filepath("filepath4")
            .sha1sum("sha1sum4")
            .fileUri(URI.create("file:///uri4"))
            .byteSize(400L);
        var dveDto = new VersionExportDto()
            .bagId("urn:uuid:UPDATED")
            .ocflObjectVersionNumber(1)
            .createdTimestamp(null)
            .archivedTimestamp(null)
            .title("title")
            .dataversePidVersion("dataversePidVersion-UPDATED")
            .otherId("otherId-UPDATED")
            .otherIdVersion("otherIdVersion")
            .metadata("metadata-UPDATED")
            .deaccessioned(false)
            .exporter("exporter")
            .exporterVersion("exporterVersion")
            .skeletonRecord(false)
            .fileMetas(List.of(fileMetaDto1, fileMetaDto2));

        conversions.updateVersionExportFromDto(dveDto, dve);

        assertThat(dve.getBagId()).isEqualTo(URI.create("urn:uuid:UPDATED"));
        assertThat(dve.getOcflObjectVersionNumber()).isEqualTo(1);
        assertThat(dve.getCreatedTimestamp()).isNull();
        assertThat(dve.getArchivedTimestamp()).isNull();
        assertThat(dve.getTitle()).isEqualTo("title");
        assertThat(dve.getDataversePidVersion()).isEqualTo("dataversePidVersion-UPDATED");
        assertThat(dve.getOtherId()).isEqualTo("otherId-UPDATED");
        assertThat(dve.getOtherIdVersion()).isEqualTo("otherIdVersion");
        assertThat(dve.getMetadata()).isEqualTo("metadata-UPDATED");
        assertThat(dve.getDeaccessioned()).isFalse();
        assertThat(dve.getExporter()).isEqualTo("exporter");
        assertThat(dve.getExporterVersion()).isEqualTo("exporterVersion");
        assertThat(dve.getSkeletonRecord()).isFalse();
        assertThat(dve.getFileMetas()).hasSize(2);
        assertThat(dve.getFileMetas().get(0).getVersionExport()).isEqualTo(dve);
        assertThat(dve.getFileMetas().get(0).getFilepath()).isEqualTo("filepath3");
        assertThat(dve.getFileMetas().get(0).getSha1sum()).isEqualTo("sha1sum3");
        assertThat(dve.getFileMetas().get(0).getFileUri()).isEqualTo(URI.create("file:///uri3"));
        assertThat(dve.getFileMetas().get(0).getByteSize()).isEqualTo(300L);
        assertThat(dve.getFileMetas().get(1).getVersionExport()).isEqualTo(dve);
        assertThat(dve.getFileMetas().get(1).getFilepath()).isEqualTo("filepath4");
        assertThat(dve.getFileMetas().get(1).getSha1sum()).isEqualTo("sha1sum4");
        assertThat(dve.getFileMetas().get(1).getFileUri()).isEqualTo(URI.create("file:///uri4"));
        assertThat(dve.getFileMetas().get(1).getByteSize()).isEqualTo(400L);
    }

    @Test
    public void mapVersionExportDtoListToDatasetVersionExportList() {
        var fileMetaDto1 = new FileMetaDto()
            .filepath("filepath1")
            .sha1sum("sha1sum1")
            .fileUri(URI.create("file:///uri1"))
            .byteSize(456L);
        var fileMetaDto2 = new FileMetaDto()
            .filepath("filepath2")
            .sha1sum("sha1sum2")
            .fileUri(URI.create("file:///uri2"))
            .byteSize(789L);
        var fileMetaDto3 = new FileMetaDto()
            .filepath("filepath3")
            .sha1sum("sha1sum3")
            .fileUri(URI.create("file:///uri3"))
            .byteSize(300L);
        var fileMetaDto4 = new FileMetaDto()
            .filepath("filepath4")
            .sha1sum("sha1sum4")
            .fileUri(URI.create("file:///uri4"))
            .byteSize(400L);

        var dveDto1 = new VersionExportDto()
            .bagId("urn:uuid:1234")
            .ocflObjectVersionNumber(1)
            .createdTimestamp(null)
            .archivedTimestamp(null)
            .title("title1")
            .dataversePidVersion("dataversePidVersion1")
            .otherId("otherId1")
            .otherIdVersion("otherIdVersion1")
            .metadata("metadata1")
            .deaccessioned(false)
            .exporter("exporter1")
            .exporterVersion("exporterVersion1")
            .skeletonRecord(false)
            .fileMetas(List.of(fileMetaDto1, fileMetaDto2));
        var dveDto2 = new VersionExportDto()
            .bagId("urn:uuid:5678")
            .ocflObjectVersionNumber(2)
            .createdTimestamp(null)
            .archivedTimestamp(null)
            .title("title2")
            .dataversePidVersion("dataversePidVersion2")
            .otherId("otherId2")
            .otherIdVersion("otherIdVersion2")
            .metadata("metadata2")
            .deaccessioned(false)
            .exporter("exporter2")
            .exporterVersion("exporterVersion2")
            .skeletonRecord(false)
            .fileMetas(List.of(fileMetaDto3, fileMetaDto4));
        var dveDtos = List.of(dveDto1, dveDto2);
        var dves = conversions.mapVersionExportDtoListToDatasetVersionExportList(dveDtos);

        assertThat(dves).hasSize(2);

        // DVE 1
        assertThat(dves.get(0).getBagId()).isEqualTo(URI.create("urn:uuid:1234"));
        assertThat(dves.get(0).getOcflObjectVersionNumber()).isEqualTo(1);
        assertThat(dves.get(0).getCreatedTimestamp()).isNull();
        assertThat(dves.get(0).getArchivedTimestamp()).isNull();
        assertThat(dves.get(0).getTitle()).isEqualTo("title1");
        assertThat(dves.get(0).getDataversePidVersion()).isEqualTo("dataversePidVersion1");
        assertThat(dves.get(0).getOtherId()).isEqualTo("otherId1");
        assertThat(dves.get(0).getOtherIdVersion()).isEqualTo("otherIdVersion1");
        assertThat(dves.get(0).getMetadata()).isEqualTo("metadata1");
        assertThat(dves.get(0).getDeaccessioned()).isFalse();
        assertThat(dves.get(0).getExporter()).isEqualTo("exporter1");
        assertThat(dves.get(0).getExporterVersion()).isEqualTo("exporterVersion1");
        assertThat(dves.get(0).getSkeletonRecord()).isFalse();
        assertThat(dves.get(0).getFileMetas()).hasSize(2);

        // DVE 1, FileMeta 1
        assertThat(dves.get(0).getFileMetas().get(0).getVersionExport()).isEqualTo(dves.get(0));
        assertThat(dves.get(0).getFileMetas().get(0).getFilepath()).isEqualTo("filepath1");
        assertThat(dves.get(0).getFileMetas().get(0).getSha1sum()).isEqualTo("sha1sum1");
        assertThat(dves.get(0).getFileMetas().get(0).getFileUri()).isEqualTo(URI.create("file:///uri1"));
        assertThat(dves.get(0).getFileMetas().get(0).getByteSize()).isEqualTo(456L);

        // DVE 1, FileMeta 2
        assertThat(dves.get(0).getFileMetas().get(1).getVersionExport()).isEqualTo(dves.get(0));
        assertThat(dves.get(0).getFileMetas().get(1).getFilepath()).isEqualTo("filepath2");
        assertThat(dves.get(0).getFileMetas().get(1).getSha1sum()).isEqualTo("sha1sum2");
        assertThat(dves.get(0).getFileMetas().get(1).getFileUri()).isEqualTo(URI.create("file:///uri2"));
        assertThat(dves.get(0).getFileMetas().get(1).getByteSize()).isEqualTo(789L);

        // DVE 2
        assertThat(dves.get(1).getBagId()).isEqualTo(URI.create("urn:uuid:5678"));
        assertThat(dves.get(1).getOcflObjectVersionNumber()).isEqualTo(2);
        assertThat(dves.get(1).getCreatedTimestamp()).isNull();
        assertThat(dves.get(1).getArchivedTimestamp()).isNull();
        assertThat(dves.get(1).getTitle()).isEqualTo("title2");
        assertThat(dves.get(1).getDataversePidVersion()).isEqualTo("dataversePidVersion2");
        assertThat(dves.get(1).getOtherId()).isEqualTo("otherId2");
        assertThat(dves.get(1).getOtherIdVersion()).isEqualTo("otherIdVersion2");
        assertThat(dves.get(1).getMetadata()).isEqualTo("metadata2");
        assertThat(dves.get(1).getDeaccessioned()).isFalse();
        assertThat(dves.get(1).getExporter()).isEqualTo("exporter2");
        assertThat(dves.get(1).getExporterVersion()).isEqualTo("exporterVersion2");
        assertThat(dves.get(1).getSkeletonRecord()).isFalse();
        assertThat(dves.get(1).getFileMetas()).hasSize(2);

        // DVE 2, FileMeta 1
        assertThat(dves.get(1).getFileMetas().get(0).getVersionExport()).isEqualTo(dves.get(1));
        assertThat(dves.get(1).getFileMetas().get(0).getFilepath()).isEqualTo("filepath3");
        assertThat(dves.get(1).getFileMetas().get(0).getSha1sum()).isEqualTo("sha1sum3");
        assertThat(dves.get(1).getFileMetas().get(0).getFileUri()).isEqualTo(URI.create("file:///uri3"));
        assertThat(dves.get(1).getFileMetas().get(0).getByteSize()).isEqualTo(300L);

        // DVE 2, FileMeta 2
        assertThat(dves.get(1).getFileMetas().get(1).getVersionExport()).isEqualTo(dves.get(1));
        assertThat(dves.get(1).getFileMetas().get(1).getFilepath()).isEqualTo("filepath4");
        assertThat(dves.get(1).getFileMetas().get(1).getSha1sum()).isEqualTo("sha1sum4");
        assertThat(dves.get(1).getFileMetas().get(1).getFileUri()).isEqualTo(URI.create("file:///uri4"));
        assertThat(dves.get(1).getFileMetas().get(1).getByteSize()).isEqualTo(400L);
    }

    @Test
    public void mapDatasetVersionExportListToVersionExportDtoList() {
        var fileMeta1 = new FileMeta(1L, null, "filepath1", URI.create("file:///uri1"), 456L, "sha1sum1");
        var fileMeta2 = new FileMeta(2L, null, "filepath2", URI.create("file:///uri2"), 789L, "sha1sum2");
        var fileMeta3 = new FileMeta(3L, null, "filepath3", URI.create("file:///uri3"), 300L, "sha1sum3");
        var fileMeta4 = new FileMeta(4L, null, "filepath4", URI.create("file:///uri4"), 400L, "sha1sum4");

        var dve1 = new DatasetVersionExport();
        dve1.setBagId(URI.create("urn:uuid:1234"));
        dve1.setOcflObjectVersionNumber(1);
        dve1.setCreatedTimestamp(null);
        dve1.setArchivedTimestamp(null);
        dve1.setTitle("title1");
        dve1.setDataversePidVersion("dataversePidVersion1");
        dve1.setOtherId("otherId1");
        dve1.setOtherIdVersion("otherIdVersion1");
        dve1.setMetadata("metadata1");
        dve1.setDeaccessioned(false);
        dve1.setExporter("exporter1");
        dve1.setExporterVersion("exporterVersion1");
        dve1.setSkeletonRecord(false);
        dve1.getFileMetas().add(fileMeta1);
        dve1.getFileMetas().add(fileMeta2);

        var dve2 = new DatasetVersionExport();
        dve2.setBagId(URI.create("urn:uuid:5678"));
        dve2.setOcflObjectVersionNumber(2);
        dve2.setCreatedTimestamp(null);
        dve2.setArchivedTimestamp(null);
        dve2.setTitle("title2");
        dve2.setDataversePidVersion("dataversePidVersion2");
        dve2.setOtherId("otherId2");
        dve2.setOtherIdVersion("otherIdVersion2");
        dve2.setMetadata("metadata2");
        dve2.setDeaccessioned(false);
        dve2.setExporter("exporter2");
        dve2.setExporterVersion("exporterVersion2");
        dve2.setSkeletonRecord(false);
        dve2.getFileMetas().add(fileMeta3);
        dve2.getFileMetas().add(fileMeta4);

        var dves = List.of(dve1, dve2);

        var dveDto = conversions.mapDatasetVersionExportListToVersionExportDtoList(dves);

        // DVE 1
        assertThat(dveDto).hasSize(2);
        assertThat(dveDto.get(0).getBagId()).isEqualTo("urn:uuid:1234");
        assertThat(dveDto.get(0).getOcflObjectVersionNumber()).isEqualTo(1);
        assertThat(dveDto.get(0).getCreatedTimestamp()).isNull();
        assertThat(dveDto.get(0).getArchivedTimestamp()).isNull();
        assertThat(dveDto.get(0).getTitle()).isEqualTo("title1");
        assertThat(dveDto.get(0).getDataversePidVersion()).isEqualTo("dataversePidVersion1");
        assertThat(dveDto.get(0).getOtherId()).isEqualTo("otherId1");
        assertThat(dveDto.get(0).getOtherIdVersion()).isEqualTo("otherIdVersion1");
        assertThat(dveDto.get(0).getMetadata()).isEqualTo("metadata1");
        assertThat(dveDto.get(0).getDeaccessioned()).isFalse();
        assertThat(dveDto.get(0).getExporter()).isEqualTo("exporter1");
        assertThat(dveDto.get(0).getExporterVersion()).isEqualTo("exporterVersion1");
        assertThat(dveDto.get(0).getSkeletonRecord()).isFalse();
        assertThat(dveDto.get(0).getFileMetas()).hasSize(2);

        // DVE 1, FileMeta 1
        assertThat(dveDto.get(0).getFileMetas().get(0).getFilepath()).isEqualTo("filepath1");
        assertThat(dveDto.get(0).getFileMetas().get(0).getSha1sum()).isEqualTo("sha1sum1");
        assertThat(dveDto.get(0).getFileMetas().get(0).getFileUri()).isEqualTo(URI.create("file:///uri1"));
        assertThat(dveDto.get(0).getFileMetas().get(0).getByteSize()).isEqualTo(456L);

        // DVE 1, FileMeta 2
        assertThat(dveDto.get(0).getFileMetas().get(1).getFilepath()).isEqualTo("filepath2");
        assertThat(dveDto.get(0).getFileMetas().get(1).getSha1sum()).isEqualTo("sha1sum2");
        assertThat(dveDto.get(0).getFileMetas().get(1).getFileUri()).isEqualTo(URI.create("file:///uri2"));
        assertThat(dveDto.get(0).getFileMetas().get(1).getByteSize()).isEqualTo(789L);

        // DVE 2
        assertThat(dveDto.get(1).getBagId()).isEqualTo("urn:uuid:5678");
        assertThat(dveDto.get(1).getOcflObjectVersionNumber()).isEqualTo(2);
        assertThat(dveDto.get(1).getCreatedTimestamp()).isNull();
        assertThat(dveDto.get(1).getArchivedTimestamp()).isNull();
        assertThat(dveDto.get(1).getTitle()).isEqualTo("title2");
        assertThat(dveDto.get(1).getDataversePidVersion()).isEqualTo("dataversePidVersion2");
        assertThat(dveDto.get(1).getOtherId()).isEqualTo("otherId2");
        assertThat(dveDto.get(1).getOtherIdVersion()).isEqualTo("otherIdVersion2");
        assertThat(dveDto.get(1).getMetadata()).isEqualTo("metadata2");
        assertThat(dveDto.get(1).getDeaccessioned()).isFalse();
        assertThat(dveDto.get(1).getExporter()).isEqualTo("exporter2");
        assertThat(dveDto.get(1).getExporterVersion()).isEqualTo("exporterVersion2");
        assertThat(dveDto.get(1).getSkeletonRecord()).isFalse();
        assertThat(dveDto.get(1).getFileMetas()).hasSize(2);

        // DVE 2, FileMeta 1
        assertThat(dveDto.get(1).getFileMetas().get(0).getFilepath()).isEqualTo("filepath3");
        assertThat(dveDto.get(1).getFileMetas().get(0).getSha1sum()).isEqualTo("sha1sum3");
        assertThat(dveDto.get(1).getFileMetas().get(0).getFileUri()).isEqualTo(URI.create("file:///uri3"));
        assertThat(dveDto.get(1).getFileMetas().get(0).getByteSize()).isEqualTo(300L);

        // DVE 2, FileMeta 2
        assertThat(dveDto.get(1).getFileMetas().get(1).getFilepath()).isEqualTo("filepath4");
        assertThat(dveDto.get(1).getFileMetas().get(1).getSha1sum()).isEqualTo("sha1sum4");
        assertThat(dveDto.get(1).getFileMetas().get(1).getFileUri()).isEqualTo(URI.create("file:///uri4"));
        assertThat(dveDto.get(1).getFileMetas().get(1).getByteSize()).isEqualTo(400L);
    }

    @Test
    public void convert_Dataset_to_DatasetDto() {
        var fileMeta1 = new FileMeta(1L, null, "filepath1", URI.create("file:///uri1"), 456L, "sha1sum1");
        var fileMeta2 = new FileMeta(2L, null, "filepath2", URI.create("file:///uri2"), 789L, "sha1sum2");
        var fileMeta3 = new FileMeta(3L, null, "filepath3", URI.create("file:///uri3"), 300L, "sha1sum3");
        var fileMeta4 = new FileMeta(4L, null, "filepath4", URI.create("file:///uri4"), 400L, "sha1sum4");

        var dve1 = new DatasetVersionExport();
        dve1.setBagId(URI.create("urn:uuid:1234"));
        dve1.setOcflObjectVersionNumber(1);
        dve1.setCreatedTimestamp(null);
        dve1.setArchivedTimestamp(null);
        dve1.setTitle("title1");
        dve1.setDataversePidVersion("dataversePidVersion1");
        dve1.setOtherId("otherId1");
        dve1.setOtherIdVersion("otherIdVersion1");
        dve1.setMetadata("metadata1");
        dve1.setDeaccessioned(false);
        dve1.setExporter("exporter1");
        dve1.setExporterVersion("exporterVersion1");
        dve1.setSkeletonRecord(false);
        dve1.getFileMetas().add(fileMeta1);
        dve1.getFileMetas().add(fileMeta2);

        var dve2 = new DatasetVersionExport();
        dve2.setBagId(URI.create("urn:uuid:5678"));
        dve2.setOcflObjectVersionNumber(2);
        dve2.setCreatedTimestamp(null);
        dve2.setArchivedTimestamp(null);
        dve2.setTitle("title2");
        dve2.setDataversePidVersion("dataversePidVersion2");
        dve2.setOtherId("otherId2");
        dve2.setOtherIdVersion("otherIdVersion2");
        dve2.setMetadata("metadata2");
        dve2.setDeaccessioned(false);
        dve2.setExporter("exporter2");
        dve2.setExporterVersion("exporterVersion2");
        dve2.setSkeletonRecord(false);
        dve2.getFileMetas().add(fileMeta3);
        dve2.getFileMetas().add(fileMeta4);

        var dves = List.of(dve1, dve2);

        var dataset = new Dataset();
        dataset.setId(1L);
        dataset.setNbn("nbn");
        dataset.setDataSupplier("dataSupplier");
        dataset.setSwordToken("swordToken");
        dataset.setDataversePid("dataversePid");
        dves.forEach(dataset::addDatasetVersionExport);

        var datasetDto = conversions.convert(dataset);
        
        assertThat(datasetDto.getNbn()).isEqualTo("nbn");
        assertThat(datasetDto.getDataSupplier()).isEqualTo("dataSupplier");
        assertThat(datasetDto.getSwordToken()).isEqualTo("swordToken");
        assertThat(datasetDto.getDataversePid()).isEqualTo("dataversePid");
        assertThat(datasetDto.getVersionExports()).hasSize(2);
        
        // DVE 1
        assertThat(datasetDto.getVersionExports().get(0).getDatasetNbn()).isEqualTo("nbn");
        assertThat(datasetDto.getVersionExports().get(0).getBagId()).isEqualTo("urn:uuid:1234");
        assertThat(datasetDto.getVersionExports().get(0).getOcflObjectVersionNumber()).isEqualTo(1);
        assertThat(datasetDto.getVersionExports().get(0).getCreatedTimestamp()).isNull();
        assertThat(datasetDto.getVersionExports().get(0).getArchivedTimestamp()).isNull();
        assertThat(datasetDto.getVersionExports().get(0).getTitle()).isEqualTo("title1");
        assertThat(datasetDto.getVersionExports().get(0).getDataversePidVersion()).isEqualTo("dataversePidVersion1");
        assertThat(datasetDto.getVersionExports().get(0).getOtherId()).isEqualTo("otherId1");
        assertThat(datasetDto.getVersionExports().get(0).getOtherIdVersion()).isEqualTo("otherIdVersion1");
        assertThat(datasetDto.getVersionExports().get(0).getMetadata()).isEqualTo("metadata1");
        assertThat(datasetDto.getVersionExports().get(0).getDeaccessioned()).isFalse();
        assertThat(datasetDto.getVersionExports().get(0).getExporter()).isEqualTo("exporter1");
        assertThat(datasetDto.getVersionExports().get(0).getExporterVersion()).isEqualTo("exporterVersion1");
        assertThat(datasetDto.getVersionExports().get(0).getSkeletonRecord()).isFalse();
        assertThat(datasetDto.getVersionExports().get(0).getFileMetas()).hasSize(2);
        
        // DVE 1, FileMeta 1
        assertThat(datasetDto.getVersionExports().get(0).getFileMetas().get(0).getFilepath()).isEqualTo("filepath1");
        assertThat(datasetDto.getVersionExports().get(0).getFileMetas().get(0).getSha1sum()).isEqualTo("sha1sum1");
        assertThat(datasetDto.getVersionExports().get(0).getFileMetas().get(0).getFileUri()).isEqualTo(URI.create("file:///uri1"));
        assertThat(datasetDto.getVersionExports().get(0).getFileMetas().get(0).getByteSize()).isEqualTo(456L);
        
        // DVE 1, FileMeta 2
        assertThat(datasetDto.getVersionExports().get(0).getFileMetas().get(1).getFilepath()).isEqualTo("filepath2");
        assertThat(datasetDto.getVersionExports().get(0).getFileMetas().get(1).getSha1sum()).isEqualTo("sha1sum2");
        assertThat(datasetDto.getVersionExports().get(0).getFileMetas().get(1).getFileUri()).isEqualTo(URI.create("file:///uri2"));
        assertThat(datasetDto.getVersionExports().get(0).getFileMetas().get(1).getByteSize()).isEqualTo(789L);
        
        // DVE 2
        assertThat(datasetDto.getVersionExports().get(1).getDatasetNbn()).isEqualTo("nbn");
        assertThat(datasetDto.getVersionExports().get(1).getBagId()).isEqualTo("urn:uuid:5678");
        assertThat(datasetDto.getVersionExports().get(1).getOcflObjectVersionNumber()).isEqualTo(2);
        assertThat(datasetDto.getVersionExports().get(1).getCreatedTimestamp()).isNull();
        assertThat(datasetDto.getVersionExports().get(1).getArchivedTimestamp()).isNull();
        assertThat(datasetDto.getVersionExports().get(1).getTitle()).isEqualTo("title2");
        assertThat(datasetDto.getVersionExports().get(1).getDataversePidVersion()).isEqualTo("dataversePidVersion2");
        assertThat(datasetDto.getVersionExports().get(1).getOtherId()).isEqualTo("otherId2");
        assertThat(datasetDto.getVersionExports().get(1).getOtherIdVersion()).isEqualTo("otherIdVersion2");
        assertThat(datasetDto.getVersionExports().get(1).getMetadata()).isEqualTo("metadata2");
        assertThat(datasetDto.getVersionExports().get(1).getDeaccessioned()).isFalse();
        assertThat(datasetDto.getVersionExports().get(1).getExporter()).isEqualTo("exporter2");
        assertThat(datasetDto.getVersionExports().get(1).getExporterVersion()).isEqualTo("exporterVersion2");
        assertThat(datasetDto.getVersionExports().get(1).getSkeletonRecord()).isFalse();
        assertThat(datasetDto.getVersionExports().get(1).getFileMetas()).hasSize(2);
        
        // DVE 2, FileMeta 1
        assertThat(datasetDto.getVersionExports().get(1).getFileMetas().get(0).getFilepath()).isEqualTo("filepath3");
        assertThat(datasetDto.getVersionExports().get(1).getFileMetas().get(0).getSha1sum()).isEqualTo("sha1sum3");
        assertThat(datasetDto.getVersionExports().get(1).getFileMetas().get(0).getFileUri()).isEqualTo(URI.create("file:///uri3"));
        assertThat(datasetDto.getVersionExports().get(1).getFileMetas().get(0).getByteSize()).isEqualTo(300L);
        
        // DVE 2, FileMeta 2
        assertThat(datasetDto.getVersionExports().get(1).getFileMetas().get(1).getFilepath()).isEqualTo("filepath4");
        assertThat(datasetDto.getVersionExports().get(1).getFileMetas().get(1).getSha1sum()).isEqualTo("sha1sum4");
        assertThat(datasetDto.getVersionExports().get(1).getFileMetas().get(1).getFileUri()).isEqualTo(URI.create("file:///uri4"));
        assertThat(datasetDto.getVersionExports().get(1).getFileMetas().get(1).getByteSize()).isEqualTo(400L);
    }
    
    @Test
    public void convert_DatasetDto_to_Dataset() {
        var fileMetaDto1 = new FileMetaDto()
            .filepath("filepath1")
            .sha1sum("sha1sum1")
            .fileUri(URI.create("file:///uri1"))
            .byteSize(456L);
        var fileMetaDto2 = new FileMetaDto()
            .filepath("filepath2")
            .sha1sum("sha1sum2")
            .fileUri(URI.create("file:///uri2"))
            .byteSize(789L);
        var fileMetaDto3 = new FileMetaDto()
            .filepath("filepath3")
            .sha1sum("sha1sum3")
            .fileUri(URI.create("file:///uri3"))
            .byteSize(300L);
        var fileMetaDto4 = new FileMetaDto()
            .filepath("filepath4")
            .sha1sum("sha1sum4")
            .fileUri(URI.create("file:///uri4"))
            .byteSize(400L);

        var dveDto1 = new VersionExportDto()
            .bagId("urn:uuid:1234")
            .ocflObjectVersionNumber(1)
            .createdTimestamp(null)
            .archivedTimestamp(null)
            .title("title1")
            .dataversePidVersion("dataversePidVersion1")
            .otherId("otherId1")
            .otherIdVersion("otherIdVersion1")
            .metadata("metadata1")
            .deaccessioned(false)
            .exporter("exporter1")
            .exporterVersion("exporterVersion1")
            .skeletonRecord(false)
            .fileMetas(List.of(fileMetaDto1, fileMetaDto2));
        var dveDto2 = new VersionExportDto()
            .bagId("urn:uuid:5678")
            .ocflObjectVersionNumber(2)
            .createdTimestamp(null)
            .archivedTimestamp(null)
            .title("title2")
            .dataversePidVersion("dataversePidVersion2")
            .otherId("otherId2")
            .otherIdVersion("otherIdVersion2")
            .metadata("metadata2")
            .deaccessioned(false)
            .exporter("exporter2")
            .exporterVersion("exporterVersion2")
            .skeletonRecord(false)
            .fileMetas(List.of(fileMetaDto3, fileMetaDto4));
        
        var datasetDto = new DatasetDto()
            .nbn("nbn")
            .dataSupplier("dataSupplier")
            .swordToken("swordToken")
            .dataversePid("dataversePid")
            .versionExports(List.of(dveDto1, dveDto2));
        
        var dataset = conversions.convert(datasetDto);
        
        assertThat(dataset.getNbn()).isEqualTo("nbn");
        assertThat(dataset.getDataSupplier()).isEqualTo("dataSupplier");
        assertThat(dataset.getSwordToken()).isEqualTo("swordToken");
        assertThat(dataset.getDataversePid()).isEqualTo("dataversePid");
        assertThat(dataset.getDatasetVersionExports()).hasSize(2);
        
        // DVE 1
        assertThat(dataset.getDatasetVersionExports().get(0).getDataset()).isEqualTo(dataset);
        assertThat(dataset.getDatasetVersionExports().get(0).getBagId()).isEqualTo(URI.create("urn:uuid:1234"));
        assertThat(dataset.getDatasetVersionExports().get(0).getOcflObjectVersionNumber()).isEqualTo(1);
        assertThat(dataset.getDatasetVersionExports().get(0).getCreatedTimestamp()).isNull();
        assertThat(dataset.getDatasetVersionExports().get(0).getArchivedTimestamp()).isNull();
        assertThat(dataset.getDatasetVersionExports().get(0).getTitle()).isEqualTo("title1");
        assertThat(dataset.getDatasetVersionExports().get(0).getDataversePidVersion()).isEqualTo("dataversePidVersion1");
        assertThat(dataset.getDatasetVersionExports().get(0).getOtherId()).isEqualTo("otherId1");
        assertThat(dataset.getDatasetVersionExports().get(0).getOtherIdVersion()).isEqualTo("otherIdVersion1");
        assertThat(dataset.getDatasetVersionExports().get(0).getMetadata()).isEqualTo("metadata1");
        assertThat(dataset.getDatasetVersionExports().get(0).getDeaccessioned()).isFalse();
        assertThat(dataset.getDatasetVersionExports().get(0).getExporter()).isEqualTo("exporter1");
        assertThat(dataset.getDatasetVersionExports().get(0).getExporterVersion()).isEqualTo("exporterVersion1");
        assertThat(dataset.getDatasetVersionExports().get(0).getSkeletonRecord()).isFalse();
        assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas()).hasSize(2);
        
        // DVE 1, FileMeta 1
        assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas().get(0).getVersionExport()).isEqualTo(dataset.getDatasetVersionExports().get(0));
        assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas().get(0).getFilepath()).isEqualTo("filepath1");
        assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas().get(0).getSha1sum()).isEqualTo("sha1sum1");
        assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas().get(0).getFileUri()).isEqualTo(URI.create("file:///uri1"));
        assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas().get(0).getByteSize()).isEqualTo(456L);
        
        // DVE 1, FileMeta 2
        assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas().get(1).getVersionExport()).isEqualTo(dataset.getDatasetVersionExports().get(0));
        assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas().get(1).getFilepath()).isEqualTo("filepath2");
        assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas().get(1).getSha1sum()).isEqualTo("sha1sum2");
        assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas().get(1).getFileUri()).isEqualTo(URI.create("file:///uri2"));
        assertThat(dataset.getDatasetVersionExports().get(0).getFileMetas().get(1).getByteSize()).isEqualTo(789L);
        
        // DVE 2
        assertThat(dataset.getDatasetVersionExports().get(1).getDataset()).isEqualTo(dataset);
        assertThat(dataset.getDatasetVersionExports().get(1).getBagId()).isEqualTo(URI.create("urn:uuid:5678"));
        assertThat(dataset.getDatasetVersionExports().get(1).getOcflObjectVersionNumber()).isEqualTo(2);
        assertThat(dataset.getDatasetVersionExports().get(1).getCreatedTimestamp()).isNull();
        assertThat(dataset.getDatasetVersionExports().get(1).getArchivedTimestamp()).isNull();
        assertThat(dataset.getDatasetVersionExports().get(1).getTitle()).isEqualTo("title2");
        assertThat(dataset.getDatasetVersionExports().get(1).getDataversePidVersion()).isEqualTo("dataversePidVersion2");
        assertThat(dataset.getDatasetVersionExports().get(1).getOtherId()).isEqualTo("otherId2");
        assertThat(dataset.getDatasetVersionExports().get(1).getOtherIdVersion()).isEqualTo("otherIdVersion2");
        assertThat(dataset.getDatasetVersionExports().get(1).getMetadata()).isEqualTo("metadata2");
        assertThat(dataset.getDatasetVersionExports().get(1).getDeaccessioned()).isFalse();
        assertThat(dataset.getDatasetVersionExports().get(1).getExporter()).isEqualTo("exporter2");
        assertThat(dataset.getDatasetVersionExports().get(1).getExporterVersion()).isEqualTo("exporterVersion2");
        assertThat(dataset.getDatasetVersionExports().get(1).getSkeletonRecord()).isFalse();
        assertThat(dataset.getDatasetVersionExports().get(1).getFileMetas()).hasSize(2);
        
        // DVE 2, FileMeta 1
        assertThat(dataset.getDatasetVersionExports().get(1).getFileMetas().get(0).getVersionExport()).isEqualTo(dataset.getDatasetVersionExports().get(1));
        assertThat(dataset.getDatasetVersionExports().get(1).getFileMetas().get(0).getFilepath()).isEqualTo("filepath3");
        assertThat(dataset.getDatasetVersionExports().get(1).getFileMetas().get(0).getSha1sum()).isEqualTo("sha1sum3");
        assertThat(dataset.getDatasetVersionExports().get(1).getFileMetas().get(0).getFileUri()).isEqualTo(URI.create("file:///uri3"));
        assertThat(dataset.getDatasetVersionExports().get(1).getFileMetas().get(0).getByteSize()).isEqualTo(300L);
        
        // DVE 2, FileMeta 2
        assertThat(dataset.getDatasetVersionExports().get(1).getFileMetas().get(1).getVersionExport()).isEqualTo(dataset.getDatasetVersionExports().get(1));
        assertThat(dataset.getDatasetVersionExports().get(1).getFileMetas().get(1).getFilepath()).isEqualTo("filepath4");
        assertThat(dataset.getDatasetVersionExports().get(1).getFileMetas().get(1).getSha1sum()).isEqualTo("sha1sum4");
        assertThat(dataset.getDatasetVersionExports().get(1).getFileMetas().get(1).getFileUri()).isEqualTo(URI.create("file:///uri4"));
        assertThat(dataset.getDatasetVersionExports().get(1).getFileMetas().get(1).getByteSize()).isEqualTo(400L);
        
        
            
    }
}