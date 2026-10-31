package com.example.hovertotem;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Adds a Configure button for Hover Totem inside Mod Menu. */
public class HoverTotemModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new HoverTotemConfigScreen(parent);
    }
}
