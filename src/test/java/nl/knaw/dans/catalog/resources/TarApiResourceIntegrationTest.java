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

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.dans.catalog.DdVaultCatalogApplication;
import nl.knaw.dans.catalog.DdVaultCatalogConfiguration;
import nl.knaw.dans.catalog.api.OcflObjectVersionDto;
import nl.knaw.dans.catalog.api.OcflObjectVersionParametersDto;
import nl.knaw.dans.catalog.api.OcflObjectVersionRefDto;
import nl.knaw.dans.catalog.api.TarDto;
import nl.knaw.dans.catalog.api.TarParameterDto;
import nl.knaw.dans.catalog.api.TarPartParameterDto;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(DropwizardExtensionsSupport.class)
class TarApiResourceIntegrationTest {

    public static final DropwizardTestSupport<DdVaultCatalogConfiguration> SUPPORT =
        new DropwizardTestSupport<>(DdVaultCatalogApplication.class,
            ResourceHelpers.resourceFilePath("debug-etc/test.yml")
        );

    @BeforeAll
    public static void beforeClass() throws Exception {
        SUPPORT.before();
        SUPPORT.getObjectMapper().registerModule(new JavaTimeModule());
    }

    @AfterAll
    public static void afterClass() {
        SUPPORT.after();
    }

    @Test
    // TODO should we really accept empty tars?
    public void createEmptyTar_should_return_200() throws Exception {
        var client = new JerseyClientBuilder().build();
        var entity = new TarParameterDto()
            .archivalTimestamp(OffsetDateTime.now())
            .vaultPath("test")
            .tarUuid(UUID.randomUUID());

        var str = SUPPORT.getObjectMapper().writeValueAsString(entity);

        try (var response = client.target(
                String.format("http://localhost:%d/tar", SUPPORT.getLocalPort()))
            .request()
            .post(Entity.json(str))) {

            assertEquals(201, response.getStatus());
        }
    }

    @Test
    public void createTar_with_tarParts_should_return_200() throws Exception {
        var client = new JerseyClientBuilder().build();

        var part1 = new TarPartParameterDto()
            .partName("part1")
            .checksumAlgorithm("MD5")
            .checksumValue("secret");

        var part2 = new TarPartParameterDto()
            .partName("part2")
            .checksumAlgorithm("MD5")
            .checksumValue("even more secret");


        var entity = new TarParameterDto()
            .archivalTimestamp(OffsetDateTime.now())
            .vaultPath("test")
            .tarUuid(UUID.randomUUID())
            .tarParts(List.of(part1, part2));

        var tarJson = SUPPORT.getObjectMapper().writeValueAsString(entity);
        try (var response = client
            .target(String.format("http://localhost:%d/tar", SUPPORT.getLocalPort()))
            .request()
            .post(Entity.json(tarJson))) {

            assertEquals(201, response.getStatus());

            var data = response.readEntity(TarDto.class);

            assertThat(data.getTarParts())
                .extracting("tarUuid")
                .containsOnly(entity.getTarUuid());
        }
    }

    @Test
    public void createTar_with_unknown_ocflObjectVersions_should_return_404() throws Exception {
        var client = new JerseyClientBuilder().build();

        var part1 = new TarPartParameterDto()
            .partName("part1")
            .checksumAlgorithm("MD5")
            .checksumValue("secret");

        var ocfl1 = new OcflObjectVersionRefDto()
            .bagId(String.valueOf(UUID.randomUUID()))
            .objectVersion(1);

        var entity = new TarParameterDto()
            .archivalTimestamp(OffsetDateTime.now())
            .vaultPath("test")
            .tarUuid(UUID.randomUUID())
            .ocflObjectVersions(List.of(ocfl1))
            .tarParts(List.of(part1));

        var str = SUPPORT.getObjectMapper().writeValueAsString(entity);

        try (var response = client.target(
                String.format("http://localhost:%d/tar", SUPPORT.getLocalPort()))
            .request()
            .post(Entity.json(str))) {

            assertEquals(404, response.getStatus());
        }
    }

    @Test
    public void createTar_with_ocflObjectVersions_should_return_201() throws Exception {
        var client = new JerseyClientBuilder().build();

        var part1 = new TarPartParameterDto()
            .partName("part1")
            .checksumAlgorithm("MD5")
            .checksumValue("secret");

        var version = createOcflVersion(client, new OcflObjectVersionParametersDto()
            .otherId("random id"));

        var entity = new TarParameterDto()
            .archivalTimestamp(OffsetDateTime.now())
            .vaultPath("test")
            .tarUuid(UUID.randomUUID())
            .ocflObjectVersions(List.of(new OcflObjectVersionRefDto().bagId(version.getBagId()).objectVersion(version.getObjectVersion())))
            .tarParts(List.of(part1));

        var str = SUPPORT.getObjectMapper().writeValueAsString(entity);

        try (var response = client.target(
                String.format("http://localhost:%d/tar", SUPPORT.getLocalPort()))
            .request()
            .post(Entity.json(str))) {

            assertEquals(201, response.getStatus());
        }

        var response = client.target(
                String.format("http://localhost:%d/tar/%s", SUPPORT.getLocalPort(), entity.getTarUuid()))
            .request()
            .get(TarDto.class);

        var expected = new OcflObjectVersionDto()
            .bagId(version.getBagId())
            .objectVersion(version.getObjectVersion())
            .otherId("random id")
            .tarUuid(entity.getTarUuid());

        assertThat(response.getOcflObjectVersions())
            .containsOnly(expected);
    }

    OcflObjectVersionDto createOcflVersion(Client client, OcflObjectVersionParametersDto dto) throws Exception {
        var str = SUPPORT.getObjectMapper().writeValueAsString(dto);
        var bagId = UUID.randomUUID().toString();
        var version = 1;

        try (var response = client.target(
                String.format("http://localhost:%d/ocflObject/bagId/%s/version/%s", SUPPORT.getLocalPort(), bagId, version))
            .request()
            .put(Entity.json(str))) {

            return new OcflObjectVersionDto()
                .bagId(bagId)
                .objectVersion(version);
        }
    }
}
