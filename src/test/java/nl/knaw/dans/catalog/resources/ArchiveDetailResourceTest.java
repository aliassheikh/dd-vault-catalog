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

import com.codahale.metrics.MetricRegistry;
import freemarker.template.Configuration;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import io.dropwizard.views.ViewMessageBodyWriter;
import io.dropwizard.views.freemarker.FreemarkerViewRenderer;
import nl.knaw.dans.catalog.UseCaseFixture;
import nl.knaw.dans.catalog.core.OcflObjectVersion;
import nl.knaw.dans.catalog.core.Tar;
import nl.knaw.dans.catalog.resources.ArchiveDetailResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(DropwizardExtensionsSupport.class)
class ArchiveDetailResourceTest {
    private static final ResourceExtension EXT = ResourceExtension.builder()
        .addProvider(new ViewMessageBodyWriter(new MetricRegistry(),
            List.of(new FreemarkerViewRenderer(Configuration.VERSION_2_3_31))))
        .addResource(new ArchiveDetailResource(UseCaseFixture.useCases)).build();

    @AfterEach
    void tearDown() {
        UseCaseFixture.reset();
    }

    @Test
    void getOK() {
        var tar = Tar.builder()
            .tarUuid("7905c66e-3a91-487b-baa4-afae5d123e59")
            .vaultPath("path")
            .archivalTimestamp(OffsetDateTime.now())
            .build();

        var ocflObjectVersion1 = OcflObjectVersion.builder()
            .bagId("bagid")
            .objectVersion(2)
            .otherId("OTHER ID")
            .nbn("urn:uuid:f6626f32-b026-4dcf-85da-1ec03b148dfc")
            .tar(tar)
            .build();

        var ocflObjectVersion2 = OcflObjectVersion.builder()
            .bagId("bagid")
            .objectVersion(1)
            .otherId("OTHER ID DIFFERENT")
            .nbn("urn:uuid:f6626f32-b026-4dcf-85da-1ec03b148dfc")
            .tar(tar)
            .build();

        Mockito.when(UseCaseFixture.ocflObjectVersionDao.findByNbn(Mockito.any()))
            .thenReturn(List.of(ocflObjectVersion1, ocflObjectVersion2));

        var response = EXT.target("/nbn/urn:uuid:7905c66e-3a91-487b-baa4-afae5d123e59")
            .request()
            .accept(MediaType.TEXT_HTML_TYPE)
            .get(Response.class);

        assertEquals(200, response.getStatusInfo().getStatusCode());
    }

    @Test
    void getMissingNBN() {
        Mockito.when(UseCaseFixture.ocflObjectVersionDao.findByNbn(Mockito.any()))
            .thenReturn(List.of());

        var response = EXT.target("/nbn/urn:uuid:123")
            .request()
            .accept(MediaType.TEXT_HTML_TYPE)
            .get(Response.class);

        assertEquals(404, response.getStatusInfo().getStatusCode());
    }
}
