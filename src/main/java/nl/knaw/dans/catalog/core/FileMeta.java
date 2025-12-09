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
import nl.knaw.dans.convert.jpa.UriConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.net.URI;

@Entity
@Table(name = "file_meta", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "version_export_id", "filepath" }),
    @UniqueConstraint(columnNames = { "version_export_id", "file_uri" })
})
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FileMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "version_export_id")
    @NotNull
    @JsonIgnore
    private DatasetVersionExport versionExport;

    // length = 2 * 255 + some margin
    @Column(name = "filepath", nullable = false, length = 520)
    private String filepath;

    @Column(name = "file_uri", nullable = false)
    @Convert(converter = UriConverter.class)
    private URI fileUri;

    @Column(name = "byte_size", nullable = false)
    private Long byteSize;
    
    @Column(name = "sha1sum", nullable = false) 
    private String sha1sum;
}
