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
import nl.knaw.dans.catalog.core.Tar;
import nl.knaw.dans.catalog.core.TarPart;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TarDao extends AbstractDAO<Tar>  {
    public TarDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Optional<Tar> getTarById(String id) {
        return query("from Tar t where tarUuid = :id")
            .setParameter("id", id)
            .uniqueResultOptional()
            .map(this::initializeChildren);
    }

    public Tar save(Tar tar) {
        for (var version : tar.getOcflObjectVersions()) {
            version.setTar(tar);
            currentSession().save(version);
        }

        for (var part : tar.getTarParts()) {
            part.setTar(tar);
        }

        var merged = (Tar) currentSession().merge(tar);
        return persist(merged);
    }

    public List<Tar> findAll() {
        return list(query("from Tar"))
            .stream().map(this::initializeChildren)
            .collect(Collectors.toList());
    }

    void evict(Tar tar) {
        currentSession().evict(tar);
        currentSession().flush();
    }

    void delete(Tar tar) {
        for (var version: tar.getOcflObjectVersions()) {
            version.setTar(null);
            currentSession().save(version);
        }

        currentSession().delete(tar);
        currentSession().flush();
    }

    List<TarPart> findAllParts() {
        return currentSession().createQuery("from TarPart", TarPart.class).list();
    }

    Tar initializeChildren(Tar entity) {
        Hibernate.initialize(entity.getTarParts());
        Hibernate.initialize(entity.getOcflObjectVersions());

        return entity;
    }
}
