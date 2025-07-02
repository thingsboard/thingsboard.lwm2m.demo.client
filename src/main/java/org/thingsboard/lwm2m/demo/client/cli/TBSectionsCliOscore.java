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
package org.thingsboard.lwm2m.demo.client.cli;

import org.eclipse.californium.elements.util.Bytes;
import org.eclipse.leshan.core.demo.cli.MultiParameterException;
import org.eclipse.leshan.core.demo.cli.converters.HexadecimalConverter;
import org.eclipse.leshan.core.oscore.AeadAlgorithm;
import org.eclipse.leshan.core.oscore.HkdfAlgorithm;
import org.eclipse.leshan.core.oscore.InvalidOscoreSettingException;
import org.eclipse.leshan.core.oscore.OscoreSetting;
import org.eclipse.leshan.core.oscore.OscoreValidator;
import org.eclipse.leshan.core.util.StringUtils;
import picocli.CommandLine;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.TypeConversionException;

import java.util.Arrays;

/**
 * Command line Section about OSCORE credentials.
 */
public class TBSectionsCliOscore {

    // mandatory parameters
    @Option(required = true,
            names = { "-sid", "--sender-id" },
            description = { "Hexadecimal byte string used to identify the Sender Context, to"
                    + " derive AEAD keys and Common IV, and to contribute to the"
                    + " uniqueness of AEAD nonces.  Maximum length is determined by the AEAD Algorithm." },
            converter = HexadecimalConverter.class)
    public Bytes senderId;

    @Option(required = true,
            names = { "-msec", "--master-secret" },
            description = { "Variable length, Hexadecimal byte string used to derive AEAD keys and Common IV." },
            converter = HexadecimalConverter.class)
    public Bytes masterSecret;

    @Option(required = true,
            names = { "-rid", "--recipient-id" },
            description = { "Hexadecimal byte string used to identify the Recipient Context,"
                    + " to derive AEAD keys and Common IV, and to contribute to the"
                    + " uniqueness of AEAD nonces.  Maximum length is determined by the AEAD Algorithm." },
            converter = HexadecimalConverter.class)
    public Bytes recipientId;

    // optional parameters
    @Option(required = true,
            names = { "-aead", "--aead-algorithm" },
            description = { "The COSE AEAD algorithm to use for encryption.", "Default is ${DEFAULT-VALUE}." },
            defaultValue = "AES-CCM-16-64-128",
            converter = AeadAlgorithmConverter.class)
    public Integer aeadAlgorithm;

    private static class AeadAlgorithmConverter implements ITypeConverter<Integer> {
        @Override
        public Integer convert(String s) {
            AeadAlgorithm aeadAlgorithm;
            if (StringUtils.isNumeric(s)) {
                try {
                    // Indicated as integer
                    aeadAlgorithm = AeadAlgorithm.fromValue(Integer.parseInt(s));
                } catch (NumberFormatException e) {
                    aeadAlgorithm = null;
                }
            } else {
                // Indicated as string
                aeadAlgorithm = AeadAlgorithm.fromName(s);
            }
            if (aeadAlgorithm == null || !aeadAlgorithm.isKnown()) {
                throw new TypeConversionException(
                        String.format("Unkown AEAD Algorithm for [%s] \nSupported AEAD algorithm are %s.", s,
                                Arrays.toString(AeadAlgorithm.knownAeadAlgorithms)));
            }
            return aeadAlgorithm.getValue();
        }
    };

    @Option(required = true,
            names = { "-msalt", "--master-salt" },
            description = {
                    "Optional variable-length hexadecimal byte string containing the salt used to derive AEAD keys and Common IV.",
                    "Default is an empty string." },
            defaultValue = "",
            converter = HexadecimalConverter.class)
    public Bytes masterSalt;

    @Option(required = true,
            names = { "-hkdf", "--hkdf-algorithm" },
            description = {
                    "An HMAC-based key derivation function used to derive the Sender Key, Recipient Key, and Common IV.",
                    "Default is ${DEFAULT-VALUE}." },
            defaultValue = "HKDF-SHA-256",
            converter = hkdfAlgorithmConverter.class)
    public Integer hkdfAlgorithm;

    private static class hkdfAlgorithmConverter implements ITypeConverter<Integer> {
        @Override
        public Integer convert(String s) {
            HkdfAlgorithm hkdfAlgorithm;
            if (s.matches("-?\\d+")) {
                try {
                    // Indicated as integer
                    hkdfAlgorithm = HkdfAlgorithm.fromValue(Integer.parseInt(s));
                } catch (NumberFormatException e) {
                    hkdfAlgorithm = null;
                }
            } else {
                // Indicated as string
                hkdfAlgorithm = HkdfAlgorithm.fromName(s);
            }
            if (hkdfAlgorithm == null || !hkdfAlgorithm.isKnown()) {
                throw new TypeConversionException(
                        String.format("Unkown HKDF Algorithm for [%s] \nSupported HKDF algorithm are %s.", s,
                                Arrays.toString(HkdfAlgorithm.knownHkdfAlgorithms)));
            }
            return hkdfAlgorithm.getValue();
        }
    };

    // ---------------------------------------------------------------------------------------//
    // TODO OSCORE I don't know if we need to add an option for anti-replay,
    // maybe this will be more an californium config, let's skip it for now.
    //
    // @Option(required = true, names = { "-rw", "--replay-windows" }, description = { "TBD." })
    // public String replayWindows;
    // ---------------------------------------------------------------------------------------//

    public OscoreSetting getOscoreSetting() {
        return new OscoreSetting(senderId.getBytes(), recipientId.getBytes(), masterSecret.getBytes(), aeadAlgorithm,
                hkdfAlgorithm, masterSalt.getBytes());
    }

    public void validateOscoreSetting(CommandLine commanLine) throws MultiParameterException {
        try {
            OscoreValidator validator = new OscoreValidator();
            validator.validateOscoreSetting(getOscoreSetting());
        } catch (IllegalArgumentException | InvalidOscoreSettingException e) {
            throw new MultiParameterException(commanLine, String.format("Invalid OSCORE setting : %s", e.getMessage()),
                    "-sid", "-msec", "-rid", "-aead", "-msalt", "-hkdf");
        }
    }
}
