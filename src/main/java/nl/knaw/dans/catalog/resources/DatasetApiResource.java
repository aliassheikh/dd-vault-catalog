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
import nl.knaw.dans.catalog.core.Conversions;
import nl.knaw.dans.catalog.api.DatasetDto;
import nl.knaw.dans.catalog.api.VersionExportDto;
import nl.knaw.dans.catalog.core.Dataset;
import nl.knaw.dans.catalog.db.DatasetDao;
import org.mapstruct.factory.Mappers;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@RequiredArgsConstructor
public class DatasetApiResource implements DatasetApi {
    private static final Conversions conversions = Mappers.getMapper(Conversions.class);

    @NonNull
    private final DatasetDao datasetDao;

    @Context
    private HttpHeaders headers;

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
        datasetDao.add(conversions.convert(datasetDto));
        return Response.ok().build();
    }

    @Override
    @UnitOfWork
    public Response addVersionExport(String nbn, String bagId, VersionExportDto versionExportDto) {
        if (!bagId.equals(versionExportDto.getBagId())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("BagId in path and body do not match").build();
        }
        var datasetOptional = datasetDao.findByNbn(nbn);
        if (datasetOptional.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Dataset not found").build();
        }
        var dataset = datasetOptional.get();
        // TODO: check that createdTimestamp is newer than the latest version export
        dataset.getDatasetVersionExports().add(conversions.convert(versionExportDto));
        datasetDao.save(dataset);
        return Response.ok().build();
    }

    @Override
    @UnitOfWork
    public Response getDataset(String nbn) {
        Optional<Dataset> datasetOptional = datasetDao.findByNbn(nbn);
        if (datasetOptional.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Dataset not found").build();
        }
        Dataset dataset = datasetOptional.get();
        if (headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
            View view = new DatasetView(dataset);
            return Response.ok(view).build();
        }
        else {
            return Response.ok(dataset).build();
        }
    }

    @Override
    @UnitOfWork
    public Response getDatasetBySwordToken(String swordToken) {
        return null;
    }

    @Override
    @UnitOfWork
    public Response getVersionExport(String nbn, String bagId) {
        return null;
    }
}
