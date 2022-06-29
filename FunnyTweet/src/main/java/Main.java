import twitter4j.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    static int day = 0;

    static Twitter twitter = new TwitterFactory().getInstance();

    static Thread sendingThread;

    static String[] messages = new String[]
            { "day %day% or so trying to reach @discord and give them my great Ideas!",
                "day %day% of wanting to pitch my Ideas to @discord !",
                "day %day% an I still want to reach @discord and give them my great Ideas!",
                "day %day% of trying to pitch my great Ideas to @discord !"};

    public static void main(String[] args) {
        Path of = Path.of("day.txt");
        try {
            if (Files.exists(of)) {
                String fileContent = Files.readString(of);

                fileContent = fileContent.trim()
                        .replaceAll("\\r", "")
                        .replaceAll("\\b", "")
                        .replaceAll("\\n", "");

                day = fileContent.matches("\\d+") ? Integer.parseInt(fileContent) : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendingThread = new Thread(() -> {
            while (Thread.currentThread().isAlive() && !Thread.currentThread().isInterrupted()) {
                tweet(messages[ThreadLocalRandom.current().nextInt(messages.length)]
                        .replaceAll("%day%", String.valueOf(++day)));
                try {
                    Files.write(of, Collections.singleton(String.valueOf(day)));
                    Thread.sleep(Duration.ofDays(1).toMillis());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        sendingThread.start();
    }

    private static void tweet(String text) {
        try {
            StatusUpdate statusUpdate = new StatusUpdate(text);
            Status status = twitter.updateStatus(statusUpdate);
            System.out.println("Successfully updated the status to [" + status.getText() + "].");
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to update the status: " + te.getMessage());
        }
    }

}
