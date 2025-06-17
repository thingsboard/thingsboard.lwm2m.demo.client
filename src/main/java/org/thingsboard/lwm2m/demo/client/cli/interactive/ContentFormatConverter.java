/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.lwm2m.demo.client.cli.interactive;

import org.eclipse.leshan.core.request.ContentFormat;
import picocli.CommandLine.ITypeConverter;

import java.util.Arrays;

public class ContentFormatConverter implements ITypeConverter<ContentFormat> {

    private final ContentFormat[] allowedContentFormat;

    public ContentFormatConverter() {
        this.allowedContentFormat = ContentFormat.knownContentFormat;
    }

    public ContentFormatConverter(ContentFormat... allowedContentFormats) {
        this.allowedContentFormat = allowedContentFormats;
    }

    @Override
    public ContentFormat convert(String value) throws Exception {
        // try to get format by name
        ContentFormat ct = ContentFormat.fromName(value);

        // if not found try to get format by code
        if (ct == null) {
            try {
                int code = Integer.parseInt(value);
                ct = ContentFormat.fromCode(code);
            } catch (NumberFormatException e) {
                // we do nothing more if value is not a integer, means user probably try to get the content format by
                // name
            }
        }

        if (ct == null) {
            throw new IllegalArgumentException(
                    String.format("%s is not a known content format name. Allowed Content Format are %s.", value,
                            Arrays.toString(allowedContentFormat)));
        }

        if (!Arrays.asList(allowedContentFormat).contains(ct)) {
            throw new IllegalArgumentException(
                    String.format("%s is not allowed for this operation. Allowed content format are %s.", ct,
                            Arrays.toString(allowedContentFormat)));
        }

        return ct;
    }

}
