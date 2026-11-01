package ru.turik.hud;

import ru.turik.Turik;
import ru.turik.font.FontType;
import ru.turik.render.Render2D;

/** Coordinates + player info plate (health, hunger, biome). */
public class PlayerInfoElement extends HudElement {
    public PlayerInfoElement() {
        super(8, 40);
    }

    @Override
    public boolean visible() {
        return Turik.get().mc().player != null;
    }

    @Override
    public void render() {
        var mc = Turik.get().mc();
        float size = 9;
        String coords = String.format("XYZ %.1f / %.1f / %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
        var biome = mc.world.getBiome(mc.player.getBlockPos()).getKey();
        String info = String.format("HP %.0f · Food %d · %s",
                mc.player.getHealth() + mc.player.getAbsorptionAmount(),
                mc.player.getHungerManager().getFoodLevel(),
                biome.map(b -> b.getValue().getPath()).orElse("n/a"));

        width = Math.max(Render2D.textWidth(coords, size, FontType.MEDIUM),
                Render2D.textWidth(info, size, FontType.MEDIUM)) + 14;
        height = Render2D.textHeight(size) * 2 + 8;
        plate(0);
        Render2D.text(coords, x + 7, y + 4, size, FontType.MEDIUM, 0xFFE8F6FF);
        Render2D.text(info, x + 7, y + Render2D.textHeight(size) + 5, size, FontType.MEDIUM, 0xFF9FC8E8);
    }
}