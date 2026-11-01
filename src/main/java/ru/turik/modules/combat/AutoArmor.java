package ru.turik.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.util.InventoryUtils;
import ru.turik.util.TickTimer;

/**
 * 5. AutoArmor — equips the best armor piece per slot by comparing
 * protection + durability; used pieces are never swapped down.
 */
public class AutoArmor extends Module {
    private final NumberSetting delay = new NumberSetting("Delay", 0, 1000, 50, 300, "ms");
    private final BooleanSetting respectDurability = new BooleanSetting("Prefer durability", true);
    private final BooleanSetting inInventoryToo = new BooleanSetting("Work with any screen", true);

    private final TickTimer timer = new TickTimer();

    public AutoArmor() {
        super("AutoArmor", "Автоматическая экипировка лучшей брони",
                Category.COMBAT);
        add(delay, respectDurability, inInventoryToo);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) {
            return;
        }
        if (!inInventoryToo.get() && mc.currentScreen == null) {
            // default: only acts inside the inventory screen
            return;
        }
        if (!timer.elapsed((long) delay.get())) {
            return;
        }
        EquipmentSlot[] order = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slotType : order) {
            double bestScore = InventoryUtils.armorScore(mc.player.getEquippedStack(slotType));
            int bestContainerSlot = -1;
            for (int i = 0; i < mc.player.playerScreenHandler.slots.size(); i++) {
                ItemStack stack = mc.player.playerScreenHandler.getSlot(i).getStack();
                if (!InventoryUtils.isArmorForSlot(stack, slotType)) {
                    continue;
                }
                double score = InventoryUtils.armorScore(stack);
                if (respectDurability.get()) {
                    score += stack.getDamage() < stack.getMaxDamage() / 3.0 ? 1 : 0;
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestContainerSlot = i;
                }
            }
            if (bestContainerSlot >= 0) {
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                        bestContainerSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                return; // one piece per delay window
            }
        }
    }
}
