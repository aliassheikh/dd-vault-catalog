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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.dans.catalog.api.DatasetDto;
import nl.knaw.dans.catalog.api.FileMetaDto;
import nl.knaw.dans.catalog.api.VersionExportDto;
import nl.knaw.dans.catalog.core.Dataset;
import nl.knaw.dans.catalog.core.DatasetVersionExport;
import nl.knaw.dans.catalog.core.FileMeta;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Conversion between DTOs and domain objects.
 */
@Mapper
public interface Conversions {
    FileMetaDto convert(FileMeta fileMeta);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "versionExport", ignore = true)
    FileMeta convert(FileMetaDto fileMetaDto);

    @Named("mapFileMetaDtoListToFileMetaList")
    default List<FileMeta> mapFileMetaDtoListToFileMetaList(List<FileMetaDto> fileMetaDtos) {
        List<FileMeta> fileMetas = new ArrayList<>();
        for (FileMetaDto fileMetaDto : fileMetaDtos) {
            FileMeta fileMeta = convert(fileMetaDto);
            fileMetas.add(fileMeta);
        }
        return fileMetas;
    }

    @Named("mapFileMetaListToFileMetaDtoList")
    default List<FileMetaDto> mapFileMetaListToFileMetaDtoList(List<FileMeta> fileMetas) {
        List<FileMetaDto> fileMetaDtos = new ArrayList<>();
        for (FileMeta fileMeta : fileMetas) {
            FileMetaDto fileMetaDto = convert(fileMeta);
            fileMetaDtos.add(fileMetaDto);
        }
        return fileMetaDtos;
    }

    @Mapping(target = "datasetNbn", source = "dataset.nbn")
    @Mapping(target = "removeFileMetasItem", ignore = true) // Not sure why mapstruct thinks the removeFileMetasItem method is a property
    @Mapping(target = "fileMetas", source = "datasetVersionExport.fileMetas", qualifiedByName = "mapFileMetaListToFileMetaDtoList")
    VersionExportDto convert(DatasetVersionExport datasetVersionExport);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataset", ignore = true)
    @Mapping(target = "fileMetas", source = "versionExportDto.fileMetas", qualifiedByName = "mapFileMetaDtoListToFileMetaList")
    DatasetVersionExport convert(VersionExportDto versionExportDto);

    @AfterMapping
    default void setDataset(Object source, @MappingTarget DatasetVersionExport dve) {
        for (FileMeta fileMeta : dve.getFileMetas()) {
            fileMeta.setVersionExport(dve);
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataset", ignore = true)
    void updateVersionExportFromDto(VersionExportDto versionExportDto, @MappingTarget DatasetVersionExport datasetVersionExport);

    @Named("mapVersionExportDtoListToDatasetVersionExportList")
    default List<DatasetVersionExport> mapVersionExportDtoListToDatasetVersionExportList(List<VersionExportDto> versionExportDtoList) {
        List<DatasetVersionExport> datasetVersionExports = new ArrayList<>();
        for (VersionExportDto versionExportDto : versionExportDtoList) {
            DatasetVersionExport datasetVersionExport = convert(versionExportDto);
            datasetVersionExports.add(datasetVersionExport);
        }
        return datasetVersionExports;
    }

    @Named("mapDatasetVersionExportListToVersionExportDtoList")
    default List<VersionExportDto> mapDatasetVersionExportListToVersionExportDtoList(List<DatasetVersionExport> datasetVersionExports) {
        List<VersionExportDto> versionExportDtos = new ArrayList<>();
        for (DatasetVersionExport datasetVersionExport : datasetVersionExports) {
            VersionExportDto versionExportDto = convert(datasetVersionExport);
            versionExportDtos.add(versionExportDto);
        }
        return versionExportDtos;
    }

    @AfterMapping
    default void setDataset(Object source, @MappingTarget Dataset dataset) {
        for (DatasetVersionExport datasetVersionExport : dataset.getDatasetVersionExports()) {
            datasetVersionExport.setDataset(dataset);
        }
    }

    @Mapping(target = "datasetVersionExports", source = "datasetDto.versionExports", qualifiedByName = "mapVersionExportDtoListToDatasetVersionExportList")
    @Mapping(target = "id", ignore = true)
    Dataset convert(DatasetDto datasetDto);

    @Mapping(target = "versionExports", source = "dataset.datasetVersionExports", qualifiedByName = "mapDatasetVersionExportListToVersionExportDtoList")
    @Mapping(target = "removeVersionExportsItem", ignore = true)
        // Not sure why mapstruct thinks the removeVersionExportsItem method is a property
    DatasetDto convert(Dataset dataset);

    default URI convert(String value) {
        if (value == null) {
            return null;
        }
        return URI.create(value);
    }

    default String objectToString(Object value) {
        if (value == null) {
            return null;
        }

        return value.toString();
    }

    List<VersionExportDto> convertDatasetVersionExportList(List<DatasetVersionExport> unconfirmed);

}
