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
import nl.knaw.dans.catalog.Conversions;
import nl.knaw.dans.catalog.api.OcflObjectVersionDto;
import nl.knaw.dans.catalog.api.OcflObjectVersionParametersDto;
import nl.knaw.dans.catalog.api.OcflObjectVersionRefDto;
import nl.knaw.dans.catalog.api.TarDto;
import nl.knaw.dans.catalog.api.TarParameterDto;
import nl.knaw.dans.catalog.core.exception.OcflObjectVersionAlreadyExistsException;
import nl.knaw.dans.catalog.core.exception.OcflObjectVersionAlreadyInTarException;
import nl.knaw.dans.catalog.core.exception.OcflObjectVersionNotFoundException;
import nl.knaw.dans.catalog.core.exception.TarAlreadyExistsException;
import nl.knaw.dans.catalog.core.exception.TarNotFoundException;
import nl.knaw.dans.catalog.db.OcflObjectVersionDao;
import nl.knaw.dans.catalog.db.TarDao;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class UseCases {
    private final OcflObjectVersionDao ocflObjectVersionDao;
    private final TarDao tarDao;
    private final Conversions conversions = Mappers.getMapper(Conversions.class);

    public UseCases(OcflObjectVersionDao ocflObjectVersionDao, TarDao tarDao, SearchIndex searchIndex) {
        this.ocflObjectVersionDao = ocflObjectVersionDao;
        this.tarDao = tarDao;
    }


    @UnitOfWork
    public Collection<OcflObjectVersionDto> findOcflObjectVersionsByBagId(String bagId) {
        return ocflObjectVersionDao.findAllByBagId(bagId).stream().map(conversions::convert).collect(Collectors.toList());
    }

    @UnitOfWork
    public Collection<OcflObjectVersionDto> findOcflObjectVersionsBySwordToken(String swordToken) {
        return ocflObjectVersionDao.findAllBySwordToken(swordToken).stream().map(conversions::convert).collect(Collectors.toList());
    }


    @UnitOfWork
    public List<OcflObjectVersionDto> findOcflObjectVersionsByNbn(String nbn) throws OcflObjectVersionNotFoundException {
        var results = ocflObjectVersionDao.findByNbn(nbn);

        log.info("Found {} OCFL object versions for NBN {}", results.size(), nbn);

        if (results.isEmpty()) {
            throw new OcflObjectVersionNotFoundException(
                String.format("No OCFL object versions found for NBN %s", nbn)
            );
        }

        return results.stream().map(conversions::convert).collect(Collectors.toList());
    }

    @UnitOfWork
    public Optional<OcflObjectVersionDto> findOcflObjectVersionByBagIdAndVersion(String bagId, Integer versionNumber) {
        return ocflObjectVersionDao.findByBagIdAndVersion(bagId, versionNumber).map(conversions::convert);
    }


    @UnitOfWork
    public OcflObjectVersionDto createOcflObjectVersion(OcflObjectVersionRefDto id, OcflObjectVersionParametersDto parameters) throws OcflObjectVersionAlreadyExistsException {
        var ocflObjectVersion = conversions.convert(parameters);
        ocflObjectVersion.setObjectVersion(id.getObjectVersion());
        ocflObjectVersion.setBagId(id.getBagId());

        log.info("Creating new OCFL object version with bagId {} and version {}: {}", id.getBagId(), id.getObjectVersion(), ocflObjectVersion);
        ocflObjectVersionDao.save(ocflObjectVersion);

        return conversions.convert(ocflObjectVersion);
    }

    @UnitOfWork
    public TarDto createTar(String id, TarParameterDto params) throws TarAlreadyExistsException, OcflObjectVersionNotFoundException, OcflObjectVersionAlreadyInTarException {
        var existingTar = tarDao.getTarById(id);

        if (existingTar.isPresent()) {
            log.debug("Found existing tar with id {}: {}", id, existingTar.get());
            throw new TarAlreadyExistsException(id);
        }

        // throws exception if one cannot be found
        var ocflObjectVersions = ocflObjectVersionDao.findAll(params.getOcflObjectVersions());

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

        var tar = conversions.convert(params);
        tar.setTarUuid(id);
        tar.setOcflObjectVersions(ocflObjectVersions);


        log.info("Saving new TAR {}", tar);
        var result = tarDao.save(tar);

        return conversions.convert(result);
    }

    @UnitOfWork
    public Optional<TarDto> findTarById(String id) {
        return tarDao.getTarById(id).map(conversions::convert);
    }

    @UnitOfWork
    public TarDto updateTar(String id, TarParameterDto params) throws TarNotFoundException, OcflObjectVersionNotFoundException, OcflObjectVersionAlreadyInTarException {
        var tar = tarDao.getTarById(id)
            .orElseThrow(() -> new TarNotFoundException(
                String.format("Tar with id %s not found", id)
            ));

        log.info("Found existing tar with id {}: {}", id, tar);
        var ocflObjectVersions = ocflObjectVersionDao.findAll(params.getOcflObjectVersions());

        // check if all ocfl object versions are not already in a tar
        for (var version : ocflObjectVersions) {
            if (version.getTar() != null && !version.getTar().equals(tar)) {
                throw new OcflObjectVersionAlreadyInTarException(String.format(
                    "OcflObjectVersion with bagId %s and version %d is already in TAR %s, cannot add to TAR %s",
                    version.getId().getBagId(), version.getId().getObjectVersion(), version.getTar().getTarUuid(), tar.getTarUuid()
                ));
            }
        }

        var parts = params.getTarParts().stream().map(conversions::convert).collect(Collectors.toList());
        tar.setArchivalTimestamp(params.getArchivalTimestamp());
        tar.setVaultPath(params.getVaultPath());
        tar.setTarParts(parts);
        tar.setOcflObjectVersions(ocflObjectVersions);

        log.info("Updating TAR {}", tar);
        var result = tarDao.save(tar);

        return conversions.convert(result);
    }
}
