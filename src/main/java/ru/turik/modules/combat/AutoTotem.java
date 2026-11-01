package ru.turik.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.util.TickTimer;

/**
 * 4. AutoTotem — keeps a totem of undying in the off-hand as soon as the
 * current one pops or health drops under the threshold.
 */
public class AutoTotem extends Module {
    private final NumberSetting healthThreshold = new NumberSetting("Health threshold", 0, 40, 0.5, 12, "HP");
    private final BooleanSetting onlyLowHealth = new BooleanSetting("Only under threshold", false);
    private final BooleanSetting countTotems = new BooleanSetting("Check remaining totems", true);
    private final NumberSetting minTotems = new NumberSetting("Min totems", 0, 36, 1, 1, "шт");
    private final NumberSetting cooldown = new NumberSetting("Cooldown", 0, 1000, 50, 250, "ms");

    private final TickTimer timer = new TickTimer();

    public AutoTotem() {
        super("AutoTotem", "Автоматическая установка тотема в левую руку",
                Category.COMBAT);
        add(healthThreshold, onlyLowHealth, countTotems, minTotems, cooldown);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.currentScreen instanceof ru.turik.gui.ClickGuiScreen) {
            return;
        }
        ItemStack offhand = mc.player.getOffHandStack();
        boolean offhandTotem = offhand.getItem() == Items.TOTEM_OF_UNDYING;
        boolean low = !onlyLowHealth.get()
                || mc.player.getHealth() + mc.player.getAbsorptionAmount() <= healthThreshold.get();
        if (offhandTotem || !low) {
            return;
        }
        int totems = 0;
        int totemSlot = -1;
        for (int i = 9; i < 45; i++) {
            // Player screen handler: 9..44 covers main inventory + hotbar.
            if (i < mc.player.playerScreenHandler.slots.size()) {
                ItemStack s = mc.player.playerScreenHandler.getSlot(i).getStack();
                if (s.getItem() == Items.TOTEM_OF_UNDYING) {
                    totems++;
                    if (totemSlot < 0) totemSlot = i;
                }
            }
        }
        if (totemSlot < 0 || (countTotems.get() && totems < minTotems.getInt())) {
            return;
        }
        if (!timer.elapsed((long) cooldown.get())) {
            return;
        }
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, totemSlot,
                0, SlotActionType.QUICK_MOVE, mc.player);
    }
}
