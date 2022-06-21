import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        File folder = new File(args[0]);

        if (folder.isDirectory()) {
            checkFile(folder);
        }
    }

    public static void checkFile(File folder) {
        if (folder == null || folder.listFiles() == null) return;
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                checkFile(file);
                continue;
            }

            try {
                BufferedImage bufferedImage = ImageIO.read(file);
                if (bufferedImage == null) {
                    file.delete();
                    System.out.println("Deleted " + file.getName() + ", cause null");
                    continue;
                }

                if (bufferedImage.getWidth() < 256 || bufferedImage.getHeight() < 256) {
                    file.delete();
                    System.out.println("Deleted " + file.getName() + " Width: " + bufferedImage.getWidth() + ", Height: " + bufferedImage.getHeight());
                } else {
                    Raster ras = bufferedImage.getRaster();

                    if (ras.getNumDataElements() != 3) {
                        file.delete();
                        System.out.println("Deleted " + file.getName() + " cause its not a 3 channel image instead its a " + ras.getNumDataElements() + " channel image");
                    }
                }
            } catch (Exception exception) {
                file.delete();
                System.out.println("Deleted " + file.getName());
                exception.printStackTrace();
            }
        }
    }

}