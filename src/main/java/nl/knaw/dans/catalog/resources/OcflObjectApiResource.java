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
import nl.knaw.dans.catalog.api.OcflObjectVersionParametersDto;
import nl.knaw.dans.catalog.api.OcflObjectVersionRefDto;
import nl.knaw.dans.catalog.core.UseCases;
import nl.knaw.dans.catalog.core.exception.OcflObjectVersionAlreadyExistsException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Slf4j
public class OcflObjectApiResource implements OcflObjectApi {
    private final UseCases useCases;

    public OcflObjectApiResource(UseCases useCases) {
        this.useCases = useCases;
    }

    @Override
    public Response createOcflObjectVersion(String bagId, Integer versionNumber, OcflObjectVersionParametersDto parameters) {
        try {
            var result = useCases.createOcflObjectVersion(
                new OcflObjectVersionRefDto().bagId(bagId).objectVersion(versionNumber),
                parameters
            );

            return Response.ok(result).status(201).build();
        }
        catch (OcflObjectVersionAlreadyExistsException e) {
            log.error(e.getMessage());
            throw new WebApplicationException(e.getMessage(), Response.Status.CONFLICT);
        }
    }

    @Override
    public Response getOcflObjectByBagIdAndVersionNumber(String bagId, Integer versionNumber) {
        var result = useCases.findOcflObjectVersionByBagIdAndVersion(bagId, versionNumber)
            .orElseThrow(() -> new WebApplicationException(
                String.format("No ocfl object version found for bagId %s and version %d", bagId, versionNumber),
                Response.Status.NOT_FOUND
            ));

        return Response.ok(result).build();
    }

    @Override
    public Response getOcflObjectsByBagId(String bagId) {
        return Response.ok(useCases.findOcflObjectVersionsByBagId(bagId)).build();
    }

    @Override
    public Response getOcflObjectsBySwordToken(String swordToken) {
        return Response.ok(useCases.findOcflObjectVersionsBySwordToken(swordToken)).build();
    }
}
