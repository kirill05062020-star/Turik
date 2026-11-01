package ru.turik.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.AxeItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Item/inventory helpers: food detection, armor scoring, weapon scoring and
 * hotbar switching. Inventory manipulation always goes through the vanilla
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

    /** Heuristic armor score: protection value weighted by remaining durability. */
    public static double armorScore(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem armor)) {
            return -1;
        }
        float defense = armor.getMaterial().value().defense(EquipmentSlot.MAINHAND);
        double durability = 1.0 - (double) stack.getDamage() / Math.max(1, stack.getMaxDamage());
        return defense * 10 + durability * 5 + (stack.contains(DataComponentTypes.ENCHANTABLE) ? 1 : 0);
    }

    /** Weapon score by type/material: swords beat axes beat everything else. */
    public static double weaponScore(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof SwordItem sword) {
            return 100 + sword.getMaterial().value().getAttackDamage();
        }
        if (item instanceof AxeItem axe) {
            return 60 + axe.getMaterial().value().getAttackDamage();
        }
        if (item instanceof ToolItem) {
            return 10;
        }
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
        if (slot >= 0 && slot < 9) {
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
            double score = armorScore(mc.player.getInventory().getStack(slot));
            if (mc.player.getInventory().getStack(slot).getItem() instanceof ArmorItem armor
                    && armor.getSlotType() == type && score > bestScore) {
                bestScore = score;
                best = slot;
            }
        }
        return best;
    }

    public static final ToolMaterials DUMMY = null; // reference kept out of runtime paths
}
