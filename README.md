# Minecraft Rich Presence

Client-side Fabric mod that shows your Minecraft session in Discord Rich Presence.

## Compatibility

| Minecraft | Status | Java |
| --- | --- | --- |
| 26.3 | Configured target; build not verified in this environment | 25 |
| Other versions | Not yet supported | — |

Version compatibility is not claimed until that version has a successful build and runtime verification. Older versions require their own tested Minecraft/Fabric adapters and Java-compatible Discord RPC packaging.

## Features

- Shows the Minecraft version, singleplayer or multiplayer state, and elapsed session time.
- Updates on join, disconnect, startup, and shutdown events rather than polling every game tick.
- Runs Discord IPC on a dedicated daemon executor and retries after a Discord disconnect.
- Can show a configurable Main Menu status or clear presence while in the menu.
- Works without Discord; Minecraft continues normally while RPC retries in the background.

The mod needs a Discord desktop client and an Application ID, but no bot, user token, or OAuth login.

## Install and configure

1. Install Fabric Loader and Fabric API for Minecraft 26.3.
2. Put the release JAR in the `mods` folder and start Minecraft once.
3. Create a Discord application and copy its Application ID.
4. Open `config/minecraft-rich-presence.json`, set `applicationId`, and restart Minecraft.
5. Add an image asset named by `fallbackImage` to that Discord application (default key: `minecraft`).

The generated configuration also includes:

```json
{
  "applicationId": "",
  "fallbackImage": "minecraft",
  "fallbackImageText": "Minecraft",
  "showMainMenu": true,
  "mainMenuLabel": "Main Menu",
  "serverIconUrls": {}
}
```

`serverIconUrls` is an explicit opt-in map from a server address to a public HTTPS image URL. The address and configured URL are not sent to an image-hosting service by the mod. Discord's current Rich Presence API accepts external image URLs, but a Minecraft server favicon is provided to the game as image bytes, not as a public URL. The mod never uploads those bytes or sends private server addresses to third parties; unless you explicitly map an address to a public URL, it uses the configured Discord application asset as a fallback. Only HTTPS URLs without embedded credentials are accepted.

## Build and tests

GitHub Actions is configured to install the JDK and Gradle and build the declared matrix, which currently contains only 26.3. No local Java or Gradle installation is required to download published artifacts. A successful tagged `v*` build publishes only the production mod JAR as a GitHub Release asset.

For contributors, run `gradle clean build` with JDK 25 and Gradle 9.6. The test suite covers session-state formatting and the safe server-image fallback policy. Discord-client restart behavior and an in-game join/leave cycle still require runtime integration testing.

Before declaring a release compatible, manually verify multiplayer join/server switch, singleplayer join, returning to the main menu, Discord absent/restarted, servers without a favicon or with an invalid address, and Minecraft shutdown. These require a real game and Discord client and are not represented as automated unit tests.

## License

MIT
