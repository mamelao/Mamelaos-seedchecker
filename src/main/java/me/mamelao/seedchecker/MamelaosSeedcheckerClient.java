package me.mamelao.seedchecker;

import me.mamelao.seedchecker.event.KeyInputHandler;
import net.fabricmc.api.ClientModInitializer;

public class MamelaosSeedcheckerClient  implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyInputHandler.register();
    }
}