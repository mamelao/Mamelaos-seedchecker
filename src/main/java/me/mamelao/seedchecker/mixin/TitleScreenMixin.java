package me.mamelao.seedchecker.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalLong;

import static me.mamelao.seedchecker.WorldLoader.loadNextWorld;
import static me.mamelao.seedchecker.WorldLoader.nextSeed;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "initWidgetsNormal")
    private void addSeedButton(int y, int spacingY, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("key.mamelaos-seedchecker.next_seed"), button -> {
            OptionalLong optionalSeed = nextSeed();
            if (optionalSeed.isPresent()) {
                loadNextWorld(client, optionalSeed.getAsLong());
            } else {
                button.setMessage(Text.translatable("mamelaos-seedchecker.no_seeds"));
            }

        }).dimensions(this.width / 2 + 110, y, 100, 20).build());
    }
}