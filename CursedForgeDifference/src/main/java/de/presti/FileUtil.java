package de.presti;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileUtil {
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
