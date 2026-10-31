package com.example.hovertotem;

import com.example.hovertotem.HoverTotemConfig.Mode;
import com.example.hovertotem.mixin.HandledScreenAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Hover Totem: hover the cursor over a Totem of Undying in your inventory and
 * it is moved to your offhand and/or hotbar after a configurable delay (1-100 ms).
 * Never swaps a totem with another totem.
 */
public class HoverTotemClient implements ClientModInitializer {
    private static final int OFFHAND_SLOT_ID = 45;   // PlayerScreenHandler offhand slot
    private static final int HOTBAR_FIRST_ID = 36;   // hotbar slots 36..44
    private static final int HOTBAR_LAST_ID = 44;
    private static final int OFFHAND_BUTTON = 40;    // SWAP click button for offhand

    private Slot hovered;
    private long hoverStart;
    private long lastAction;
    private int currentDelay;

    @Override
    public void onInitializeClient() {
        HoverTotemConfig.get();

        // Runs every frame while the inventory is open, so delays below 50 ms (one tick) work.
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof InventoryScreen inv) {
                hovered = null;
                ScreenEvents.afterRender(screen).register((s, ctx, mx, my, delta) -> handle(client, inv));
            }
        });
    }

    private static boolean isTotem(ItemStack stack) {
        return stack.isOf(Items.TOTEM_OF_UNDYING);
    }

    private static int rollDelay(HoverTotemConfig cfg) {
        if (!cfg.randomDelay) return cfg.delayMs;
        int lo = Math.min(cfg.randomMinMs, cfg.randomMaxMs);
        int hi = Math.max(cfg.randomMinMs, cfg.randomMaxMs);
        return ThreadLocalRandom.current().nextInt(lo, hi + 1);
    }

    private void handle(MinecraftClient client, InventoryScreen screen) {
        HoverTotemConfig cfg = HoverTotemConfig.get();
        if (!cfg.enabled || client.player == null || client.interactionManager == null) return;

        Slot slot = ((HandledScreenAccessor) screen).hovertotem$getFocusedSlot();
        if (slot == null || !slot.hasStack() || !isTotem(slot.getStack())) {
            hovered = null;
            return;
        }

        long now = System.currentTimeMillis();
        if (slot != hovered) {
            hovered = slot;
            hoverStart = now;
            currentDelay = rollDelay(cfg);
        }
        if (now - hoverStart < currentDelay) return;
        if (now - lastAction < currentDelay) return;

        if (moveTotem(client, screen, slot, cfg)) {
            lastAction = now;
            hovered = null;
        }
    }

    private boolean moveTotem(MinecraftClient client, InventoryScreen screen, Slot slot, HoverTotemConfig cfg) {
        PlayerEntity p = client.player;
        int syncId = screen.getScreenHandler().syncId;
        boolean useOffhand = cfg.mode == Mode.OFFHAND || cfg.mode == Mode.BOTH;
        boolean useHotbar = cfg.mode == Mode.HOTBAR || cfg.mode == Mode.BOTH;

        // 1) Offhand first. Skipped if the offhand already holds a totem (no totem-for-totem swap).
        if (useOffhand && slot.id != OFFHAND_SLOT_ID && !isTotem(p.getOffHandStack())) {
            client.interactionManager.clickSlot(syncId, slot.id, OFFHAND_BUTTON, SlotActionType.SWAP, p);
            return true;
        }

        // 2) Then hotbar. Skipped if a totem is already in the hotbar or the target slot holds a totem.
        boolean hoveredInHotbar = slot.id >= HOTBAR_FIRST_ID && slot.id <= HOTBAR_LAST_ID;
        if (useHotbar && !hoveredInHotbar && !hotbarHasTotem(p)) {
            int target = firstEmptyHotbar(p);
            if (target < 0) target = cfg.hotbarSlot - 1;
            if (isTotem(p.getInventory().getStack(target))) return false;
            client.interactionManager.clickSlot(syncId, slot.id, target, SlotActionType.SWAP, p);
            return true;
        }
        return false;
    }

    private static boolean hotbarHasTotem(PlayerEntity p) {
        for (int i = 0; i < 9; i++) {
            if (isTotem(p.getInventory().getStack(i))) return true;
        }
        return false;
    }

    private static int firstEmptyHotbar(PlayerEntity p) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getStack(i).isEmpty()) return i;
        }
        return -1;
    }
}
