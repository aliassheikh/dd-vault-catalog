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
package nl.knaw.dans.catalog.core;

import io.dropwizard.hibernate.UnitOfWork;
import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.catalog.core.domain.OcflObjectVersionId;
import nl.knaw.dans.catalog.core.domain.OcflObjectVersionParameters;
import nl.knaw.dans.catalog.core.domain.TarParameters;
import nl.knaw.dans.catalog.core.exception.*;
import nl.knaw.dans.catalog.db.OcflObjectVersion;
import nl.knaw.dans.catalog.db.Tar;
import nl.knaw.dans.catalog.db.mappers.OcflObjectVersionMapper;
import nl.knaw.dans.catalog.db.mappers.TarMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class UseCases {
    private final OcflObjectVersionRepository ocflObjectVersionRepository;
    private final TarRepository tarRepository;
    private final SearchIndex searchIndex;

    private final TarMapper tarMapper = TarMapper.INSTANCE;
    private final OcflObjectVersionMapper ocflObjectVersionMapper = OcflObjectVersionMapper.INSTANCE;

    public UseCases(OcflObjectVersionRepository ocflObjectVersionRepository, TarRepository tarRepository, SearchIndex searchIndex) {
        this.ocflObjectVersionRepository = ocflObjectVersionRepository;
        this.tarRepository = tarRepository;
        this.searchIndex = searchIndex;
    }

    @UnitOfWork
    public Collection<OcflObjectVersion> findOcflObjectVersionsByBagId(String bagId) {
        return ocflObjectVersionRepository.findAllByBagId(bagId);
    }

    @UnitOfWork
    public Collection<OcflObjectVersion> findOcflObjectVersionsBySwordToken(String swordToken) {
        return ocflObjectVersionRepository.findAllBySwordToken(swordToken);
    }

    @UnitOfWork
    public List<OcflObjectVersion> findOcflObjectVersionsByNbn(String nbn) throws OcflObjectVersionNotFoundException {
        var results = ocflObjectVersionRepository.findByNbn(nbn);

        log.info("Found {} OCFL object versions for NBN {}", results.size(), nbn);

        if (results.size() == 0) {
            throw new OcflObjectVersionNotFoundException(
                String.format("No OCFL object versions found for NBN %s", nbn)
            );
        }

        return results;
    }

    @UnitOfWork
    public Optional<OcflObjectVersion> findOcflObjectVersionByBagIdAndVersion(String bagId, Integer versionNumber) {
        return ocflObjectVersionRepository.findByBagIdAndVersion(bagId, versionNumber);
    }

    @UnitOfWork
    public OcflObjectVersion createOcflObjectVersion(OcflObjectVersionId id, OcflObjectVersionParameters parameters) throws OcflObjectVersionAlreadyExistsException {
        var ocflObjectVersion = ocflObjectVersionMapper.convert(parameters);
        ocflObjectVersion.setObjectVersion(id.getObjectVersion());
        ocflObjectVersion.setBagId(id.getBagId());

        log.info("Creating new OCFL object version with bagId {} and version {}: {}", id.getBagId(), id.getObjectVersion(), ocflObjectVersion);
        ocflObjectVersionRepository.save(ocflObjectVersion);

        log.info("Indexing OCFL object version in search index: {}", ocflObjectVersion.getId());
        searchIndex.indexOcflObjectVersion(ocflObjectVersion);

        return ocflObjectVersion;
    }

    @UnitOfWork
    public Tar createTar(String id, TarParameters params) throws TarAlreadyExistsException, OcflObjectVersionNotFoundException, OcflObjectVersionAlreadyInTarException {
        var existingTar = tarRepository.getTarById(id);

        if (existingTar.isPresent()) {
            log.debug("Found existing tar with id {}: {}", id, existingTar.get());
            throw new TarAlreadyExistsException(id);
        }

        // throws exception if one cannot be found
        var ocflObjectVersions = ocflObjectVersionRepository.findAll(params.getVersions());

        // check if all ocfl object versions are not already in a tar
        for (var version : ocflObjectVersions) {
            if (version.getTar() != null) {
                throw new OcflObjectVersionAlreadyInTarException(String.format(
                    "OcflObjectVersion with bagId %s and version %d is already in TAR %s",
                    version.getId().getBagId(), version.getId().getObjectVersion(), version.getTar().getTarUuid()
                ));
            }
        }

        log.info("Successfully found all OCFL object versions for TAR {}", id);

        var tar = tarMapper.convert(params);
        tar.setTarUuid(id);
        tar.setOcflObjectVersions(ocflObjectVersions);

        log.info("Saving new TAR {}", tar);
        var result = tarRepository.save(tar);

        log.info("Indexing TAR in search index: {}", tar);
        searchIndex.indexTar(result);

        return result;
    }

    @UnitOfWork
    public Optional<Tar> findTarById(String id) {
        return tarRepository.getTarById(id);
    }

    @UnitOfWork
    public Tar updateTar(String id, TarParameters params) throws TarNotFoundException, OcflObjectVersionNotFoundException, OcflObjectVersionAlreadyInTarException {
        var tar = tarRepository.getTarById(id)
            .orElseThrow(() -> new TarNotFoundException(
                String.format("Tar with id %s not found", id)
            ));

        log.info("Found existing tar with id {}: {}", id, tar);
        var ocflObjectVersions = ocflObjectVersionRepository.findAll(params.getVersions());

        // check if all ocfl object versions are not already in a tar
        for (var version : ocflObjectVersions) {
            if (version.getTar() != null && !version.getTar().equals(tar)) {
                throw new OcflObjectVersionAlreadyInTarException(String.format(
                    "OcflObjectVersion with bagId %s and version %d is already in TAR %s, cannot add to TAR %s",
                    version.getId().getBagId(), version.getId().getObjectVersion(), version.getTar().getTarUuid(), tar.getTarUuid()
                ));
            }
        }

        var parts = params.getTarParts().stream().map(tarMapper::convert).collect(Collectors.toList());
        tar.setArchivalTimestamp(params.getArchivalTimestamp());
        tar.setVaultPath(params.getVaultPath());
        tar.setTarParts(parts);
        tar.setOcflObjectVersions(ocflObjectVersions);

        log.info("Updating TAR {}", tar);
        var result = tarRepository.save(tar);

        log.info("Reindexing TAR in search index: {}", tar);
        searchIndex.indexTar(result);

        return result;
    }


    @UnitOfWork
    public void reindexAllTars() {
        var tars = tarRepository.findAll();

        log.info("Reindexing {} archives", tars.size());

        for (var tar : tars) {
            log.info("Reindexing TAR {}", tar.getTarUuid());
            searchIndex.indexTar(tar);
        }
    }

}
