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
import nl.knaw.dans.catalog.core.DatasetVersionExport;
import nl.knaw.dans.validation.UrnUuid;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.net.URI;
import java.util.UUID;
import java.util.stream.Stream;

public class DatasetVersionExportDao extends AbstractDAO<DatasetVersionExport> {
    public DatasetVersionExportDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public DatasetVersionExport findByBagId(@UrnUuid URI bagId) {
        
        
        CriteriaBuilder cb = currentSession().getCriteriaBuilder();
        CriteriaQuery<DatasetVersionExport> cq = cb.createQuery(DatasetVersionExport.class);
        Root<DatasetVersionExport> root = cq.from(DatasetVersionExport.class);

        Predicate bagIdPredicate = cb.equal(root.get("bagId"), bagId);
        cq.where(bagIdPredicate);

        return uniqueResult(currentSession().createQuery(cq));
    }

    public void add(DatasetVersionExport datasetVersionExport) {
        currentSession().save(datasetVersionExport);
    }

    public Stream<DatasetVersionExport> streamAll() {
        CriteriaBuilder cb = currentSession().getCriteriaBuilder();
        CriteriaQuery<DatasetVersionExport> cq = cb.createQuery(DatasetVersionExport.class);
        Root<DatasetVersionExport> root = cq.from(DatasetVersionExport.class);
        cq.select(root);
        return currentSession().createQuery(cq).stream();
    }
}
