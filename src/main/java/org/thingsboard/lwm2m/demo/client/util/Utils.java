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
package org.thingsboard.lwm2m.demo.client.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.scandium.dtls.MaxFragmentLengthExtension.Length;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.demo.LwM2mDemoConstant;
import org.eclipse.leshan.core.model.LwM2mModelRepository;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.util.StringUtils;
import org.thingsboard.lwm2m.demo.client.cli.TBSectionsCliMain;
import org.thingsboard.lwm2m.demo.client.entities.LwM2MClientOtaInfo;
import org.thingsboard.lwm2m.demo.client.entities.OtaPackageType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public class Utils {

    public static final int BINARY_APP_DATA_CONTAINER = 19;
    public static final int OBJECT_ID_TEMPERATURE_SENSOR = 3303;
    public static final int OBJECT_ID_LWM2M_TEST_OBJECT = 3442;
    public static final String CF_CONFIGURATION_FILENAME = "Californium3.client.properties";
    public static final String CF_CONFIGURATION_HEADER = "Thingsboard Lwm2m Demo Client - " + Configuration.DEFAULT_HEADER;

    public static final String OTA_FOLDER_DEF = "./ota";
    public static final String FW_DATA_FILE_NANE_DEF = "FW_OtaPackage.bin";
    public static final String FW_INFO_FILE_NANE_DEF = "FW_Ota.json";
    public static final String SW_DATA_FILE_NANE_DEF = "SW_OtaPackage.bin";
    public static final String SW_INFO_FILE_NANE_DEF = "SW_Ota.json";
    public static final String PREF_TMP = "_tmp";
    public static final String PREF_FW = "FW_";
    public static final String PREF_SW = "SW_";
    public static final Integer FW_INFO_19_INSTANCE_ID = 65533;
    public static final Integer SW_INFO_19_INSTANCE_ID = 65534;
    private static String otaFolder;
    private static LwM2MClientOtaInfo otaInfoUpdateFw;
    private static LwM2MClientOtaInfo otaInfoUpdateSw;

    public static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    public static LwM2mModelRepository createModel(TBSectionsCliMain cli) throws Exception {

        List<ObjectModel> models = ObjectLoader.loadAllDefault();
        models.addAll(ObjectLoader.loadDdfResources("/models", LwM2mDemoConstant.modelPaths));
        if (cli.main.modelsFolder != null) {
            List<ObjectModel> modelsCli = ObjectLoader.loadObjectsFromDir(cli.main.modelsFolder, true);
            Set<Integer> idsToRemove = new HashSet<>();
            for (ObjectModel model : modelsCli) {
                idsToRemove.add(model.id);
            }
            models.removeIf(model -> idsToRemove.contains(model.id));
            models.addAll(modelsCli);
        }
        return new LwM2mModelRepository(models);
    }
    public static Length fromLength(int length) {
        for (Length l : Length.values()) {
            if (l.length() == length) {
                return l;
            }
        }
        return null;
    }

    public static <T> T fromString(String string, Class<T> clazz) {
        try {
            return string != null ? OBJECT_MAPPER.readValue(string, clazz) : null;
        } catch (IOException e) {
            throw new IllegalArgumentException("The given string value cannot be transformed to Json object: " + string, e);
        }
    }

    public static JsonNode toJsonNode(String value) {
        return toJsonNode(value, OBJECT_MAPPER);
    }

    public static JsonNode toJsonNode(String value, ObjectMapper mapper) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return mapper.readTree(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T treeToValue(JsonNode node, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.treeToValue(node, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't convert value: " + node.toString(), e);
        }
    }

    public static void writeOtaInfoToFile(String filePath, LwM2MClientOtaInfo info){
        try {
            Path dirPath = Paths.get(filePath).getParent();
            Files.createDirectories(dirPath);
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), info);
            log.info("New otsInfo successfully saved to: \"{}\" [{}].", filePath, info);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't write to file: \"" + filePath + "\" value: [" + info.toString() + "]", e);
        }
    }
    public static LwM2MClientOtaInfo readOtaInfoFromFile(String filePath){
        try {
            return OBJECT_MAPPER.readValue(new File(filePath), LwM2MClientOtaInfo.class);
        } catch (IOException e) {
            log.error("Can't read from file: \"" + filePath + "\" OtaInfo. [{}]", e.getMessage());
            return null;
        }
    }

    public static String getOtaFolder() {
        return StringUtils.isEmpty(Utils.otaFolder) ? OTA_FOLDER_DEF : Utils.otaFolder;
    }

    public static void setOtaFolder(String otaFolder) {
        Utils.otaFolder = otaFolder;
    }

    public static void setOtaInfoUpdateFw(LwM2MClientOtaInfo otaInfoUpdateFW) {
        Utils.otaInfoUpdateFw = otaInfoUpdateFW;
    }

    public static LwM2MClientOtaInfo getOtaInfoUpdateFw(){
        return Utils.otaInfoUpdateFw;
    }

    public static void setOtaInfoUpdate(LwM2MClientOtaInfo otaInfoUpdate) {
        if (otaInfoUpdate.getType().equals(OtaPackageType.FIRMWARE)) {
            setOtaInfoUpdateFw(otaInfoUpdate);
        } else {
            setOtaInfoUpdateSw(otaInfoUpdate);
        }
    }
    public static void setOtaInfoUpdateSw(LwM2MClientOtaInfo otaInfoUpdateSW) {
        Utils.otaInfoUpdateSw = otaInfoUpdateSW;
    }

    public static LwM2MClientOtaInfo getOtaInfoUpdateSw(){
        return Utils.otaInfoUpdateSw;
    }

    public static String getPathInfoOtaFw() {
        return getOtaFolder() + "/" + FW_INFO_FILE_NANE_DEF;
    }
    public static String getPathInfoOtaSw() {
        return getOtaFolder() + "/" + SW_INFO_FILE_NANE_DEF;
    }

    public static void renameOtaFilesToTmp(Path directory, String mask, String prefix) {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(p -> p.getFileName().toString().contains(mask))
                    .forEach(p -> {
                        String fileName = p.getFileName().toString();
                        int dotIndex = fileName.lastIndexOf('.');
                        String newFileName = fileName.substring(0, dotIndex) + prefix + fileName.substring(dotIndex);
                        Path target = p.resolveSibling(newFileName);
                        try {
                            Files.move(p, target, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            log.error("Failed rename file [{}] in directory: -> [{}] [{}]", fileName, directory, e.getMessage());
                        }
                        log.info("Renamed file: [{}] -> [{}]", p.getFileName(), target.getFileName());

                    });
        } catch (IOException e) {
            log.error("Failed rename any file in directory: -> [{}] [{}]", directory, e.getMessage());
        }
    }

    public static void deleteOtaFiles(Path directory, String mask) {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(p -> p.getFileName().toString().contains(mask))
                    .forEach(p -> {
                        waitUntilDeleted(p, Duration.ofSeconds(1));
                    });
        } catch (IOException e) {
            log.error("Failed to list directory: -> [{}] [{}]", directory, e.getMessage());
        }
    }

    private static boolean waitUntilDeleted(Path path, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Failed to delete file: -> [{}] [{}].", path, e.getMessage());
        }
        while (System.currentTimeMillis() < deadline) {
            if (!Files.exists(path)) {
                log.info("Successfully to delete file: -> [{}].", path);
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return !Files.exists(path);
    }

    public static void printReadLog(LwM2mServer server, String nameClazz, int modelId, int instanceId, int resourceId, Object value) {
        if (!server.isSystem()) {
            log.info("Read on {} resource /{}/{}/{} = {}", nameClazz, modelId, instanceId, resourceId, value);
        }
    }

    public static String printMap (Map<Integer, ?> map) {
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    String.format("Failed to serialize map: %s | Error: %s", map, e.getMessage()), e
            );
        }
    }
}
