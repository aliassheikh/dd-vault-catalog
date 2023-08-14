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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.knaw.dans.catalog.api.OcflObjectVersionRefDto;
import org.hibernate.Hibernate;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "ocfl_object_versions", uniqueConstraints = { @UniqueConstraint(columnNames = { "bag_id", "object_version" }) })
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@TypeDefs({
    @TypeDef(name = "string", defaultForType = java.lang.String.class, typeClass = org.hibernate.type.TextType.class)
})
public class OcflObjectVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bag_id", nullable = false)
    private String bagId;

    @Column(name = "object_version", nullable = false)
    private Integer objectVersion;

    @Column(name = "nbn")
    private String nbn;

    @Column(name = "sword_token")
    private String swordToken;

    @Column(name = "data_supplier")
    private String dataSupplier;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinColumn(name = "tar_uuid")
    private Tar tar;

    @Column(name = "ocfl_object_path")
    private String ocflObjectPath;

    @Column(name = "datastation")
    private String datastation;

    @Column(name = "dataverse_pid")
    private String dataversePid;

    @Column(name = "dataverse_pid_version")
    private String dataversePidVersion;

    @Column(name = "other_id")
    private String otherId;

    @Column(name = "other_id_version")
    private String otherIdVersion;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "filepid_to_local_path")
    private String filepidToLocalPath;

    @Column
    private Boolean deaccessioned;

    @Column
    private String exporter;

    @Column(name = "exporter_version")
    private String exporterVersion;

    @Column(name = "skeleton_record", nullable = false, columnDefinition = "boolean default false")
    private boolean skeletonRecord = false;

    @Column(name = "created", nullable = false, updatable = false)
    private OffsetDateTime created;

    @Column(name = "updated")
    private OffsetDateTime updated;

    public OcflObjectVersionRefDto getId() {
        return new OcflObjectVersionRefDto().bagId(bagId).objectVersion(objectVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        OcflObjectVersion that = (OcflObjectVersion) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public Long getInternalId() {
        return id;
    }

    public void setInternalId(Long id) {
        this.id = id;
    }
}
