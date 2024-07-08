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
import nl.knaw.dans.catalog.core.Dataset;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Optional;

public class DatasetDao extends AbstractDAO<Dataset> {

    public DatasetDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Optional<Dataset> findByNbn(String nbn) {
        CriteriaBuilder cb = currentSession().getCriteriaBuilder();
        CriteriaQuery<Dataset> cq = cb.createQuery(Dataset.class);
        Root<Dataset> root = cq.from(Dataset.class);
        cq.select(root).where(cb.equal(root.get("nbn"), nbn));
        Dataset dataset = currentSession().createQuery(cq).uniqueResult();
        return Optional.ofNullable(dataset);
    }

    public Optional<Dataset> findBySwordToken(String swordToken) {
        CriteriaBuilder cb = currentSession().getCriteriaBuilder();
        CriteriaQuery<Dataset> cq = cb.createQuery(Dataset.class);
        Root<Dataset> root = cq.from(Dataset.class);
        cq.select(root).where(cb.equal(root.get("swordToken"), swordToken));
        Dataset dataset = currentSession().createQuery(cq).uniqueResult();
        return Optional.ofNullable(dataset);
    }

    public Dataset save(Dataset dataset) {
        try {
            if (dataset.getId() == null || get(dataset.getId()) == null) {
                persist(dataset);
            }
            else {
                currentSession().update(dataset);
            }
            return dataset;
        }
        catch (ConstraintViolationException e) {
            throw new IllegalArgumentException(e.getSQLException().getMessage());
        }
    }
}
