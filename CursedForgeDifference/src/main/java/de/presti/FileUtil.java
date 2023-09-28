package de.presti;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class FileUtil {


    public static void loadPresets(File presets, File directory) throws IOException {
        if (presets.isDirectory()) {
            new File(directory, presets.getName()).delete();
        }

        FileUtil.copyDirectory(presets, directory);
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
            readAll = readAll.replace("Better MC [FORGE]", "Pockets Dimensional Craft")
                    .replace(bmc, custom)
                    .replace("SHXRKIE", "Presti")
                    .replace("Better Minecraft", "Pockets Dimensional Craft")
                    .replace("BMC3", "PDC")
                    .replace("BMC", "PDC")
                    .replace("BetterMC", "PocketsDC")
                    .replace("Better MC", "Pockets Dimensional Craft")
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

    public static void copyDirectory(File baseDirectory, File destDirectory) throws IOException {
        if (baseDirectory.isDirectory()) {
            if (!destDirectory.exists()) {
                destDirectory.mkdirs();
            }

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
