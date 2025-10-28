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
import lombok.RequiredArgsConstructor;
import nl.knaw.dans.catalog.api.UnconfirmedDatasetVersionExportDto;
import nl.knaw.dans.catalog.db.DatasetVersionExportDao;

import javax.ws.rs.core.Response;
import java.util.ArrayList;

@RequiredArgsConstructor
public class UnconfirmedDatasetVersionExportsApiResource implements UnconfirmedDatasetVersionExportsApi {
    private final DatasetVersionExportDao dao;

    @Override
    @UnitOfWork
    public Response getUnconfirmedDatasetVersionExports(Integer limit, Integer offset) {
        var dves = dao.findUnconfirmed(limit, offset);
        var unconfirmedDtos = new ArrayList<UnconfirmedDatasetVersionExportDto>();
        for (var dve : dves) {
            unconfirmedDtos.add(new UnconfirmedDatasetVersionExportDto()
                .datasetNbn(dve.getDataset().getNbn())
                .storageRoot(dve.getDataset().getOcflStorageRoot())
                .ocflObjectVersionNumber(dve.getOcflObjectVersionNumber()));
        }
        return Response.ok(unconfirmedDtos).build();
    }
}