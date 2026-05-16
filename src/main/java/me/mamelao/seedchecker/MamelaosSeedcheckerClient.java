package me.mamelao.seedchecker;

import me.mamelao.seedchecker.event.KeyInputHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MamelaosSeedcheckerClient  implements ClientModInitializer {
    private static final Logger logger = LoggerFactory.getLogger(MamelaosSeedcheckerClient.class);
    @Override
    public void onInitializeClient() {
        KeyInputHandler.register();
        File file1 = new File("config/mamelaos-seedchecker/commands.txt");
        try {
            if (file1.createNewFile()) {
                System.out.println("File created: " + file1.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            logger.error("An error occurred.", e);
        }
        try (FileWriter writer = new FileWriter("config/mamelaos-seedchecker/commands.txt")) {
            writer.write("tellraw @a \"Hello! go to .minecraft/config/mamelaos-seedchecker/commands.txt to get rid of/edit this\"\n");
            writer.write("tellraw @a \"If you are typing a command, you need to type it in without a slash\"\n");
            writer.write("tellraw @a [\"I also recommend using \",{\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://mcstacker.net\"},\"color\":\"yellow\",\"text\":\"mcstacker.net\"}]");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            logger.error("An error occurred.", e);
        }
        File file2 = new File("config/mamelaos-seedchecker/seeds.txt");
        try {
            if (file2.createNewFile()) {
                System.out.println("File created: " + file2.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            logger.error("An error occurred.", e);
        }
        ClientPlayConnectionEvents.JOIN.register(
                (handler, sender, client) -> {

                    new Thread(() -> {

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }

                        WorldLoader.runCommandsFromFile();

                    }).start();
                });
    }
}