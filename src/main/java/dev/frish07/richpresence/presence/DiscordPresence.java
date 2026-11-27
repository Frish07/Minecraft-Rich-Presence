package dev.frish07.richpresence.presence;

import dev.frish07.richpresence.config.ModConfig;
import dev.frish07.richpresence.state.GameSession;
import dev.frish07.richpresence.state.PresenceState;
import fun.crashsystem.jdrpc.DiscordIPC;
import fun.crashsystem.jdrpc.DiscordIPCConfig;
import fun.crashsystem.jdrpc.activity.Activity;
import fun.crashsystem.jdrpc.activity.ActivityType;
import fun.crashsystem.jdrpc.event.DiscordEventListener;
import net.minecraft.SharedConstants;
import org.slf4j.Logger;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class DiscordPresence implements AutoCloseable {
    private final ModConfig config;
    private final Logger logger;
    private final ScheduledThreadPoolExecutor executor;
    private final String minecraftVersion = SharedConstants.getCurrentVersion().name();
    private final Long applicationId;

    private DiscordIPC client;
    private volatile PresenceState latest;
    private volatile boolean closed;
    private boolean warnedAboutApplicationId;

    public DiscordPresence(ModConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
        Long parsedApplicationId = null;
        try {
            long parsed = Long.parseLong(config.applicationId());
            if (parsed > 0) parsedApplicationId = parsed;
        } catch (NumberFormatException ignored) { }
        this.applicationId = parsedApplicationId;
        if (applicationId == null && !config.applicationId().isBlank()) {
            logger.warn("Discord presence is disabled: applicationId must be a positive numeric ID.");
            warnedAboutApplicationId = true;
        }
        this.executor = new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread thread = new Thread(runnable, "Minecraft Rich Presence");
            thread.setDaemon(true);
            return thread;
        });
        this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.executor.setRemoveOnCancelPolicy(true);
    }

    public synchronized void update(GameSession session) {
        if (closed) return;
        latest = session.toPresence(minecraftVersion);
        schedulePublish();
    }

    public synchronized void clear() {
        if (closed) return;
        latest = null;
        schedulePublish();
    }

    private void schedulePublish() {
        if (applicationId == null) {
            if (!warnedAboutApplicationId) {
                logger.warn("Discord presence is disabled until applicationId is set in "
                        + "config/minecraft-rich-presence.json.");
                warnedAboutApplicationId = true;
            }
            return;
        }
        executor.execute(this::publishLatest);
    }

    private void publishLatest() {
        if (closed) return;
        try {
            ensureConnected();
            if (latest == null) {
                client.clearActivity();
            } else {
                client.setActivity(toActivity(latest));
            }
        } catch (Exception exception) {
            logger.debug("Discord RPC is unavailable; will retry shortly.", exception);
            if (client == null && !closed) {
                executor.schedule(this::publishLatest, 5, TimeUnit.SECONDS);
            }
        }
    }

    private void ensureConnected() throws Exception {
        if (client != null) return;
        DiscordIPC connection = DiscordIPC.create(DiscordIPCConfig.builder()
                .clientId(applicationId)
                .reconnect(true)
                .maxReconnectAttempts(0)
                .build());
        try {
            connection.addListener(new DiscordEventListener() {
                @Override
                public void onReady(fun.crashsystem.jdrpc.entity.User user) {
                    if (!closed) {
                        executor.execute(() -> {
                            if (client == connection && !closed) {
                                publishLatest();
                            }
                        });
                    }
                }
            });
            client = connection;
            connection.connect();
        } catch (Exception exception) {
            if (client == connection) client = null;
            connection.close();
            throw exception;
        }
    }

    private Activity toActivity(PresenceState state) {
        Activity.Builder builder = new Activity.Builder()
                .setType(ActivityType.PLAYING)
                .setDetails(limitText(state.details()))
                .setState(limitText(state.state()))
                .setLargeImage(state.image(), limitText(state.imageText()));
        if (state.startedAtMillis() > 0) {
            builder.setStartTimestamp(state.startedAtMillis());
        }
        return builder.build();
    }

    private static String limitText(String value) {
        String text = value == null ? "" : value;
        if (text.length() > 128) {
            text = text.substring(0, 128);
            if (Character.isHighSurrogate(text.charAt(127))) {
                text = text.substring(0, 127);
            }
        }
        return text.length() == 1 ? text + " " : text;
    }

    private void disconnect() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception exception) {
                logger.debug("Error while closing Discord RPC.", exception);
            }
            client = null;
        }
    }

    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        executor.execute(() -> {
            try {
                if (client != null) client.clearActivity();
            } catch (Exception exception) {
                logger.debug("Could not clear Discord activity during shutdown.", exception);
            } finally {
                disconnect();
                executor.shutdown();
            }
        });
        executor.shutdown();
    }
}
