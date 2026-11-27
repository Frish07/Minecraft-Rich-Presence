package dev.frish07.richpresence.state;

public record GameSession(Mode mode, String label, String image, String imageText, long startedAtMillis) {
    public enum Mode {
        MENU,
        SINGLEPLAYER,
        MULTIPLAYER
    }

    public static GameSession mainMenu(String label, String image, String imageText) {
        return new GameSession(Mode.MENU, label, image, imageText, 0);
    }

    public static GameSession singleplayer(String image, String imageText) {
        return new GameSession(Mode.SINGLEPLAYER, "Singleplayer", image, imageText,
                System.currentTimeMillis());
    }

    public static GameSession multiplayer(String serverName, String image, String imageText) {
        String label = serverName == null || serverName.isBlank() ? "Multiplayer" : serverName.trim();
        return new GameSession(Mode.MULTIPLAYER, label, image, imageText, System.currentTimeMillis());
    }

    public PresenceState toPresence(String minecraftVersion) {
        String details = "Minecraft Java " + minecraftVersion;
        String state = switch (mode) {
            case MENU -> label;
            case SINGLEPLAYER -> "Singleplayer";
            case MULTIPLAYER -> "Playing on " + label;
        };
        return new PresenceState(details, state, image, imageText, startedAtMillis);
    }
}
