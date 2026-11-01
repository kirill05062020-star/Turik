package ru.turik.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.util.InventoryUtils;
import ru.turik.util.MouseUtil;

/**
 * 6. AutoWeapon — switches to the best weapon (configurable priority)
 * when an enemy is under the crosshair.
 */
public class AutoWeapon extends Module {
    private final ModeSetting priority = new ModeSetting("Priority", "Auto", "Sword", "Axe");
    private final NumberSetting range = new NumberSetting("Range", 1, 6, 0.1, 4.0, "m");
    private final BooleanSetting onlyOnTarget = new BooleanSetting("Only with target", true);

    public AutoWeapon() {
        super("AutoWeapon", "Автовыбор оружия с настраиваемым приоритетом",
                Category.COMBAT);
        add(priority, range, onlyOnTarget);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        PlayerEntity target = MouseUtil.crosshairPlayer(mc, range.get());
        if (onlyOnTarget.get() && target == null) {
            return;
        }
        if (target != null && isFriend(target)) {
            return;
        }
        int best = -1;
        double bestScore = InventoryUtils.weaponScore(mc.player.getMainHandStack());
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            double score = InventoryUtils.weaponScore(stack);
            String itemPath = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).getPath();
            if (priority.is("Sword") && !itemPath.endsWith("_sword")) continue;
            if (priority.is("Axe") && !itemPath.endsWith("_axe")) continue;
            if (score > bestScore) {
                bestScore = score;
                best = slot;
            }
        }
        if (best >= 0 && best != InventoryUtils.selectedSlot(mc)) {
            InventoryUtils.selectSlot(mc, best);
        }
    }
}
