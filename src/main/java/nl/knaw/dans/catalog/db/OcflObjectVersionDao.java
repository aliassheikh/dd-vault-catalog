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

import io.dropwizard.hibernate.AbstractDAO;
import nl.knaw.dans.catalog.api.OcflObjectVersionRefDto;
import nl.knaw.dans.catalog.core.OcflObjectVersion;
import nl.knaw.dans.catalog.core.exception.OcflObjectVersionNotFoundException;
import org.hibernate.SessionFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class OcflObjectVersionDao extends AbstractDAO<OcflObjectVersion>  {
    public OcflObjectVersionDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Optional<OcflObjectVersion> findByBagIdAndVersion(String bagId, int version) {
        return query("from OcflObjectVersion where bagId = :bagId and objectVersion = :version order by id")
            .setParameter("bagId", bagId)
            .setParameter("version", version)
            .uniqueResultOptional();
    }

    public List<OcflObjectVersion> findAllByBagId(String bagId) {
        return new ArrayList<>(
            query("from OcflObjectVersion where bagId = :bagId order by objectVersion desc")
                .setParameter("bagId", bagId)
                .list()
        );
    }

    public List<OcflObjectVersion> findAll() {
        return new ArrayList<>(list(query("from OcflObjectVersion")));
    }

    public List<OcflObjectVersion> findAllBySwordToken(String swordToken) {
        return new ArrayList<>(
            query("from OcflObjectVersion where swordToken = :swordToken order by objectVersion desc")
                .setParameter("swordToken", swordToken)
                .list()
        );
    }

    public OcflObjectVersion save(OcflObjectVersion ocflObjectVersion) {
        findByBagIdAndVersion(ocflObjectVersion.getBagId(), ocflObjectVersion.getObjectVersion())
            .ifPresent(item -> {
                ocflObjectVersion.setInternalId(item.getInternalId());
                ocflObjectVersion.setUpdated(OffsetDateTime.now());
            });

        if (ocflObjectVersion.getInternalId() == null) {
            ocflObjectVersion.setCreated(OffsetDateTime.now());
        }

        var merged = (OcflObjectVersion) (currentSession().merge(ocflObjectVersion));
        return persist(merged);
    }

    public List<OcflObjectVersion> findAll(Collection<OcflObjectVersionRefDto> versions) throws OcflObjectVersionNotFoundException {
        var ocflObjectVersions = new ArrayList<OcflObjectVersion>();

        if (versions != null) {
            for (var version : versions) {
                var ocflObjectVersion = findByBagIdAndVersion(version.getBagId(), version.getObjectVersion())
                    .orElseThrow(() -> new OcflObjectVersionNotFoundException(
                        String.format("OcflObjectVersion with bagId %s and version %d not found", version.getBagId(), version.getObjectVersion())
                    ));

                ocflObjectVersions.add(ocflObjectVersion);
            }
        }

        return ocflObjectVersions;
    }


    public List<OcflObjectVersion> findByNbn(String nbn) {
        return new ArrayList<>(
            list(
                query(
                    "from OcflObjectVersion where nbn = :nbn order by objectVersion desc"
                ).setParameter("nbn", nbn)
            )
        );
    }
}
