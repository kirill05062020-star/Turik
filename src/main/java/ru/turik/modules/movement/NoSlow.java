package ru.turik.modules.movement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;

/**
 * 10. NoSlow — removes using-item slowdown per item class.
 */
public class NoSlow extends Module {
    private final BooleanSetting shield = new BooleanSetting("Shield", true);
    private final BooleanSetting bow = new BooleanSetting("Bow / Crossbow", true);
    private final BooleanSetting food = new BooleanSetting("Food", true);
    private final BooleanSetting other = new BooleanSetting("Other items", false);

    public NoSlow() {
        super("NoSlow", "Убирает замедление при использовании предметов",
                Category.MOVEMENT);
        add(shield, bow, food, other);
    }

    /** Called from the LivingEntity mixin. */
    public boolean shouldHideUsing(LivingEntity entity) {
        if (!isEnabled()) {
            return false;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (entity != mc.player || mc.player == null) {
            return false;
        }
        if (!mc.player.isUsingItem()) {
            return false;
        }
        Item item = mc.player.getActiveItem().getItem();
        if (item == Items.SHIELD) return shield.get();
        if (item == Items.BOW || item == Items.CROSSBOW) return bow.get();
        if (item.getComponents().get(net.minecraft.component.DataComponentTypes.FOOD) != null) return food.get();
        return other.get();
    }
}
