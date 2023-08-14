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

package nl.knaw.dans.catalog.core.solr;

import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.catalog.DdVaultCatalogConfiguration;
import nl.knaw.dans.catalog.core.OcflObjectVersion;
import nl.knaw.dans.catalog.core.SearchIndex;
import nl.knaw.dans.catalog.core.Tar;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Slf4j
public class SolrIndex implements SearchIndex {
    private final Http2SolrClient solrClient;
    private final OcflObjectMetadataReader ocflObjectMetadataReader;
    private final String collection;

    public SolrIndex(DdVaultCatalogConfiguration.SolrConfig solrConfig, OcflObjectMetadataReader ocflObjectMetadataReader) {
        if (solrConfig != null) {
            solrClient = new Http2SolrClient.Builder(solrConfig.getUrl()).build();
            this.ocflObjectMetadataReader = ocflObjectMetadataReader;
            this.collection = solrConfig.getSchema();

            log.info("Solr configured, urn: {}, collection: {}", solrConfig.getUrl(), this.collection);
        }
        else {
            solrClient = null;
            this.ocflObjectMetadataReader = null;
            this.collection = null;
        }
    }

    @Override
    public void indexTar(Tar tar) {
        if (solrClient == null) {
            log.warn("Solr is not configured, skipping indexing of TAR {}", tar.getTarUuid());
            return;
        }

        try {
            var documents = tar.getOcflObjectVersions().stream()
                .map(this::mapOcflObjectVersion)
                .collect(Collectors.toList());

            log.debug("Indexing documents for TAR {}", tar.getTarUuid());
            solrClient.add(collection, documents);
            solrClient.commit(collection);
        }
        catch (Throwable e) {
            log.error("Error indexing TAR {}", tar.getTarUuid(), e);
        }
    }

    @Override
    public void indexOcflObjectVersion(OcflObjectVersion ocflObjectVersion) {
        if (solrClient == null) {
            log.warn("Solr is not configured, skipping indexing of OcflObjectVersion {}", ocflObjectVersion.getId());
            return;
        }

        var doc = mapOcflObjectVersion(ocflObjectVersion);

        try {
            log.debug("Indexing document with ID {}", ocflObjectVersion.getId());
            solrClient.add(collection, doc);
            solrClient.commit(collection);
        }
        catch (Throwable e) {
            log.error("Error indexing OcflObjectVersion {}", ocflObjectVersion.getId(), e);
        }
    }

    SolrInputDocument mapOcflObjectVersion(OcflObjectVersion ocflObjectVersion) {
        var doc = new SolrInputDocument();
        var id = String.format("%s/%s", ocflObjectVersion.getId().getBagId(), ocflObjectVersion.getId().getObjectVersion());

        doc.setField("id", id);
        doc.setField("bag_id", ocflObjectVersion.getId().getBagId());
        doc.setField("object_version", ocflObjectVersion.getId().getObjectVersion());
        doc.setField("nbn", ocflObjectVersion.getNbn());
        doc.setField("dataverse_pid", ocflObjectVersion.getDataversePid());
        doc.setField("dataverse_pid_version", ocflObjectVersion.getDataversePidVersion());
        doc.setField("datastation", ocflObjectVersion.getDatastation());
        doc.setField("data_supplier", ocflObjectVersion.getDataSupplier());
        doc.setField("sword_token", ocflObjectVersion.getSwordToken());
        doc.setField("other_id", ocflObjectVersion.getOtherId());
        doc.setField("other_id_version", ocflObjectVersion.getOtherIdVersion());
        doc.setField("filepid_to_local_path", ocflObjectVersion.getFilepidToLocalPath());
        doc.setField("ocfl_object_path", ocflObjectVersion.getOcflObjectPath());
        doc.setField("deaccessioned", ocflObjectVersion.getDeaccessioned());
        doc.setField("exporter", ocflObjectVersion.getExporter());
        doc.setField("exporter_version", ocflObjectVersion.getExporterVersion());

        // make the ID's searchable
        doc.addField("_text_", ocflObjectVersion.getId().getBagId().replace("urn:uuid:", ""));
        doc.addField("_text_", ocflObjectVersion.getNbn().replace("urn:nbn:nl:ui:", ""));

        if (ocflObjectVersion.getTar() != null) {
            doc.setField("tar_id", ocflObjectVersion.getTar().getTarUuid());
            doc.setField("tar_vault_path", ocflObjectVersion.getTar().getVaultPath());
            doc.setField("tar_archival_date", formatDate(ocflObjectVersion.getTar().getArchivalTimestamp()));

            for (var tarPart : ocflObjectVersion.getTar().getTarParts()) {
                doc.addField("tar_part_name", tarPart.getPartName());
                doc.addField("tar_part_checksum_algorithm", tarPart.getChecksumAlgorithm());
                doc.addField("tar_part_checksum_value", tarPart.getChecksumValue());
            }
        }

        var metadata = ocflObjectMetadataReader.readMetadata(ocflObjectVersion.getMetadata());

        for (var entry : metadata.getMetadata().entrySet()) {
            var fieldName = entry.getKey() + "_txt";

            for (var value : entry.getValue()) {
                doc.addField(fieldName, value);
            }
        }

        // specific metadata we want to store so we can show it in search results
        doc.addField("title", metadata.getTitle());
        doc.addField("description", metadata.getDescription());

        return doc;
    }

    String formatDate(OffsetDateTime date) {
        if (date == null) {
            return null;
        }

        return date.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
