package dev.frish07.richpresence.state;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

public final class ServerIconResolver {
    private ServerIconResolver() {
    }

    public static String resolve(String serverAddress, boolean hasFavicon, Map<String, String> configuredUrls,
                                 String fallbackImage) {
        if (!hasFavicon || serverAddress == null || configuredUrls == null) {
            return fallbackImage;
        }

        String configuredUrl = configuredUrls.get(serverAddress.trim().toLowerCase());
        if (configuredUrl == null) {
            return fallbackImage;
        }

        return validHttpsUrl(configuredUrl).orElse(fallbackImage);
    }

    private static Optional<String> validHttpsUrl(String value) {
        try {
            URI uri = URI.create(value.trim());
            if ("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null
                    && uri.getUserInfo() == null) {
                return Optional.of(uri.toString());
            }
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
        return Optional.empty();
    }
}
