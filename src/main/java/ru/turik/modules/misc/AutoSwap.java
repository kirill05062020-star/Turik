package ru.turik.modules.misc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.KeybindSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.module.setting.StringSetting;
import ru.turik.util.InventoryUtils;
import ru.turik.util.TickTimer;

/**
 * 47. AutoSwap — swaps between two configured items on a key or when
 * the active item runs out.
 */
public class AutoSwap extends Module {
    private final StringSetting itemA = new StringSetting("Item A", "minecraft:diamond_sword");
    private final StringSetting itemB = new StringSetting("Item B", "minecraft:diamond_axe");
    private final KeybindSetting swapKey = new KeybindSetting("Swap key", -1);
    private final ModeSetting condition = new ModeSetting("Condition", "Manual", "Auto empty");
    private final NumberSetting delay = new NumberSetting("Delay", 0, 1000, 50, 100, "ms");

    private final TickTimer timer = new TickTimer();

    public AutoSwap() {
        super("AutoSwap", "Автопереключение между выбранными предметами",
                Category.MISC);
        add(itemA, itemB, swapKey, condition, delay);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null) {
            return;
        }
        if (swapKey.get() > 0 && org.lwjgl.glfw.GLFW.glfwGetKey(
                mc.getWindow().getHandle(), swapKey.get()) == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
            if (timer.elapsed((long) delay.get())) {
                swap(mc);
            }
            return;
        }
        if (condition.is("Auto empty") && mc.player.getMainHandStack().getCount() == 0) {
            swap(mc);
        }
    }

    private void swap(MinecraftClient mc) {
        ItemStack held = mc.player.getMainHandStack();
        String heldId = held.isEmpty() ? "" : Registries.ITEM.getId(held.getItem()).getPath();
        String a = shortName(itemA.get());
        String b = shortName(itemB.get());

        String want;
        if (heldId.endsWith(a)) {
            want = b;
        } else if (heldId.endsWith(b)) {
            want = a;
        } else {
            want = a;
        }
        int target = -1;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack s = mc.player.getInventory().getStack(slot);
            if (!s.isEmpty() && Registries.ITEM.getId(s.getItem()).getPath().endsWith(want)) {
                target = slot;
                break;
            }
        }
        if (target >= 0 && target != InventoryUtils.selectedSlot(mc)) {
            InventoryUtils.selectSlot(mc, target);
        }
    }

    private String shortName(String entry) {
        return entry.trim().replace("minecraft:", "").toLowerCase();
    }
}