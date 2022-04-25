import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main extends JFrame {

    public static Thread randomThread;

    JLabel imageLabel = new JLabel("Loading");

    public Main() {
        imageLabel.setSize(500, 500);
        imageLabel.setLocation(0, 0);
        randomThread = new Thread(() -> {
            while (randomThread != null && !randomThread.isInterrupted()) {
                values.put(new Random().nextDouble(256262), System.currentTimeMillis());
                updateData();
                try {
                    Thread.sleep(new Random().nextLong(Duration.ofSeconds(2).toMillis()));
                } catch (Exception ignored) {
                }
            }
        });
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        setSize(500,500);
        setLayout(null);
        add(imageLabel);
        randomThread.start();
    }

    public static HashMap<Double, Long> values = new HashMap<>();

    public static void main(String[] args) {
        new Main();
    }

    public void updateData() {
        double highestValue = 0;

        for (Map.Entry<Double, Long> value : values.entrySet().stream().filter(values -> ((System.currentTimeMillis() - values.getValue()) <= Duration.ofSeconds(10).toMillis())).toList()) {
            if (highestValue < value.getKey()) highestValue = value.getKey();
        }

        BufferedImage base = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

        double xOffset = (double) base.getWidth() / values.entrySet().stream().filter(value -> ((System.currentTimeMillis() - value.getValue()) <= Duration.ofSeconds(10).toMillis())).count();
        double yOffset = (base.getHeight() - 10) / (highestValue == 0 ? 1 : highestValue);

        System.out.println(highestValue + " - " + xOffset + " - " + yOffset);

        Graphics2D graphics2D = base.createGraphics();

        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRect(0, 0, base.getWidth(), base.getHeight());

        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        double lastPositionX = 0, lastPositionY = 0;

        int currentIndex = 1;
        for (Map.Entry<Double, Long> value : values.entrySet().stream().filter(values -> ((System.currentTimeMillis() - values.getValue()) <= Duration.ofSeconds(10).toMillis())).toList()) {
            double positionX = xOffset * currentIndex, positionY = yOffset * value.getKey();

            /*if (positionX > base.getWidth()) positionX = base.getWidth();

            if (positionX < 0) positionX = 0;*/

            graphics2D.setColor(Color.RED);
            graphics2D.fill(new Ellipse2D.Double((int) positionX - 5, (int) positionY - 5, 10, 10));
            graphics2D.drawString((currentIndex + 1) + "", (int) positionX - 5, (int) positionY - 5);

            graphics2D.setColor(Color.BLACK);
            graphics2D.drawLine((int) lastPositionX, (int) lastPositionY, (int) positionX,(int) positionY);

            lastPositionX = positionX;
            lastPositionY = positionY;
            currentIndex++;
        }

        graphics2D.setColor(Color.RED);
        graphics2D.drawString("Highest Value: " + highestValue, 0, graphics2D.getFontMetrics().getHeight());
        graphics2D.drawString("Last Index: " + currentIndex, 0, (graphics2D.getFontMetrics().getHeight() * 2));

        graphics2D.dispose();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(base, "PNG", outputStream);
            imageLabel.setIcon(new ImageIcon(outputStream.toByteArray()));
            imageLabel.setSize(getSize());
        } catch (Exception ignored) {}
    }
}