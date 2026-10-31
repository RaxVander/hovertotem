package com.example.hovertotem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Saved to .minecraft/config/hovertotem.json */
public class HoverTotemConfig {
    public enum Mode {
        OFFHAND("Offhand"), HOTBAR("Hotbar"), BOTH("Offhand + Hotbar");

        public final String label;
        Mode(String label) { this.label = label; }
        public Mode next() { return values()[(ordinal() + 1) % values().length]; }
    }

    public static final int MIN_DELAY = 1;
    public static final int MAX_DELAY = 100;

    public boolean enabled = true;
    public int delayMs = 10;            // fixed delay, 1 - 100
    public boolean randomDelay = false; // pick a random delay each time instead
    public int randomMinMs = 5;         // random range, 1 - 100
    public int randomMaxMs = 20;
    public Mode mode = Mode.OFFHAND;    // OFFHAND, HOTBAR or BOTH
    public int hotbarSlot = 9;          // 1 - 9, used when the hotbar has no empty slot

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("hovertotem.json");
    private static HoverTotemConfig instance;

    public static HoverTotemConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    private static HoverTotemConfig load() {
        HoverTotemConfig cfg = null;
        try {
            if (Files.exists(PATH)) cfg = GSON.fromJson(Files.readString(PATH), HoverTotemConfig.class);
        } catch (Exception ignored) { }
        if (cfg == null) cfg = new HoverTotemConfig();
        cfg.clamp();
        return cfg;
    }

    private static int clampDelay(int v) { return Math.max(MIN_DELAY, Math.min(MAX_DELAY, v)); }

    public void clamp() {
        delayMs = clampDelay(delayMs);
        randomMinMs = clampDelay(randomMinMs);
        randomMaxMs = clampDelay(randomMaxMs);
        hotbarSlot = Math.max(1, Math.min(9, hotbarSlot));
        if (mode == null) mode = Mode.OFFHAND;
    }

    public static void save() {
        HoverTotemConfig cfg = get();
        cfg.clamp();
        try {
            Files.writeString(PATH, GSON.toJson(cfg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
