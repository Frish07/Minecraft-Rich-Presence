package dev.frish07.richpresence.state;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerIconResolverTest {
    private static final String FALLBACK = "minecraft";

    @Test
    void usesFallbackWhenServerHasNoFavicon() {
        assertEquals(FALLBACK, ServerIconResolver.resolve(
                "play.example.net", false, Map.of("play.example.net", "https://cdn.example.net/icon.png"), FALLBACK));
    }

    @Test
    void usesExplicitlyConfiguredHttpsIconForServerWithFavicon() {
        assertEquals("https://cdn.example.net/icon.png", ServerIconResolver.resolve(
                "PLAY.EXAMPLE.NET", true, Map.of("play.example.net", "https://cdn.example.net/icon.png"), FALLBACK));
    }

    @Test
    void usesFallbackForUnmappedServer() {
        assertEquals(FALLBACK, ServerIconResolver.resolve(
                "private.example", true, Map.of("play.example.net", "https://cdn.example.net/icon.png"), FALLBACK));
    }

    @Test
    void rejectsNonHttpsAndCredentialBearingUrls() {
        assertEquals(FALLBACK, ServerIconResolver.resolve(
                "play.example.net", true, Map.of("play.example.net", "http://cdn.example.net/icon.png"), FALLBACK));
        assertEquals(FALLBACK, ServerIconResolver.resolve(
                "play.example.net", true, Map.of("play.example.net", "https://user@cdn.example.net/icon.png"),
                FALLBACK));
    }
}
