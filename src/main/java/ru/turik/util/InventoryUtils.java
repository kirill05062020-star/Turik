package ru.turik.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Item/inventory helpers: food detection, armor scoring, weapon scoring and
 * hotbar switching. Inventory manipulation always goes through the client
 * interaction manager so actions stay server-visible.
 */
public final class InventoryUtils {
    private InventoryUtils() {
    }

    public static boolean isFood(ItemStack stack) {
        return stack.contains(DataComponentTypes.FOOD);
    }

    public static FoodComponent food(ItemStack stack) {
        return stack.get(DataComponentTypes.FOOD);
    }

    private static String path(ItemStack stack) {
        return Registries.ITEM.getId(stack.getItem()).getPath();
    }

    private static EquipmentSlot armorSlot(ItemStack stack) {
        String path = path(stack);
        if (path.endsWith("_helmet")) return EquipmentSlot.HEAD;
        if (path.endsWith("_chestplate")) return EquipmentSlot.CHEST;
        if (path.endsWith("_leggings")) return EquipmentSlot.LEGS;
        if (path.endsWith("_boots")) return EquipmentSlot.FEET;
        return null;
    }

    public static boolean isArmorForSlot(ItemStack stack, EquipmentSlot slot) {
        return armorSlot(stack) == slot;
    }

    private static int materialDefense(String itemPath) {
        if (itemPath.startsWith("netherite_")) return 4;
        if (itemPath.startsWith("diamond_")) return 3;
        if (itemPath.startsWith("iron_") || itemPath.startsWith("chainmail_")) return 2;
        if (itemPath.startsWith("golden_") || itemPath.startsWith("turtle_")) return 1;
        if (itemPath.startsWith("leather_")) return 1;
        return 0;
    }

    /** Heuristic armor score: material protection weighted by remaining durability. */
    public static double armorScore(ItemStack stack) {
        EquipmentSlot slot = armorSlot(stack);
        if (slot == null) return -1;
        double durability = stack.isDamageable()
                ? 1.0 - (double) stack.getDamage() / Math.max(1, stack.getMaxDamage()) : 1.0;
        return materialDefense(path(stack)) * 10 + durability * 5
                + (stack.contains(DataComponentTypes.ENCHANTABLE) ? 1 : 0);
    }

    /** Weapon score by registry id: swords beat axes, followed by other tools. */
    public static double weaponScore(ItemStack stack) {
        String path = path(stack);
        if (path.endsWith("_sword")) return 100 + materialRank(path);
        if (path.endsWith("_axe")) return 60 + materialRank(path);
        if (path.endsWith("_pickaxe") || path.endsWith("_shovel") || path.endsWith("_hoe")) return 10;
        if (path.equals("trident") || path.equals("mace")) return 80;
        return 0;
    }

    private static int materialRank(String path) {
        if (path.startsWith("netherite_")) return 6;
        if (path.startsWith("diamond_")) return 5;
        if (path.startsWith("iron_") || path.startsWith("copper_")) return 4;
        if (path.startsWith("stone_")) return 3;
        if (path.startsWith("golden_")) return 2;
        if (path.startsWith("wooden_")) return 1;
        return 0;
    }

    public static boolean isWeapon(ItemStack stack) {
        return weaponScore(stack) > 0;
    }

    public static Identifier idOf(ItemStack stack) {
        return Registries.ITEM.getId(stack.getItem());
    }

    /** Switches the hotbar slot through the client only (visible to the server). */
    public static void selectSlot(MinecraftClient mc, int slot) {
        if (slot >= 0 && slot < 9 && mc.player != null) {
            mc.player.getInventory().setSelectedSlot(slot);
        }
    }

    public static int selectedSlot(MinecraftClient mc) {
        return mc.player.getInventory().getSelectedSlot();
    }

    /** Finds the hotbar slot with the best armor of the given type, or -1. */
    public static int bestHotbarArmor(MinecraftClient mc, EquipmentSlot type) {
        int best = -1;
        double bestScore = armorScore(mc.player.getEquippedStack(type));
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            double score = armorScore(stack);
            if (armorSlot(stack) == type && score > bestScore) {
                bestScore = score;
                best = slot;
            }
        }
        return best;
    }
}
