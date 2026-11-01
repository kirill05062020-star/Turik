package ru.turik.modules.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.util.TickTimer;

/**
 * 22. ChestStealer — quickly pulls items out of any open container.
 */
public class ChestStealer extends Module {
    private final ModeSetting filter = new ModeSetting("Filter", "All", "Valuables", "Whitelist");
    private final NumberSetting delay = new NumberSetting("Delay", 0, 1000, 10, 60, "ms");
    private final BooleanSetting skipTools = new BooleanSetting("Skip weapons/armor", false);
    private final BooleanSetting autoClose = new BooleanSetting("Close when empty", true);

    private final TickTimer timer = new TickTimer();
    private final String[] whitelist = {"diamond", "netherite", "emerald", "gold", "iron", "ender_pearl", "totem"};

    public ChestStealer() {
        super("ChestStealer", "Быстрое перемещение предметов из сундука",
                Category.PLAYER);
        add(filter, delay, skipTools, autoClose);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || !(mc.currentScreen instanceof HandledScreen<?> screen)) {
            return;
        }
        if (!timer.elapsed((long) delay.get())) {
            return;
        }
        var handler = screen.getScreenHandler();
        // Container slots come first in a handled screen; player inventory is after.
        int playerStart = handler.slots.size() - 36;
        for (int i = 0; i < playerStart; i++) {
            Slot slot = handler.getSlot(i);
            ItemStack stack = slot.getStack();
            if (stack.isEmpty() || !wanted(stack)) {
                continue;
            }
            mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            return; // one stack per delay window
        }
        if (autoClose.get()) {
            mc.player.closeHandledScreen();
        }
    }

    private boolean wanted(ItemStack stack) {
        if (skipTools.get() && (stack.getItem() instanceof net.minecraft.item.ToolItem
                || stack.getItem() instanceof net.minecraft.item.ArmorItem)) {
            return false;
        }
        String id = ru.turik.util.InventoryUtils.idOf(stack).getPath();
        return switch (filter.get()) {
            case "Valuables" -> {
                for (String w : whitelist) {
                    if (id.contains(w)) {
                        yield true;
                    }
                }
                yield false;
            }
            case "Whitelist" -> {
                for (String w : whitelist) {
                    if (id.contains(w)) {
                        yield true;
                    }
                }
                yield false;
            }
            default -> true;
        };
    }
}
