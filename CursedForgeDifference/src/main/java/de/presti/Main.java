package de.presti;

import com.google.gson.*;
import io.github.matyrobbrt.curseforgeapi.CurseForgeAPI;
import io.github.matyrobbrt.curseforgeapi.util.CurseForgeException;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;


public class Main {

    public static CurseForgeAPI cfApi;

    public static void main(String[] args) throws IOException, CurseForgeException {

        /*String[] files = Arrays.stream(new File("J:\\curseforge\\minecraft\\Instances\\Pockets Dimensional Craft\\mods").listFiles()).map(File::getName).toArray(String[]::new);
        String[] serverFiles = Arrays.stream(new File("J:\\curseforge\\minecraft\\Instances\\Pockets Dimensional Craft (1)\\mods").listFiles()).map(File::getName).toArray(String[]::new);
        //String[] serverFiles = Arrays.stream(new File("J:\\curseforge\\Server\\mods").listFiles()).map(File::getName).toArray(String[]::new);

        for (String f : files) {
            if (Arrays.stream(serverFiles).noneMatch(x -> x.equalsIgnoreCase(f))) {
                System.out.println("Missing Mod found -> " + f);
            }
        }

        for (String f : serverFiles) {
            if (Arrays.stream(files).noneMatch(x -> x.equalsIgnoreCase(f))) {
                System.out.println("Additional Mod found -> " + f);
            }
        }*/

        try {
            cfApi = CurseForgeAPI.builder().apiKey("$2a$10$gzUPDxpHN0qcocF6aldjfuB5NgXOO2l8cAqi3.fNp1cXu4fS0wnIG").build();
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }

        /*Main.cfApi.getHelper().getMod(839945).ifPresent(c -> {
            try {
                Main.cfApi.getHelper().getModFiles(c).orElseGet(ArrayList::new).forEach(x -> {
                    System.out.println(x.displayName() + " - " + x.serverPackFileId());
                    try {
                        System.out.println(Main.cfApi.getHelper().getModFile(c.id(),x.serverPackFileId()).orElse(null).downloadUrl());
                    } catch (CurseForgeException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (CurseForgeException e) {
                throw new RuntimeException(e);
            }
        }); */

        ModpackUpdater.updateModpack();

        /*differJSONBased();
        System.out.println("--------------------------------------------------");*/
        ////differJSONBasedReverse();
        ////updateCustomFile();
    }

    public static void differHTMLBased() throws IOException {
        File originalFile = new File("original.html");
        String[] original = Arrays.stream(Files.readString(originalFile.toPath(), StandardCharsets.ISO_8859_1).split("\n")).map(x -> x.split("\">")[1].split("\\(by")[0]).toArray(String[]::new);

        String[] custom = Arrays.stream(Files.readString(new File("custom.html").toPath(), StandardCharsets.ISO_8859_1).split("\n")).map(x -> x.split("\">")[1].split("\\(by")[0]).toArray(String[]::new);

        Arrays.stream(custom).forEach(x -> {
            String trimmed = x.trim();
            if (Arrays.stream(original).noneMatch(c -> c.trim().equalsIgnoreCase(trimmed))) {
                System.out.println("Additional Mod found -> " + trimmed);
            }
        });

        Arrays.stream(original).forEach(x -> {
            String trimmed = x.trim();
            if (Arrays.stream(custom).noneMatch(c -> c.trim().equalsIgnoreCase(trimmed))) {
                System.out.println("Missing Mod found -> " + trimmed);
            }
        });
    }

    public static void differJSONBased() throws IOException, CurseForgeException {
        JsonElement originalElement = JsonParser.parseString(Files.readString(new File("original.json").toPath()));

        JsonArray originalFileArray = originalElement.getAsJsonObject().getAsJsonArray("files");

        JsonElement customElement = JsonParser.parseString(Files.readString(new File("custom.json").toPath()));

        JsonArray customFileArray = customElement.getAsJsonObject().getAsJsonArray("files");

        for (int i = 0; i < originalFileArray.size(); i++) {
            JsonObject originalFile = originalFileArray.get(i).getAsJsonObject();

            long currentProjectId = originalFile.getAsJsonPrimitive("projectID").getAsLong();
            long currentProjectVersion = originalFile.getAsJsonPrimitive("fileID").getAsLong();

            long updateToProjectVersion = 0;

            boolean found = false;

            for (int j = 0; j < customFileArray.size(); j++) {
                JsonObject customFile = customFileArray.get(j).getAsJsonObject();

                long projectId = customFile.getAsJsonPrimitive("projectID").getAsLong();
                long projectVersion = customFile.getAsJsonPrimitive("fileID").getAsLong();

                if (currentProjectId == projectId) {
                    if (currentProjectVersion != projectVersion) {
                        updateToProjectVersion = projectVersion;
                        customElement.getAsJsonObject().addProperty("fileId", updateToProjectVersion);
                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                File missingFile = new File("missing.txt");
                cfApi.getHelper().getMod((int) currentProjectId).ifPresentOrElse(x -> {
                    System.out.println("Missing Mod found -> " + x.name());
                    try {
                        Files.writeString(missingFile.toPath(), x.name() + "\n", StandardOpenOption.APPEND);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }, () -> System.out.println("Missing Mod found -> " + currentProjectId));
            } else {
                if (updateToProjectVersion != 0) {
                    long finalUpdateToProjectVersion = updateToProjectVersion;
                    cfApi.getHelper().getMod((int) currentProjectId).ifPresentOrElse(x -> System.out.println("Found Mod -> " + x.name() + ", newer version use -> " + finalUpdateToProjectVersion), () -> System.out.println("Found Mod -> " + currentProjectId + ", newer version use -> " + finalUpdateToProjectVersion));
                }
            }
        }
    }

    public static void differJSONBasedReverse() throws IOException, CurseForgeException {
        JsonElement originalElement = JsonParser.parseString(Files.readString(new File("original.json").toPath()));

        JsonArray originalFileArray = originalElement.getAsJsonObject().getAsJsonArray("files");

        JsonElement customElement = JsonParser.parseString(Files.readString(new File("custom.json").toPath()));

        JsonArray customFileArray = customElement.getAsJsonObject().getAsJsonArray("files");

        for (int i = 0; i < customFileArray.size(); i++) {
            JsonObject originalFile = customFileArray.get(i).getAsJsonObject();

            long currentProjectId = originalFile.getAsJsonPrimitive("projectID").getAsLong();
            long currentProjectVersion = originalFile.getAsJsonPrimitive("fileID").getAsLong();

            long updateToProjectVersion = 0;

            boolean found = false;

            for (int j = 0; j < originalFileArray.size(); j++) {
                JsonObject customFile = originalFileArray.get(j).getAsJsonObject();

                long projectId = customFile.getAsJsonPrimitive("projectID").getAsLong();
                long projectVersion = customFile.getAsJsonPrimitive("fileID").getAsLong();

                if (currentProjectId == projectId) {
                    if (currentProjectVersion != projectVersion) {
                        updateToProjectVersion = projectVersion;
                        customElement.getAsJsonObject().addProperty("fileId", updateToProjectVersion);
                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                File missingFile = new File("missingReverse.txt");
                cfApi.getHelper().getMod((int) currentProjectId).ifPresentOrElse(x -> {
                    String name = x.name();
                    System.out.println("Missing Mod found -> " + name);
                    try {
                        Files.writeString(missingFile.toPath(), name + " " + x.id() + " " + currentProjectVersion + "\n", StandardOpenOption.APPEND);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }, () -> System.out.println("Missing Mod found -> " + currentProjectId));
            } else {
                if (updateToProjectVersion != 0) {
                    long finalUpdateToProjectVersion = updateToProjectVersion;
                    cfApi.getHelper().getMod((int) currentProjectId).ifPresentOrElse(x -> System.out.println("Found Mod -> " + x.name() + ", newer version use -> " + finalUpdateToProjectVersion), () -> System.out.println("Found Mod -> " + currentProjectId + ", newer version use -> " + finalUpdateToProjectVersion));
                }
            }
        }
    }

    public static void updateCustomFile() throws IOException {
        JsonElement originalElement = JsonParser.parseString(Files.readString(new File("original.json").toPath()));

        JsonArray originalFileArray = originalElement.getAsJsonObject().getAsJsonArray("files");

        JsonElement customElement = JsonParser.parseString(Files.readString(new File("custom.json").toPath()));

        JsonArray customFileArray = customElement.getAsJsonObject().getAsJsonArray("files");

        JsonArray newCustomFileArray = new JsonArray();

        for (int i = 0; i < customFileArray.size(); i++) {
            JsonObject originalFile = customFileArray.get(i).getAsJsonObject();

            long currentProjectId = originalFile.getAsJsonPrimitive("projectID").getAsLong();
            long currentProjectVersion = originalFile.getAsJsonPrimitive("fileID").getAsLong();

            long updateToProjectVersion = 0;

            JsonObject customFile = null;

            boolean found = false;

            for (int j = 0; j < originalFileArray.size(); j++) {
                customFile = originalFileArray.get(j).getAsJsonObject();

                long projectId = customFile.getAsJsonPrimitive("projectID").getAsLong();
                long projectVersion = customFile.getAsJsonPrimitive("fileID").getAsLong();

                if (currentProjectId == projectId) {
                    if (currentProjectVersion != projectVersion) {
                        updateToProjectVersion = projectVersion;
                        customElement.getAsJsonObject().addProperty("fileId", updateToProjectVersion);
                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                newCustomFileArray.add(customFile);
                System.out.println("Missing Mod found -> " + currentProjectId);
            } else {
                newCustomFileArray.add(customFile);
                if (updateToProjectVersion != 0) {
                    System.out.println("Found Mod -> " + currentProjectId);
                    System.out.println("Update to -> " + updateToProjectVersion);
                }
            }
        }

        Files.writeString(new File("newCustom.json").toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(newCustomFileArray), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println(newCustomFileArray);
    }
}