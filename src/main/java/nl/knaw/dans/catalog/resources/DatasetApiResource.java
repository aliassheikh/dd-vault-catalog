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

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.views.common.View;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.catalog.Conversions;
import nl.knaw.dans.catalog.api.DatasetDto;
import nl.knaw.dans.catalog.api.VersionExportDto;
import nl.knaw.dans.catalog.core.Dataset;
import nl.knaw.dans.catalog.core.DatasetVersionExport;
import nl.knaw.dans.catalog.db.DatasetDao;
import org.apache.http.HeaderElement;
import org.apache.http.message.BasicHeaderValueParser;
import org.mapstruct.factory.Mappers;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class DatasetApiResource implements DatasetApi {
    private static final Conversions conversions = Mappers.getMapper(Conversions.class);

    @NonNull
    private final DatasetDao datasetDao;

    @Override
    @UnitOfWork
    public Response addDataset(String nbn, DatasetDto datasetDto) {
        if (!nbn.equals(datasetDto.getNbn())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("NBN in path and body do not match").build();
        }
        var dataset = datasetDao.findByNbn(nbn);
        if (dataset.isPresent()) {
            return Response.status(Response.Status.CONFLICT).entity("Dataset already exists").build();
        }
        datasetDao.save(conversions.convert(datasetDto));
        return Response.ok().build();
    }

    @Override
    @UnitOfWork
    public Response setVersionExport(String nbn, Integer ocflObjectVersion, VersionExportDto versionExportDto) {
        var datasetOptional = datasetDao.findByNbn(nbn);
        if (datasetOptional.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Dataset not found").build();
        }
        var dataset = datasetOptional.get();
        var latestDveInCatalog = dataset.getDatasetVersionExports().stream()
            .max(Comparator.comparing(DatasetVersionExport::getOcflObjectVersionNumber)).orElseThrow(() -> new IllegalStateException("No DatasetVersionExports found for dataset with NBN " + nbn));
        if (ocflObjectVersion.equals(latestDveInCatalog.getOcflObjectVersionNumber())) {
            conversions.updateVersionExportFromDto(versionExportDto, latestDveInCatalog);
            datasetDao.save(dataset);
            return Response.ok().build();
        }
        else if (ocflObjectVersion.equals(latestDveInCatalog.getOcflObjectVersionNumber() + 1)) {
            var datasetVersionExport = conversions.convert(versionExportDto);
            datasetVersionExport.setDataset(dataset); // No way to have this done automatically in an after-mapping method it seems.
            dataset.getDatasetVersionExports().add(datasetVersionExport);
            datasetDao.save(dataset);
            log.debug("Saved dataset; returning 200 OK");
            return Response.ok().build();
        }
        else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid ocflObjectVersion; it must be equal or one greater than the latest DVE stored").build();
        }
    }

    @Override
    @UnitOfWork
    public Response getDataset(String nbn, String accept) {
        var acceptedMediaTypes = Arrays.stream(BasicHeaderValueParser.parseElements(accept, null))
            .toList().stream().map(HeaderElement::getName)
            .map(MediaType::valueOf);
        Optional<Dataset> datasetOptional = datasetDao.findByNbn(nbn);
        if (datasetOptional.isEmpty()) {
            // Make sure you do not try to return a view when the client does not accept HTML, because this will cause Jackson to try to serialize the view, which is not a JavaBean.
            if (acceptedMediaTypes.anyMatch(MediaType.TEXT_HTML_TYPE::isCompatible)) {
                return Response.status(Response.Status.NOT_FOUND).entity(new FindDatasetView()).build();
            }
            else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        Dataset dataset = datasetOptional.get();
        if (acceptedMediaTypes.anyMatch(MediaType.TEXT_HTML_TYPE::isCompatible)) {
            View view = new DatasetView(dataset);
            return Response.ok(view).build();
        }
        else {
            return Response.ok(conversions.convert(dataset)).build();
        }
    }

    @Override
    @UnitOfWork
    public Response getDatasetBySwordToken(String swordToken) {
        var dataset = datasetDao.findBySwordToken(swordToken).orElseThrow(() -> new NotFoundException("Dataset not found"));
        return Response.ok(conversions.convert(dataset)).build();
    }

    @Override
    @UnitOfWork
    public Response getVersionExport(String nbn, Integer ocflObjectVersionNumber) {
        var dataset = datasetDao.findByNbn(nbn).orElseThrow(() -> new NotFoundException("Dataset not found"));
        var datasetVersionExport = dataset.getDatasetVersionExports().stream()
            .filter(dve -> dve.getOcflObjectVersionNumber().equals(ocflObjectVersionNumber))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("DatasetVersionExport not found"));
        return Response.ok(conversions.convert(datasetVersionExport)).build();
    }
}
