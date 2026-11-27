package dev.frish07.richpresence;

import dev.frish07.richpresence.config.ModConfig;
import dev.frish07.richpresence.presence.DiscordPresence;
import dev.frish07.richpresence.state.GameSession;
import dev.frish07.richpresence.state.ServerIconResolver;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.multiplayer.ServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MinecraftRichPresence implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Minecraft Rich Presence");

    private DiscordPresence discord;
    private ModConfig config;

    @Override
    public void onInitializeClient() {
        config = ModConfig.load(LOGGER);
        discord = new DiscordPresence(config, LOGGER);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> showMainMenu());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ServerData server = handler.getServerData();
            if (server == null) {
                discord.update(GameSession.singleplayer(config.fallbackImage(), config.fallbackImageText()));
                return;
            }

            boolean hasFavicon = server.getIconBytes() != null;
            String image = ServerIconResolver.resolve(
                    server.ip,
                    hasFavicon,
                    config.serverIconUrls(),
                    config.fallbackImage()
            );
            discord.update(GameSession.multiplayer(server.name, image, config.fallbackImageText()));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> showMainMenu());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> discord.close());
    }

    private void showMainMenu() {
        if (config.showMainMenu()) {
            discord.update(GameSession.mainMenu(config.mainMenuLabel(), config.fallbackImage(),
                    config.fallbackImageText()));
        } else {
            discord.clear();
        }
    }
}
