package me.mamelao.seedchecker;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.mojang.text2speech.Narrator.LOGGER;

public class WorldLoader {

    public static void loadNextWorld(
            MinecraftClient client,
            long seed
    ){
        quitWorld("key.mamelaos-seedchecker.load_next");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.error("Sleep interrupted", e);
        }
        ResourcePackManager resourcePackManager =
                new ResourcePackManager(
                        new VanillaDataPackProvider()
                );
        SaveLoading.ServerConfig serverConfig =
                createServerConfig(
                        resourcePackManager,
                        DataConfiguration.SAFE_MODE
                );
        CompletableFuture<GeneratorOptionsHolder> completableFuture = SaveLoading.load(serverConfig, context -> new SaveLoading.LoadContext<WorldCreationSettings>(new WorldCreationSettings(new WorldGenSettings(new GeneratorOptions(
                seed,
                true,
                false
        ), WorldPresets.createDemoOptions(context.worldGenRegistryManager())), context.dataConfiguration()), context.dimensionsRegistryManager()), (resourceManager, dataPackContents, combinedDynamicRegistries, generatorOptions) -> {
            resourceManager.close();
            return new GeneratorOptionsHolder(generatorOptions.worldGenSettings(), combinedDynamicRegistries, dataPackContents, generatorOptions.dataConfiguration());
        }, Util.getMainWorkerExecutor(), client);
        client.runTasks(completableFuture::isDone);

        GeneratorOptionsHolder generatorOptionsHolder =
                completableFuture.join();

        DimensionOptionsRegistryHolder.DimensionsConfig dimensionsConfig =
                generatorOptionsHolder.selectedDimensions()
                        .toConfig(
                                generatorOptionsHolder.dimensionOptionsRegistry()
                        );

        CombinedDynamicRegistries<ServerDynamicRegistryType>
                combinedDynamicRegistries =
                generatorOptionsHolder
                        .combinedDynamicRegistries()
                        .with(
                                ServerDynamicRegistryType.DIMENSIONS,
                                dimensionsConfig.toDynamicRegistryManager()
                        );
        Lifecycle lifecycle =
                FeatureFlags.isNotVanilla(
                        generatorOptionsHolder
                                .dataConfiguration()
                                .enabledFeatures()
                )
                        ? Lifecycle.experimental()
                        : Lifecycle.stable();

        Lifecycle lifecycle2 =
                combinedDynamicRegistries
                        .getCombinedRegistryManager()
                        .getRegistryLifecycle();

        Lifecycle lifecycle3 =
                lifecycle2.add(lifecycle);

        startServer(
                client,
                dimensionsConfig.specialWorldProperty(),
                combinedDynamicRegistries,
                lifecycle3,
                generatorOptionsHolder
        );
    }

    private static void startServer(
            MinecraftClient client,
            LevelProperties.SpecialProperty specialProperty,
            CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries,
            Lifecycle lifecycle,
            GeneratorOptionsHolder generatorOptionsHolder
    ) {
        client.setScreenAndRender(new MessageScreen(Text.translatable("createWorld.preparing")));
        Optional<LevelStorage.Session> optional = WorldLoader.createSession(client);
        if (optional.isEmpty()) {
            return;
        }
        boolean bl = specialProperty == LevelProperties.SpecialProperty.DEBUG;
        LevelInfo levelInfo = WorldLoader.createLevelInfo(bl);
        LevelProperties saveProperties = new LevelProperties(levelInfo, generatorOptionsHolder.generatorOptions(), specialProperty, lifecycle);
        client.createIntegratedServerLoader().start(optional.get(), generatorOptionsHolder.dataPackContents(), combinedDynamicRegistries, saveProperties);
    }

    private static LevelInfo createLevelInfo(
            boolean debugWorld
    ) {

        String worldName =
                "seed_" + System.currentTimeMillis();

        if (debugWorld) {

            GameRules gameRules = new GameRules();

            gameRules.get(GameRules.DO_DAYLIGHT_CYCLE)
                    .set(false, null);

            return new LevelInfo(
                    worldName,
                    GameMode.SPECTATOR,
                    false,
                    Difficulty.PEACEFUL,
                    true,
                    gameRules,
                    DataConfiguration.SAFE_MODE
            );
        }

        return new LevelInfo(
                worldName,
                GameMode.CREATIVE,
                false,
                Difficulty.NORMAL,
                true,
                new GameRules(),
                DataConfiguration.SAFE_MODE
        );
    }

    private static Optional<LevelStorage.Session> createSession(
            MinecraftClient client
    ) {
        String worldName =
                "seed_" + System.currentTimeMillis();
        try {
            return Optional.of(
                    client.getLevelStorage()
                            .createSession(worldName)
            );
        } catch (IOException e) {
            LOGGER.error(
                    "Failed to create session",
                    e
            );
            return Optional.empty();
        }
    }

    private static SaveLoading.ServerConfig createServerConfig(
            ResourcePackManager dataPackManager,
            DataConfiguration dataConfiguration
    ) {
        SaveLoading.DataPacks dataPacks =
                new SaveLoading.DataPacks(
                        dataPackManager,
                        dataConfiguration,
                        false,
                        true
                );

        return new SaveLoading.ServerConfig(
                dataPacks,
                CommandManager.RegistrationEnvironment.INTEGRATED,
                2
        );
    }

    record WorldCreationSettings(WorldGenSettings worldGenSettings, DataConfiguration dataConfiguration) {
    }

    public static void quitWorld(String text) {
        MinecraftClient client =
                MinecraftClient.getInstance();

        if (client.world != null) {

            client.world.disconnect();

            client.disconnect(
                    new MessageScreen(
                            Text.translatable(text)
                    )
            );
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(WorldLoader.class);

    public static synchronized long nextSeed() {
        Path path = Paths.get("config/seeds.txt");
        logger.info(
                "Looking for seeds file at (nextSeed): {}",
                path.toAbsolutePath()
        );

        try {
            // 1. Wczytaj wszystkie linie z pliku
            List<String> lines = Files.readAllLines(path);

            if (lines.isEmpty()) {
                logger.warn("Plik z seedami jest pusty! (nextSeed)");
                return 1;
            }

            // 2. Pobierz pierwszy seed
            String firstLine = lines.get(0).trim();
            long seed;
            try {
                seed = Long.parseLong(firstLine);
            } catch (NumberFormatException e) {
                seed = firstLine.hashCode();
            }

            // 3. Usuń wykorzystany seed z listy i zapisz resztę z powrotem do pliku
            List<String> remainingLines = new ArrayList<>(lines);
            remainingLines.remove(0);
            Files.write(path, remainingLines);

            logger.info("Pobrano seed: " + seed + ". Pozostało w pliku: " + remainingLines.size());
            return seed;

        } catch (IOException e) {
            logger.error("Błąd podczas operacji na pliku seeds.txt: (nextSeed)", e);
        } catch (NumberFormatException e) {
            logger.error("Błąd formatu liczby w pliku: ", e);

        }

        throw new RuntimeException(
                "Failed to load next seed"
        );
    }
}
