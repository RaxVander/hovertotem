# Hover Totem (Fabric 1.21.11, client-side PvP mod)

Open your inventory and hover over a Totem of Undying. After the delay, it moves to your
offhand and/or hotbar. It never swaps a totem with another totem. Nothing to install on the server.

Settings: Mod Menu -> Hover Totem -> Configure (or edit .minecraft/config/hovertotem.json)
- Mod ON/OFF
- Mode: Offhand, Hotbar, or Offhand + Hotbar
- Delay: 1 - 100 ms (how long the cursor must rest on the totem)
- Random delay range: when ON, each hover picks a random delay between Random min and Random max (1 - 100 ms)
- Hotbar fallback slot: 1 - 9, used only when the hotbar has no empty slot

Build (JDK 21, Gradle 9.4+):
  gradle wrapper --gradle-version 9.4.1   (once, if there is no gradlew)
  ./gradlew build
Jar: build/libs/hovertotem-1.0.0.jar -> .minecraft/mods, with Fabric API (Mod Menu optional, needed for the config screen)
Test: ./gradlew runClient

If Gradle says a dependency version does not exist, update gradle.properties from https://fabricmc.net/develop
Works in the survival inventory screen. Some servers ban inventory-automation mods, so check the rules first.
