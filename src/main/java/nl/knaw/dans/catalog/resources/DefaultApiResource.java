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

import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.catalog.api.AppInfoDto;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.message.BasicHeaderValueParser;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;

@Slf4j
public class DefaultApiResource implements DefaultApi {

    @Override
    public Response getInfo(String accept) {
        var parser = new BasicHeaderValueParser();
        var acceptedMediaTypes = Arrays.stream(parser.parseElements(accept, null))
            .toList().stream().map(HeaderElement::getName)
            .map(MediaType::valueOf);
        if (acceptedMediaTypes.anyMatch(MediaType.TEXT_HTML_TYPE::isCompatible) && !"*/*".equals(accept)) {
            return Response.ok(new FindDatasetView()).build();
        }
        else {
            return Response.ok(new AppInfoDto()
                .name("dd-vault-catalog")
                .version(this.getClass().getPackage().getImplementationVersion())).build();
        }

    }
}
