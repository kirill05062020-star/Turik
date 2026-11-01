package ru.turik.modules.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.turik.mixin.MinecraftClientAccessor;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 25. FastUse — shortens the vanilla item-use cooldown.
 */
public class FastUse extends Module {
    private final ModeSetting filter = new ModeSetting("Items", "All", "Throwables", "Whitelist");
    private final NumberSetting cooldown = new NumberSetting("Cooldown", 0, 4, 1, 1, "т");
    private final BooleanSetting showFeedback = new BooleanSetting("Feedback", false);

    public FastUse() {
        super("FastUse", "Оптимизирует использование предметов",
                Category.PLAYER);
        add(filter, cooldown, showFeedback);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) {
            return;
        }
        ItemStack stack = mc.player.getMainHandStack();
        int current = ((MinecraftClientAccessor) mc).turik$getItemUseCooldown();
        if (stack.isEmpty() || current <= cooldown.getInt()) {
            return;
        }
        if (!applies(stack.getItem())) {
            return;
        }
        ((MinecraftClientAccessor) mc).setItemUseCooldown(cooldown.getInt());
        if (showFeedback.get() && current > 1) {
            Turik.get().notifications().info("FastUse", "Кулдаун " + current + " → " + cooldown.getInt());
        }
    }

    private boolean applies(Item item) {
        return switch (filter.get()) {
            case "Throwables" -> item instanceof EnderPearlItem || item == Items.SNOWBALL
                    || item == Items.EGG || item == Items.SPLASH_POTION;
            default -> true;
        };
    }
}
