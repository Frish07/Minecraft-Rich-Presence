# Minecraft Rich Presence — Copilot Instructions

## Project
Develop a lightweight Minecraft Java Edition mod
that automatically updates Discord Rich Presence.

## Compatibility
- Primary mod loader: Fabric.
- Target Minecraft 1.14 through the latest release.
- Support multiple Minecraft versions using separate builds.
- Keep version-specific code isolated.
- Prioritize backwards compatibility.

## Features
- Detect Minecraft version.
- Detect multiplayer server name.
- Display the server icon when available.
- Display "Singleplayer" in singleplayer worlds.
- Track session duration.
- Update Discord presence automatically.
- Reconnect when Discord restarts.

## Performance
- Minimize RAM and CPU usage.
- Avoid unnecessary background threads.
- Never block the Minecraft render thread.
- Avoid polling every game tick.
- Properly release resources.

## Development
- Use Java and Gradle.
- Prefer shared code across Minecraft versions.
- Follow official Fabric documentation.
- Use Discord RPC without user tokens.
- Never hardcode credentials.
- Handle errors without crashing Minecraft.

## CI/CD
- Build using GitHub Actions.
- Do not require local development tools.
- Build separate JARs for supported versions.
- Upload successful builds as artifacts.
- Fix compilation errors before reporting success.

## Quality
- Do not claim untested versions are supported.
- Write clean and maintainable code.
- Document compatibility limitations.
- Maintain English and Russian documentation.
