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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DatasetVersionExportTest {

    @Test
    public void setTitle_should_allow_null() {
        var dve = new DatasetVersionExport();
        dve.setTitle(null);
        assertThat(dve.getTitle()).isNull();
    }

    @Test
    public void setTitle_should_keep_title_when_length_is_at_most_300() {
        var title = "a".repeat(300);
        var dve = new DatasetVersionExport();
        dve.setTitle(title);
        assertThat(dve.getTitle()).isEqualTo(title);
        assertThat(dve.getTitle()).hasSize(300);
    }

    @Test
    public void setTitle_should_ellipsize_when_length_exceeds_300() {
        var original = "x".repeat(301);
        var dve = new DatasetVersionExport();
        dve.setTitle(original);
        assertThat(dve.getTitle()).isNotNull();
        assertThat(dve.getTitle()).hasSize(300);
        assertThat(dve.getTitle()).isEqualTo(original.substring(0, 297) + "...");
    }
}
