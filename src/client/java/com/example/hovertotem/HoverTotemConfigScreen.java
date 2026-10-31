package com.example.hovertotem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;

public class HoverTotemConfigScreen extends Screen {
    private final Screen parent;
    private final HoverTotemConfig cfg = HoverTotemConfig.get();

    private IntSlider delaySlider;
    private IntSlider randomMinSlider;
    private IntSlider randomMaxSlider;

    public HoverTotemConfigScreen(Screen parent) {
        super(Text.literal("Hover Totem"));
        this.parent = parent;
    }

    private Text enabledText() { return Text.literal("Mod: " + (cfg.enabled ? "ON" : "OFF")); }
    private Text modeText() { return Text.literal("Mode: " + cfg.mode.label); }
    private Text randomText() { return Text.literal("Random delay range: " + (cfg.randomDelay ? "ON" : "OFF")); }

    private void refreshActive() {
        delaySlider.active = !cfg.randomDelay;
        randomMinSlider.active = cfg.randomDelay;
        randomMaxSlider.active = cfg.randomDelay;
    }

    @Override
    protected void init() {
        int x = width / 2 - 100;
        int y = Math.max(30, height / 2 - 100);
        int step = 24;
        int row = 0;

        addDrawableChild(ButtonWidget.builder(enabledText(), b -> {
            cfg.enabled = !cfg.enabled;
            b.setMessage(enabledText());
        }).dimensions(x, y + step * row++, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(modeText(), b -> {
            cfg.mode = cfg.mode.next();
            b.setMessage(modeText());
        }).dimensions(x, y + step * row++, 200, 20).build());

        delaySlider = addDrawableChild(new IntSlider(x, y + step * row++, 200, 20, "Delay (ms)",
                HoverTotemConfig.MIN_DELAY, HoverTotemConfig.MAX_DELAY, cfg.delayMs, v -> cfg.delayMs = v));

        addDrawableChild(ButtonWidget.builder(randomText(), b -> {
            cfg.randomDelay = !cfg.randomDelay;
            b.setMessage(randomText());
            refreshActive();
        }).dimensions(x, y + step * row++, 200, 20).build());

        randomMinSlider = addDrawableChild(new IntSlider(x, y + step * row++, 200, 20, "Random min (ms)",
                HoverTotemConfig.MIN_DELAY, HoverTotemConfig.MAX_DELAY, cfg.randomMinMs, v -> cfg.randomMinMs = v));

        randomMaxSlider = addDrawableChild(new IntSlider(x, y + step * row++, 200, 20, "Random max (ms)",
                HoverTotemConfig.MIN_DELAY, HoverTotemConfig.MAX_DELAY, cfg.randomMaxMs, v -> cfg.randomMaxMs = v));

        addDrawableChild(new IntSlider(x, y + step * row++, 200, 20, "Hotbar fallback slot",
                1, 9, cfg.hotbarSlot, v -> cfg.hotbarSlot = v));

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(x, y + step * row + 8, 200, 20).build());

        refreshActive();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, Math.max(30, height / 2 - 100) - 16, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        HoverTotemConfig.save();
        client.setScreen(parent);
    }

    private static class IntSlider extends SliderWidget {
        private final String label;
        private final int min, max;
        private final IntConsumer onChange;

        IntSlider(int x, int y, int w, int h, String label, int min, int max, int current, IntConsumer onChange) {
            super(x, y, w, h, Text.empty(), (current - min) / (double) (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            updateMessage();
        }

        private int intValue() { return min + (int) Math.round(value * (max - min)); }

        @Override
        protected void updateMessage() {
            if (label == null) return; // called by super constructor before fields are set
            setMessage(Text.literal(label + ": " + intValue()));
        }

        @Override
        protected void applyValue() { onChange.accept(intValue()); }
    }
}
