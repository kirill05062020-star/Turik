package ru.turik.modules.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.SlotActionType;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 24. ItemScroller — moves items between container and inventory with the
 * mouse wheel (works in chests, barrels, shulkers, furnaces, hoppers,
 * crafting tables and any other HandledScreen).
 */
public class ItemScroller extends Module {
    private final NumberSetting perTick = new NumberSetting("Items per tick", 1, 9, 1, 3, "шт");
    private final BooleanSetting invertDirection = new BooleanSetting("Invert direction", false);
    private final BooleanSetting stopOnHover = new BooleanSetting("Pause on hover", true);

    public ItemScroller() {
        super("ItemScroller", "Перемещение предметов колёсиком мыши",
                Category.PLAYER);
        add(perTick, invertDirection, stopOnHover);
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.MOUSE_SCROLLED.register((client, screen, dx, dy) -> {
            if (screen instanceof HandledScreen<?> handled && isEnabled()) {
                onScroll(dy);
            }
        });
    }

    private double accumulatedScroll;

    private void onScroll(double dy) {
        accumulatedScroll += dy;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || !(mc.currentScreen instanceof HandledScreen<?> screen)) {
            return;
        }
        int steps = (int) Math.signum(accumulatedScroll);
        if (steps == 0) {
            return;
        }
        accumulatedScroll -= steps;
        if (invertDirection.get()) {
            steps = -steps;
        }
        var handler = screen.getScreenHandler();
        int playerStart = handler.slots.size() - 36;
        int moved = 0;
        for (int i = 0; i < playerStart && moved < perTick.getInt(); i++) {
            if (steps > 0) {
                // scroll up: pull container items into the inventory
                if (handler.getSlot(i).getStack().isEmpty()) {
                    continue;
                }
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                moved++;
            }
        }
        if (steps < 0) {
            // scroll down: push inventory items into the container
            for (int i = playerStart; i < handler.slots.size() && moved < perTick.getInt(); i++) {
                if (handler.getSlot(i).getStack().isEmpty()) {
                    continue;
                }
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                moved++;
            }
        }
    }

    public boolean pausesOnHover() {
        return stopOnHover.get();
    }
}
