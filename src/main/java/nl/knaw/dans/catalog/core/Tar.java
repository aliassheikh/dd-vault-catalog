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
import org.hibernate.Hibernate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ToString
@Getter
@Setter
@Entity
@Table(name = "tars")
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Tar {
    @Id
    @Column(name = "tar_uuid", nullable = false)
    private String tarUuid;
    @Column(name = "vault_path")
    private String vaultPath;
    @Column(name = "archival_timestamp")
    private OffsetDateTime archivalTimestamp;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "tar")
    @ToString.Exclude
    private List<TarPart> tarParts = new ArrayList<>();
    @OneToMany(cascade = { CascadeType.REFRESH }, mappedBy = "tar")
    @ToString.Exclude
    private List<OcflObjectVersion> ocflObjectVersions = new ArrayList<>();

    public List<TarPart> getTarParts() {
        return new ArrayList<>(tarParts);
    }

    public void setTarParts(List<TarPart> tarParts) {
        if (this.tarParts == null) {
            this.tarParts = new ArrayList<>();
        }

        this.tarParts.clear();

        for (var part : tarParts) {
            part.setTar(this);
            this.tarParts.add(part);
        }
    }

    public List<OcflObjectVersion> getOcflObjectVersions() {
        return new ArrayList<>(ocflObjectVersions);
    }

    public void setOcflObjectVersions(List<OcflObjectVersion> ocflObjectVersions) {
        if (this.ocflObjectVersions == null) {
            this.ocflObjectVersions = new ArrayList<>();
        }

        for (var version : this.ocflObjectVersions) {
            version.setTar(null);
        }

        this.ocflObjectVersions.clear();

        for (var version : ocflObjectVersions) {
            version.setTar(this);
            this.ocflObjectVersions.add(version);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        Tar tar = (Tar) o;
        return getTarUuid() != null && Objects.equals(getTarUuid(), tar.getTarUuid());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
