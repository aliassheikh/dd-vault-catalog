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

import io.dropwizard.views.common.View;
import lombok.Getter;
import nl.knaw.dans.catalog.core.Dataset;
import nl.knaw.dans.catalog.core.DatasetVersionExport;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Getter
public class DatasetView extends View {

    private final Dataset dataset;
    private final List<DatasetVersionExport> datasetVersionExports;
    private final String title;

    protected DatasetView(Dataset dataset) {
        super("dataset.mustache");
        /*
         * N.B. Everything to be displayed must be fetched from the database before the View object leaves the @UnitOfWork
         * scope. Otherwise, the database may be accessed outside a transaction (especially for LOB fields), which is not
         * allowed. This is even true for fields that are not explicitly references from the template, such as 'metadata'.
         * This is why a copy of the Dataset is made here, with the 'metadata' field set to null. As an alternative, the
         * complete Dataset, DVE and all its fields could be fetched eagerly, but that could be inefficient.
         */
        this.dataset = dataset;
        this.datasetVersionExports = copyWithoutMetadataField(dataset.getDatasetVersionExports()).stream()
                .sorted(Comparator.comparing(DatasetVersionExport::getOcflObjectVersionNumber).reversed())
                .toList();
        if (dataset.getDatasetVersionExports().isEmpty()) {
            this.title = "<no title>";
        }
        else {
            this.title = Optional.ofNullable(dataset.getDatasetVersionExports().get(dataset.getDatasetVersionExports().size() - 1).getTitle()).orElse("<no title>");
        }
    }

    private List<DatasetVersionExport> copyWithoutMetadataField(List<DatasetVersionExport> originalDves) {
        List<DatasetVersionExport> copy = new ArrayList<>();
        for (DatasetVersionExport dve : originalDves) {
            DatasetVersionExport dveCopy = new DatasetVersionExport();
            dveCopy.setId(dve.getId());
            dveCopy.setDataset(dve.getDataset());
            dveCopy.setBagId(dve.getBagId());
            dveCopy.setOcflObjectVersionNumber(dve.getOcflObjectVersionNumber());
            dveCopy.setCreatedTimestamp(dve.getCreatedTimestamp());
            dveCopy.setTitle(dve.getTitle());
            dveCopy.setArchivedTimestamp(dve.getArchivedTimestamp());
            dveCopy.setDataversePidVersion(dve.getDataversePidVersion());
            dveCopy.setOtherId(dve.getOtherId());
            dveCopy.setOtherIdVersion(dve.getOtherIdVersion());
            dveCopy.setMetadata(null); // Not displayed, so avoid retrieving it from the database
            dveCopy.setDeaccessioned(dve.getDeaccessioned());
            dveCopy.setExporter(dve.getExporter());
            dveCopy.setExporterVersion(dve.getExporterVersion());
            dveCopy.setSkeletonRecord(dve.getSkeletonRecord());
            for (var fileMeta : dve.getFileMetas()) {
                dveCopy.addFileMeta(fileMeta);
            }
            copy.add(dveCopy);
        }
        return copy;
    }
}
