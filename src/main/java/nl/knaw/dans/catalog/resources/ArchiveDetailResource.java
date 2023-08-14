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
import nl.knaw.dans.catalog.core.UseCases;
import nl.knaw.dans.catalog.core.exception.OcflObjectVersionNotFoundException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
@Path("/nbn/{id}")
@Produces(MediaType.TEXT_HTML)
public class ArchiveDetailResource {

    private final UseCases useCases;

    public ArchiveDetailResource(UseCases useCases) {
        this.useCases = useCases;
    }

    @GET
    public ArchiveDetailView get(@PathParam("id") String id) {
        log.debug("Received request for page with NBN {}", id);
        try {
            var version = useCases.findOcflObjectVersionsByNbn(id);
            return new ArchiveDetailView(version);
        }
        catch (OcflObjectVersionNotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }
}
