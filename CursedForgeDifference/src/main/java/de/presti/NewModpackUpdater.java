package de.presti;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.matyrobbrt.curseforgeapi.util.CurseForgeException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class NewModpackUpdater {

    static List<Integer> currentMods = new ArrayList<>();

    public static void updateModpack() throws CurseForgeException {
        Main.cfApi.getHelper().getMod(550864).ifPresent(c -> {
            try {
                Path filePath = Path.of("ModPacks", "LatestBMC.zip");
                Path endZip = Path.of("ModPacks", "Custom.zip");

                if (filePath.toFile().exists())
                    Files.delete(filePath);

                if (endZip.toFile().exists())
                    Files.delete(endZip);

                List<io.github.matyrobbrt.curseforgeapi.schemas.file.File> files = c.latestFiles();
                io.github.matyrobbrt.curseforgeapi.schemas.file.File file = files.get(files.size() - 1);

                System.out.println("Downloading " + file.fileName() + " from " + file.downloadUrl());

                file.download(filePath);

                String bmcVersion = file.displayName().replace("Better MC [FORGE] - 1.19.2 ", "").trim();
                System.out.println("BMC Version: " + bmcVersion);
                System.out.println("Custom Version: " + ModPackInfo.currentCustomVersion);

                File customDir = new File("ModPacks", "Custom");

                if (customDir.exists() && customDir.isDirectory())
                    FileUtil.deleteFolder(customDir);

                ZipFiles.unzip(filePath, customDir);
                System.out.println("Unzipped BMC");

                File manifest = new File(customDir, "manifest.json");

                new File(customDir, "modlist.html").delete();
                System.out.println("Deleted modlist.html");

                System.out.println("Starting to detox the modpack");
                detoxModList(manifest);
                System.out.println("Detoxed the Modlist.");

                File presetFiles = new File("ModPacks", "newPresets");
                File configDir = new File(customDir, "overrides/config"), defaultConfigDir = new File(customDir, "overrides/defaultconfigs");
                File configPresetFiles = new File(presetFiles, "config");

                File resourcePackDir = new File(customDir, "overrides/resourcepacks");
                File resourcePresetFiles = new File(presetFiles, "resourcepacks");

                System.out.println("Deleting all folders.");
                FileUtil.deleteFolder(configDir);
                FileUtil.deleteFolder(defaultConfigDir);
                FileUtil.deleteFolder(resourcePackDir);
                System.out.println("Deleted all folders.");

                FileUtil.replaceStringsInFiles(manifest, bmcVersion, ModPackInfo.currentCustomVersion);
                System.out.println("Replaced strings in manifest.json");

                FileUtil.loadPresets(configPresetFiles, configDir);
                System.out.println("Loaded config presets");

                FileUtil.loadPresets(configPresetFiles, defaultConfigDir);
                System.out.println("Loaded default config presets");

                FileUtil.loadPresets(resourcePresetFiles, resourcePackDir);
                System.out.println("Loaded resource pack presets");

                System.out.println("Finished updating the modpack");

                System.out.println("Starting to zip the modpack");
                new ZipFiles().zipDirectory(customDir, endZip.toFile());
                System.out.println("Finished zipping the modpack");
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Files.delete(Path.of("ModPacks", "LatestBMC.zip"));
                    FileUtil.deleteFolder(Path.of("ModPacks", "Custom").toFile());
                    Files.delete(Path.of("ModPacks", "Custom.zip"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void detoxModList(File file) throws IOException, CurseForgeException {
        if (file.isFile() && file.getName().equalsIgnoreCase("manifest.json")) {
            JsonObject jsonObject = JsonParser.parseString(Files.readString(file.toPath())).getAsJsonObject();

            if (jsonObject.has("files")) {
                JsonArray jsonArray = jsonObject.getAsJsonArray("files");
                JsonArray newJsonArray = new JsonArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject object = jsonArray.get(i).getAsJsonObject();
                    int projectId = object.get("projectID").getAsInt();

                    if (ModPackInfo.removeMods.contains(projectId)) continue;

                    if (currentMods.contains(projectId)) continue;

                    int fileId = object.get("fileID").getAsInt();

                    io.github.matyrobbrt.curseforgeapi.schemas.file.File modFile =
                            Main.cfApi.getHelper().getModFile(projectId, fileId).orElse(null);

                    if (modFile != null && modFile.isAvailable()) {
                        newJsonArray.add(object);
                        currentMods.add(projectId);
                    } else  {
                        System.out.println("Mod file for " + projectId + " is not available, trying to use the latest.");
                        Main.cfApi.getHelper().getModFiles(projectId).ifPresent(mf -> {
                            List<io.github.matyrobbrt.curseforgeapi.schemas.file.File> files =
                                    mf.stream().filter(file1 -> !file1.isServerPack() &&
                                            file1.gameVersions().contains("1.19.2") && file1.gameVersions().contains("Forge")).toList();

                            if (files.isEmpty()) {
                                System.out.println("Couldn't find a file for mod " + projectId);
                                return;
                            }

                            object.addProperty("fileID", files.get(0).id());

                            newJsonArray.add(object);
                            currentMods.add(projectId);
                            System.out.println("Updated mod " + projectId);
                        });
                    }
                }

                for (int missingMod : ModPackInfo.missingMods.stream().filter(missingMod -> !currentMods.contains(missingMod)).toList()) {
                    JsonObject object = new JsonObject();

                    System.out.println("Trying to add missing mod " + missingMod);

                    Main.cfApi.getHelper().getMod(missingMod).ifPresent(c -> {
                        object.addProperty("projectID", missingMod);

                        try {
                            Main.cfApi.getHelper().getModFiles(c).ifPresent(mf -> {
                                List<io.github.matyrobbrt.curseforgeapi.schemas.file.File> files =
                                        mf.stream().filter(file1 -> !file1.isServerPack() &&
                                                file1.gameVersions().contains("1.19.2") && file1.gameVersions().contains("Forge")).toList();

                                if (files.isEmpty()) {
                                    System.out.println("Couldn't find a file for mod " + c.name() + "(" + missingMod + ")");
                                    return;
                                }

                                object.addProperty("fileID", files.get(0).id());

                                newJsonArray.add(object);
                                currentMods.add(missingMod);

                                System.out.println("Added missing mod " + missingMod + " (" + c.name() + ")");
                            });
                        } catch (CurseForgeException e) {
                            e.printStackTrace();
                            System.out.println("Couldn't find a file for mod " + c.name() + "(" + missingMod + ")");
                        }
                    });
                }

                jsonObject.remove("files");
                jsonObject.add("files", newJsonArray);
            }
            Files.writeString(file.toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject), StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

}
