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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.dans.catalog.DdVaultCatalogApplication;
import nl.knaw.dans.catalog.DdVaultCatalogConfiguration;
import nl.knaw.dans.catalog.api.OcflObjectVersionDto;
import nl.knaw.dans.catalog.api.OcflObjectVersionParametersDto;
import nl.knaw.dans.catalog.api.OcflObjectVersionRefDto;
import nl.knaw.dans.catalog.api.TarDto;
import nl.knaw.dans.catalog.api.TarParameterDto;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(DropwizardExtensionsSupport.class)
class OcflObjectApiResourceIntegrationTest {

    public static final DropwizardAppExtension<DdVaultCatalogConfiguration> EXT =
        new DropwizardAppExtension<>(
            DdVaultCatalogApplication.class,
            ResourceHelpers.resourceFilePath("debug-etc/test.yml")
        );

    @Test
    public void createOcflVersion_should_return_201() throws Exception {
        var client = new JerseyClientBuilder().build();
        var metadata = getMetadata();
        var entity = new OcflObjectVersionParametersDto()
            .nbn("someNbn")
            .swordToken("sword:" + UUID.randomUUID())
            .dataSupplier("data supplier")
            .dataversePid("dataversePid")
            .dataversePidVersion("dataversePidVersion")
            .datastation("datastation")
            .deaccessioned(Boolean.FALSE)
            .exporter("exporter")
            .exporterVersion("exporterVersion")
            .otherId("otherId")
            .otherIdVersion("otherIdVersion")
            .ocflObjectPath("ocflObjectPath")
            .metadata(metadata)
            .filepidToLocalPath("filePidToLocalPath");

        var str = EXT.getObjectMapper().writeValueAsString(entity);

        var bagId = UUID.randomUUID().toString();
        var version = 1;

        try (var response = client.target(
                String.format("http://localhost:%d/ocflObject/bagId/%s/version/%s", EXT.getLocalPort(), bagId, version))
            .request()
            .put(Entity.json(str))) {

            assertEquals(201, response.getStatus());

            var dto = response.readEntity(OcflObjectVersionDto.class);

            assertEquals(version, dto.getObjectVersion());
            assertEquals("someNbn", dto.getNbn());
            assertEquals(entity.getSwordToken(), dto.getSwordToken());
            assertEquals("datastation", dto.getDatastation());
            assertEquals(Boolean.FALSE, dto.getDeaccessioned());
            assertEquals("exporter", dto.getExporter());
            assertEquals("exporterVersion", dto.getExporterVersion());
            assertEquals("data supplier", dto.getDataSupplier());
            assertEquals("dataversePid", dto.getDataversePid());
            assertEquals("dataversePidVersion", dto.getDataversePidVersion());
            assertEquals("otherId", dto.getOtherId());
            assertEquals("otherIdVersion", dto.getOtherIdVersion());
            assertEquals("ocflObjectPath", dto.getOcflObjectPath());
            assertEquals(metadata, dto.getMetadata());
            assertEquals("filePidToLocalPath", dto.getFilepidToLocalPath());
            assertEquals(bagId, dto.getBagId());
            assertNull(dto.getTarUuid());
        }
    }

    @Test
    public void createOcflVersion_should_return_201_if_version_already_exists() throws Exception {
        var client = EXT.client();
        var entity = new OcflObjectVersionParametersDto()
            .dataSupplier("test")
            .nbn("someNbn");

        var str = EXT.getObjectMapper().writeValueAsString(entity);

        var bagId = UUID.randomUUID().toString();
        var version = 1;

        var url = String.format("http://localhost:%d/ocflObject/bagId/%s/version/%s", EXT.getLocalPort(), bagId, version);

        try (var response = client.target(url)
            .request()
            .put(Entity.json(str))) {

            assertEquals(201, response.getStatus());

            try (var sameRecord = client.target(url).request().put(Entity.json(str))) {
                assertEquals(201, sameRecord.getStatus());
            }
        }
    }

    @Test
    public void getOcflVersion_should_return_existing_item_after_unassignment_from_tar() throws Exception {
        var client = EXT.client();
        var entity = new OcflObjectVersionParametersDto()
            .dataSupplier("test")
            .nbn("someNbn");

        var str = EXT.getObjectMapper().writeValueAsString(entity);

        var bagId = "urn:uuid:" + UUID.randomUUID();
        var version = 1;

        var url = String.format("http://localhost:%d/ocflObject/bagId/%s/version/%s", EXT.getLocalPort(), bagId, version);

        // creating ocfl object
        try (var response = client.target(url).request().put(Entity.json(str))) {
            assertEquals(201, response.getStatus());
        }

        var tar = new TarParameterDto()
            .tarUuid(UUID.randomUUID())
            .vaultPath("somePath")
            .archivalTimestamp(OffsetDateTime.now())
            .ocflObjectVersions(List.of(new OcflObjectVersionRefDto().objectVersion(version).bagId(bagId)));

        // creating tar
        var apiUrl = String.format("http://localhost:%d/tar/", EXT.getLocalPort());
        try (var response = client.target(apiUrl).request().post(Entity.json(tar))) {
            assertEquals(201, response.getStatus());

            var tarResponse = response.readEntity(TarDto.class);
            assertEquals(bagId, tarResponse.getOcflObjectVersions().get(0).getBagId());
        }

        // see if the ocfl object version is bound to the tar
        try (var response = client.target(url).request().get()) {
            assertEquals(200, response.getStatus());

            var ocflResponse = response.readEntity(OcflObjectVersionDto.class);
            assertEquals(tar.getTarUuid(), ocflResponse.getTarUuid());
            assertEquals(bagId, ocflResponse.getBagId());
        }

        // updating tar, but without the ocfl object
        try (var response = client.target(apiUrl + tar.getTarUuid()).request().put(Entity.json(tar.ocflObjectVersions(List.of())))) {
            assertEquals(200, response.getStatus());
            var tarResponse = response.readEntity(TarDto.class);
            assertEquals(0, tarResponse.getOcflObjectVersions().size());
        }

        // now verify the ocfl object version is still there, not bound to a TAR
        try (var response = client.target(url).request().get()) {
            assertEquals(200, response.getStatus());

            var ocflResponse = response.readEntity(OcflObjectVersionDto.class);
            assertNull(ocflResponse.getTarUuid());
            assertEquals(bagId, ocflResponse.getBagId());
        }
    }

    @Test
    public void createOcflVersion_should_record_SkeletonRecord_property() throws Exception {
        var client = new JerseyClientBuilder().build();
        var entity = new OcflObjectVersionParametersDto()
            .dataSupplier("test")
            .skeletonRecord(true)
            .nbn("someNbn");

        var str = EXT.getObjectMapper().writeValueAsString(entity);
        var bagId = UUID.randomUUID().toString();
        var version = 1;

        var url = String.format("http://localhost:%d/ocflObject/bagId/%s/version/%s", EXT.getLocalPort(), bagId, version);

        // creating ocfl object
        try (var response = client.target(url).request().put(Entity.json(str))) {
            assertEquals(201, response.getStatus());

            var responseEntity = response.readEntity(OcflObjectVersionDto.class);
            assertEquals(true, responseEntity.getSkeletonRecord());
        }

        // verify it also returns true on the next request
        try (var response = client.target(url).request().get()) {
            var responseEntity = response.readEntity(OcflObjectVersionDto.class);
            assertEquals(true, responseEntity.getSkeletonRecord());
        }
    }

    @Test
    public void createOcflVersion_should_allow_writing_to_SkeletonRecord() throws Exception {
        var client = new JerseyClientBuilder().build();
        var entity = new OcflObjectVersionParametersDto()
            .dataSupplier("test")
            .skeletonRecord(true)
            .nbn("someNbn");

        var str = EXT.getObjectMapper().writeValueAsString(entity);
        var bagId = UUID.randomUUID().toString();
        var version = 1;

        var url = String.format("http://localhost:%d/ocflObject/bagId/%s/version/%s", EXT.getLocalPort(), bagId, version);

        // creating ocfl object
        try (var response = client.target(url).request().put(Entity.json(str))) {
            assertEquals(201, response.getStatus());
        }

        entity.dataSupplier(null);
        str = EXT.getObjectMapper().writeValueAsString(entity);

        // verify it also returns true on the next request
        try (var response = client.target(url).request().put(Entity.json(str))) {
            assertEquals(201, response.getStatus());
        }
    }

    Map<String, Object> getMetadata() throws JsonProcessingException {
        var str = "{\n" +
            "  \"dcterms:modified\": \"2021-11-17\",\n" +
            "  \"dcterms:creator\": \"DANS Archaeology Data Station (dev)\",\n" +
            "  \"@type\": \"ore:ResourceMap\",\n" +
            "  \"@id\": \"https://dar.dans.knaw.nl/datasets/export?exporter=OAI_ORE&persistentId=doi:10.5072/DAR/KXTEQT\",\n" +
            "  \"ore:describes\": {\n" +
            "    \"citation:Topic Classification\": {\n" +
            "      \"topicClassification:Term\": \"Public health\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

        return new ObjectMapper().readValue(str, new TypeReference<>() {

        });
    }
}