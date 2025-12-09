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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.knaw.dans.convert.jpa.UrnUuidConverter;
import nl.knaw.dans.validation.UrnUuid;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dataset_version_export", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "dataset_id", "ocfl_object_version_number"
    })
})
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DatasetVersionExport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dataset_id")
    @NotNull
    @JsonIgnore
    private Dataset dataset;

    @Column(name = "bag_id", nullable = false)
    @Convert(converter = UrnUuidConverter.class)
    @UrnUuid
    private URI bagId;

    @Column(name = "ocfl_object_version_number", nullable = false)
    private Integer ocflObjectVersionNumber;

    @Column(name = "created_timestamp", nullable = false)
    private OffsetDateTime createdTimestamp;

    @Column(name = "archived_timestamp")
    private OffsetDateTime archivedTimestamp;

    @Column(name = "title", length = 300)
    private String title;

    @Column(name = "dataverse_pid_version")
    private String dataversePidVersion;

    @Column(name = "other_id")
    private String otherId;

    @Column(name = "other_id_version")
    private String otherIdVersion;

    @Lob
    @Column(name = "metadata")
    private String metadata;

    @Column(name = "deaccessioned")
    private Boolean deaccessioned;

    @Column(name = "exporter")
    private String exporter;

    @Column(name = "exporter_version")
    private String exporterVersion;

    @Column(name = "skeleton_record")
    private Boolean skeletonRecord;

    @OneToMany(mappedBy = "versionExport", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<FileMeta> fileMetas = new ArrayList<>();

    public void addFileMeta(FileMeta fileMeta) {
        fileMetas.add(fileMeta);
        fileMeta.setVersionExport(this);
    }

    public void setTitle(String title) {
        final String ellipsis = "...";
        if (title != null && title.length() > 300) {
            this.title = title.substring(0, 300 - ellipsis.length()) + ellipsis;
        }
        else {
            this.title = title;
        }
    }
}
