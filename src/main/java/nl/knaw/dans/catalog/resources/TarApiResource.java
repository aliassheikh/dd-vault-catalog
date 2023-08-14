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

import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.catalog.api.TarParameterDto;
import nl.knaw.dans.catalog.core.UseCases;
import nl.knaw.dans.catalog.core.exception.OcflObjectVersionAlreadyInTarException;
import nl.knaw.dans.catalog.core.exception.OcflObjectVersionNotFoundException;
import nl.knaw.dans.catalog.core.exception.TarAlreadyExistsException;
import nl.knaw.dans.catalog.core.exception.TarNotFoundException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Slf4j
public class TarApiResource implements TarApi {
    private final UseCases useCases;

    public TarApiResource(UseCases useCases) {
        this.useCases = useCases;
    }

    @Override
    public Response addArchive(TarParameterDto tarDto) {
        log.info("Received new TAR {}, storing in database", tarDto);

        try {
            return Response.ok(useCases.createTar(tarDto.getTarUuid().toString(), tarDto)).status(201).build();
        }
        catch (OcflObjectVersionAlreadyInTarException | TarAlreadyExistsException e) {
            log.error(e.getMessage());
            throw new WebApplicationException(e.getMessage(), Response.Status.CONFLICT);
        }
        catch (OcflObjectVersionNotFoundException e) {
            log.error(e.getMessage());
            throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
        }
    }

    @Override
    public Response getArchiveById(UUID id) {
        log.debug("Fetching TAR with id {}", id);
        var result = useCases.findTarById(id.toString())
            .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        return Response.ok(result).build();
    }

    @Override
    public Response updateArchive(UUID id, TarParameterDto tarDto) {
        log.info("Received existing TAR {}, ID is {}, storing in database", tarDto, id);

        try {
            return Response.ok(useCases.updateTar(id.toString(), tarDto)).build();
        }
        catch (OcflObjectVersionAlreadyInTarException e) {
            log.error(e.getMessage());
            throw new WebApplicationException(e.getMessage(), Response.Status.CONFLICT);
        }
        catch (TarNotFoundException | OcflObjectVersionNotFoundException e) {
            log.error(e.getMessage());
            throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
        }
    }

}
