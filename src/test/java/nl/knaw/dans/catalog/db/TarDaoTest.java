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

package nl.knaw.dans.catalog.db;

import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.catalog.core.OcflObjectVersion;
import nl.knaw.dans.catalog.core.Tar;
import nl.knaw.dans.catalog.core.TarPart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
@Slf4j
class TarDaoTest {
    //
    private final DAOTestExtension daoTestRule = DAOTestExtension.newBuilder()
        .addEntityClass(Tar.class)
        .addEntityClass(TarPart.class)
        .addEntityClass(OcflObjectVersion.class)
        .build();

    private TarDao tarRepository;
    private OcflObjectVersionDao ocflObjectVersionRepository;

    @BeforeEach
    void setUp() {
        tarRepository = new TarDao(daoTestRule.getSessionFactory());
        ocflObjectVersionRepository = new OcflObjectVersionDao(daoTestRule.getSessionFactory());
    }

    @Test
    void save_should_persist_to_db() {
        daoTestRule.inTransaction(() -> {
            var tar = Tar.builder()
                .tarUuid("uuid1")
                .vaultPath("path")
                .archivalTimestamp(OffsetDateTime.now())
                .build();

            var version1 = OcflObjectVersion.builder()
                .bagId("bagid")
                .objectVersion(1)
                .otherId("TEST")
                .created(OffsetDateTime.now())
                .build();

            var part1 = TarPart.builder()
                .partName("0000")
                .checksumAlgorithm("md5")
                .checksumValue("cs")
                .build();

            tar.setTarParts(List.of(part1));
            tar.setOcflObjectVersions(List.of(version1));

            tarRepository.save(tar);
            tarRepository.evict(tar);

            version1.setTar(tar);
        });

        var tars = tarRepository.findAll();
        assertThat(tars).extracting("tarUuid").containsOnly("uuid1");
        assertThat(tars.get(0).getOcflObjectVersions()).size().isEqualTo(1);
        assertThat(tars.get(0).getOcflObjectVersions().get(0).getOtherId()).isEqualTo("TEST");
    }

    @Test
    void deleting_tar_should_not_delete_OcflObjectVersionEntity() {
        daoTestRule.inTransaction(() -> {
            var tar = Tar.builder()
                .tarUuid("uuid1")
                .vaultPath("path")
                .archivalTimestamp(OffsetDateTime.now())
                .build();

            var version1 = OcflObjectVersion.builder()
                .bagId("bagid")
                .objectVersion(1)
                .otherId("TEST")
                .created(OffsetDateTime.now())
                .build();

            var part1 = TarPart.builder()
                .partName("0000")
                .checksumAlgorithm("md5")
                .checksumValue("cs")
                .build();

            tar.setOcflObjectVersions(List.of(version1));
            tar.setTarParts(List.of(part1));

            tarRepository.save(tar);
            tarRepository.evict(tar);
        });

        daoTestRule.inTransaction(() -> {
            var versionsBefore = ocflObjectVersionRepository.findAll();
            assertThat(versionsBefore).isNotEmpty();

            var partsBefore = tarRepository.findAllParts();
            assertThat(partsBefore).isNotEmpty();
        });

        // delete the tar
        daoTestRule.inTransaction(() -> {
            var tar = tarRepository.findAll().get(0);
            tarRepository.delete(tar);
        });

        daoTestRule.inTransaction(() -> {
            var tars = tarRepository.findAll();
            assertThat(tars).isEmpty();

            // these should not be deleted
            var versions = ocflObjectVersionRepository.findAll();
            assertThat(versions)
                .isNotEmpty()
                .extracting("tar")
                .containsOnlyNulls();

            // the parts are directly linked to tars, so no need to keep them around
            var parts = tarRepository.findAllParts();
            assertThat(parts).isEmpty();
        });
    }
}
