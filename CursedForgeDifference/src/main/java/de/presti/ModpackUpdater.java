package de.presti;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.matyrobbrt.curseforgeapi.util.CurseForgeException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ModpackUpdater {

    static List<Integer> removeMods = List.of(585546, 847414, 825617);
    static List<String> deleteFiles = List.of("serverbrowser.json", "bhmenu-client.toml", "multi.txt", "server.txt", "lps.png", "studio.png", "modpack-update-checker-info.txt", "modpack-update-checker.txt");
    static List<Integer> missingMods = List.of(283644, 431725, 322896, 401955, 271740, 457570, 443915,
            318646, 351725, 282001, 416089, 316867, 289479, 501214, 654384, 666014, 258371, 549225, 825621, 836992,
            266707, 545686, 419699, 625321);
    static Map<Integer, Integer> forcedUpdateMods = Map.of(711216,4576641, 412082,4615838);

    public static void updateModpack() throws CurseForgeException {

        Main.cfApi.getHelper().getMod(550864).ifPresent(c -> {
            try {
                Path filePath = Path.of("ModPacks", "LatestBMC.zip");

                if (filePath.toFile().exists())
                    Files.delete(filePath);

                List<io.github.matyrobbrt.curseforgeapi.schemas.file.File> files = c.latestFiles();
                io.github.matyrobbrt.curseforgeapi.schemas.file.File file = files.get(files.size() - 1);

                System.out.println("Downloading " + file.fileName() + " from " + file.downloadUrl());

                file.download(filePath);

                String bmcVersion = file.displayName().replace("Better MC [FORGE] 1.19.2", "").trim(), customVersion = "v8";
                System.out.println("BMC Version: " + bmcVersion);
                System.out.println("Custom Version: " + customVersion);

                File customDir = new File("ModPacks", "Custom");

                if (customDir.exists() && customDir.isDirectory())
                    deleteFolder(customDir);

                unzip(filePath, customDir);
                System.out.println("Unzipped BMC");

                File manifest = new File(customDir, "manifest.json");

                new File(customDir, "modlist.html").delete();
                System.out.println("Deleted modlist.html");

                detoxModList(manifest);
                System.out.println("Detoxed the Modlist.");

                File configDir = new File(customDir, "overrides/config"), defaultConfigDir = new File(customDir, "overrides/defaultconfigs");

                replaceStringsInFiles(manifest, bmcVersion, customVersion);
                System.out.println("Replaced strings in manifest.json");

                detoxFiles(configDir);
                System.out.println("Detoxed the config files");

                detoxFiles(defaultConfigDir);
                System.out.println("Detoxed the default config files");

                replaceStringsInFiles(configDir, bmcVersion, customVersion);
                System.out.println("Replaced strings in config files");

                replaceStringsInFiles(defaultConfigDir, bmcVersion, customVersion);
                System.out.println("Replaced strings in default config files");

                File presetFiles = new File("ModPacks", "presets");
                File configPresetFiles = new File(presetFiles, "config");

                loadPresets(configPresetFiles, configDir);
                System.out.println("Loaded config presets");

                loadPresets(configPresetFiles, defaultConfigDir);
                System.out.println("Loaded default config presets");

                File resourcePackDir = new File(customDir, "overrides/resourcepacks");
                File resourcePresetFiles = new File(presetFiles, "resourcepacks");

                detoxFiles(resourcePackDir);
                System.out.println("Detoxed the resource packs");

                loadPresets(resourcePresetFiles, resourcePackDir);
                System.out.println("Loaded resource pack presets");

                System.out.println("Finished updating the modpack");
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Files.delete(Path.of("ModPacks", "LatestBMC.zip"));
                    deleteFolder(Path.of("ModPacks", "Custom").toFile());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void unzip(Path src, File destDir) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(src.toFile()));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public static void replaceStringsInFiles(File file, String bmc, String custom) throws IOException {
        String name = file.getName();

        if (name.endsWith(".zip") || name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".db")) {
            System.out.println("Skipping file " + name);
            return;
        }

        if (file.isFile()) {
            System.out.println("Replacing strings in file " + name);
            String readAll = Files.readString(file.toPath());
            readAll = readAll.replace("Better MC [FORGE] 1.19.2", "Pockets Dimensional Craft")
                    .replace("Better Minecraft [FORGE] 1.19.2", "Pockets Dimensional Craft [Forge] 1.19.2")
                    .replace(bmc, custom)
                    .replace("BMC", "PDC")
                    .replace("Better Minecraft", "Pockets Dimensional Craft")
                    .replace("BetterMC", "PocketsDC")
                    .replace("574291", "839945")
                    .replace("1010281625583628319", "1129972955322007646");
            Files.writeString(file.toPath(), readAll, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            System.out.println("Replacing strings in directory " + name);
            File[] files = file.listFiles();
            if (files == null) return;
            for (File f : files) {
                replaceStringsInFiles(f, bmc, custom);
            }
        }
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
                    if (forcedUpdateMods.containsKey(projectId)) {
                        object.addProperty("fileID", forcedUpdateMods.get(projectId));
                        System.out.println("Forced update mod " + projectId + " to file " + forcedUpdateMods.get(projectId));
                    }

                    if (!removeMods.contains(projectId)) {
                        newJsonArray.add(object);
                    }
                }

                for (int missingMod : missingMods) {
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

                                System.out.println("Added missing mod " + missingMod);
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

    public static void detoxFiles(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                detoxFiles(f);
            }
        } else {
            if (deleteFiles.contains(file.getName())) {
                file.delete();
            }
        }
    }

    public static void loadPresets(File presets, File directory) throws IOException {
        if (presets.isDirectory()) {
            new File(directory, presets.getName()).delete();
        }

        copyDirectory(presets, directory);
    }

    public static void copyDirectory(File baseDirectory, File destDirectory) throws IOException {
        if (baseDirectory.isDirectory()) {
            for (File file : baseDirectory.listFiles()) {
                String toPath = file.toPath().toString().replace(baseDirectory.toPath().toString(), "");

                copyDirectory(file, new File(destDirectory, toPath));
            }
        } else {
            copyFile(baseDirectory, destDirectory);
        }
    }

    public static void copyFile(File baseFile, File destFile) throws IOException {
        Files.copy(baseFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void deleteFolder(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteFolder(f);
            }
        }

        file.delete();
    }
}
