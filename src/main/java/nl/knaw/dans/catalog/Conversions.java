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
package nl.knaw.dans.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.dans.catalog.api.OcflObjectVersionDto;
import nl.knaw.dans.catalog.api.OcflObjectVersionParametersDto;
import nl.knaw.dans.catalog.api.TarDto;
import nl.knaw.dans.catalog.api.TarParameterDto;
import nl.knaw.dans.catalog.api.TarPartDto;
import nl.knaw.dans.catalog.api.TarPartParameterDto;
import nl.knaw.dans.catalog.core.OcflObjectVersion;
import nl.knaw.dans.catalog.core.Tar;
import nl.knaw.dans.catalog.core.TarPart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.UUID;

/**
 * Conversion between DTOs and domain objects.
 */
@Mapper
public interface Conversions {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    Tar convert(TarParameterDto parameters);

    TarDto convert(Tar tar);

    @Mapping(source = "tar.tarUuid", target = "tarUuid")
    TarPartDto convert(TarPart tarPart);

    TarPart convert(TarPartParameterDto parameters);


    OcflObjectVersion convert(OcflObjectVersionParametersDto parameters);

    @Mapping(target = "tarUuid", source = "tar.tarUuid")
    OcflObjectVersionDto convert(OcflObjectVersion version);

    default UUID stringToUuid(String value) {
        if (value == null) {
            return null;
        }
        return UUID.fromString(value);
    }

    default String uuidToString(UUID value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }


    default String objectToString(Object value) {
        if (value == null) {
            return null;
        }

        return value.toString();
    }

    default String mapToJsonString(Map<String, Object> value) throws JsonProcessingException {
        if (value == null) {
            return null;
        }

        return OBJECT_MAPPER.writeValueAsString(value);
    }

    default Map<String, Object> jsonStringToMap(String value) {
        if (value == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(value, new TypeReference<>() {

            });
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Unable to parse JSON: %s", value), e);
        }
    }
}
