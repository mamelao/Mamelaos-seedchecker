package me.mamelao.seedchecker.event;

import me.mamelao.seedchecker.MamelaosSeedcheckerClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String KEY_CATEGORY_SEEDCHECKER = "key.category.mamelaos-seedchecker.seedchecker";
    public static final String KEY_NEXT_SEED = "key.mamelaos-seedchecker.next_seed";

    public static KeyBinding nextSeedKey;

    public static void registerKeyBindings() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (nextSeedKey.wasPressed()) {
                // Load next world, not yet implemented
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Hello!, loading world not implemented yet :("), false);
                MamelaosSeedcheckerClient.quitWorld();
            }
        });
    }

    public static void register() {
        nextSeedKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_NEXT_SEED,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                KEY_CATEGORY_SEEDCHECKER
        ));

        registerKeyBindings();
    }

}
