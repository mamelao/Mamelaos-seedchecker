package me.mamelao.seedchecker;

import me.mamelao.seedchecker.event.KeyInputHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;

public class MamelaosSeedcheckerClient  implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyInputHandler.register();
    }

    public static void quitWorld() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            System.out.println(client.world);
            client.world.disconnect();
            client.disconnect();
            client.setScreen(new TitleScreen());
        }
    }
}
