package dev.frish07.richpresence.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("minecraft-rich-presence.json");

    private String applicationId = "";
    private String fallbackImage = "minecraft";
    private String fallbackImageText = "Minecraft";
    private boolean showMainMenu = true;
    private String mainMenuLabel = "Main Menu";
    private Map<String, String> serverIconUrls = new LinkedHashMap<>();

    public static ModConfig load(Logger logger) {
        if (!Files.exists(FILE)) {
            ModConfig defaults = new ModConfig();
            defaults.save(logger);
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(FILE)) {
            ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
            if (loaded == null) {
                throw new JsonParseException("Configuration file is empty");
            }
            loaded.normalize();
            return loaded;
        } catch (IOException | JsonParseException exception) {
            logger.error("Could not read {}. Using defaults; the file was left unchanged.", FILE, exception);
            return new ModConfig();
        }
    }

    private void save(Logger logger) {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException exception) {
            logger.error("Could not write default configuration to {}.", FILE, exception);
        }
    }

    private void normalize() {
        if (applicationId == null) applicationId = "";
        if (fallbackImage == null || fallbackImage.isBlank()) fallbackImage = "minecraft";
        if (fallbackImageText == null || fallbackImageText.isBlank()) fallbackImageText = "Minecraft";
        if (mainMenuLabel == null || mainMenuLabel.isBlank()) mainMenuLabel = "Main Menu";
        if (serverIconUrls == null) {
            serverIconUrls = new LinkedHashMap<>();
        } else {
            serverIconUrls.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null);
        }
    }

    public String applicationId() {
        return applicationId.trim();
    }

    public String fallbackImage() {
        return fallbackImage.trim();
    }

    public String fallbackImageText() {
        return fallbackImageText.trim();
    }

    public boolean showMainMenu() {
        return showMainMenu;
    }

    public String mainMenuLabel() {
        return mainMenuLabel.trim();
    }

    public Map<String, String> serverIconUrls() {
        return Map.copyOf(serverIconUrls);
    }
}
