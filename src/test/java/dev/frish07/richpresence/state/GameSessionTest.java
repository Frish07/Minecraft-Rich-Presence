package dev.frish07.richpresence.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameSessionTest {
    @Test
    void createsPresenceForSingleplayer() {
        PresenceState presence = GameSession.singleplayer("minecraft", "Minecraft").toPresence("26.3");

        assertEquals("Minecraft Java 26.3", presence.details());
        assertEquals("Singleplayer", presence.state());
        assertEquals("minecraft", presence.image());
        org.junit.jupiter.api.Assertions.assertTrue(presence.startedAtMillis() > 0);
    }

    @Test
    void createsPresenceForMultiplayerWithoutExposingAddress() {
        PresenceState presence = GameSession.multiplayer("Example Server", "minecraft", "Minecraft")
                .toPresence("26.3");

        assertEquals("Playing on Example Server", presence.state());
        org.junit.jupiter.api.Assertions.assertFalse(presence.state().contains("192.168."));
    }

    @Test
    void createsMainMenuPresenceWithoutSessionTimer() {
        PresenceState presence = GameSession.mainMenu("Main Menu", "minecraft", "Minecraft")
                .toPresence("26.3");

        assertEquals("Main Menu", presence.state());
        assertEquals(0, presence.startedAtMillis());
    }
}
