package ru.turik.modules.movement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.KeybindSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.util.TickTimer;

/**
 * 12. ElytraSwap — hot-swap chestplate ⇄ elytra on a key.
 */
public class ElytraSwap extends Module {
    private final KeybindSetting swapKey = new KeybindSetting("Swap key", -1);
    private final ModeSetting direction = new ModeSetting("Prefer", "Elytra", "Chestplate");
    private final NumberSetting delay = new NumberSetting("Delay", 0, 1000, 50, 150, "ms");

    private final TickTimer timer = new TickTimer();

    public ElytraSwap() {
        super("ElytraSwap", "Быстрая смена элитры и нагрудника",
                Category.MOVEMENT);
        add(swapKey, direction, delay);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || swapKey.get() <= 0) {
            return;
        }
        boolean pressed = org.lwjgl.glfw.GLFW.glfwGetKey(
                mc.getWindow().getHandle(), swapKey.get()) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        if (!pressed || !timer.elapsed((long) delay.get())) {
            return;
        }
        ItemStack chest = mc.player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
        boolean wearingElytra = chest.getItem() instanceof ElytraItem;
        boolean wantElytra = direction.is("Elytra");
        if (wantElytra == wearingElytra) {
            return; // already correct
        }
        int found = -1;
        for (int i = 0; i < mc.player.playerScreenHandler.slots.size(); i++) {
            ItemStack s = mc.player.playerScreenHandler.getSlot(i).getStack();
            boolean isElytra = s.getItem() instanceof ElytraItem;
            boolean isPlate = s.getItem() instanceof net.minecraft.item.ArmorItem armor
                    && armor.getSlotType() == net.minecraft.entity.EquipmentSlot.CHEST;
            if ((wantElytra && isElytra) || (!wantElytra && isPlate)) {
                found = i;
                break;
            }
        }
        if (found >= 0) {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, found,
                    0, SlotActionType.QUICK_MOVE, mc.player);
        }
    }
}
