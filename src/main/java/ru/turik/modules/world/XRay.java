package ru.turik.modules.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ColorSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.module.setting.StringSetting;
import ru.turik.render.Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * 42. XRay — highlights target ores/blocks through terrain (own world
 * shader with depth test disabled) and keeps vanilla brightness up.
 */
public class XRay extends Module {
    private final StringSetting blocks = new StringSetting("Blocks",
            "minecraft:diamond_ore, minecraft:emerald_ore, minecraft:gold_ore, minecraft:iron_ore, minecraft:ancient_debris");
    private final ColorSetting color = new ColorSetting("Color", 0xC82BD9FF);
    private final NumberSetting opacity = new NumberSetting("Opacity", 10, 100, 5, 70, "%");
    private final NumberSetting radius = new NumberSetting("Radius", 4, 32, 1, 16, "m");
    private final NumberSetting refresh = new NumberSetting("Refresh", 1, 20, 1, 5, "т");
    private final BooleanSetting fullBrightAssist = new BooleanSetting("FullBright assist", true);

    private List<BlockPos> found = new ArrayList<>();
    private int tickCounter;

    public XRay() {
        super("XRay", "Выделение выбранных блоков через стены",
                Category.WORLD);
        add(blocks, color, opacity, radius, refresh, fullBrightAssist);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) {
            return;
        }
        if (fullBrightAssist.get() && mc.options.getGamma().getValue() < 4) {
            mc.options.getGamma().setValue(8.0);
        }
        if (++tickCounter < refresh.getInt()) {
            return;
        }
        tickCounter = 0;
        found = new ArrayList<>();
        int r = radius.getInt();
        BlockPos center = BlockPos.ofFloored(mc.player.getPos());
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            var state = mc.world.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            String id = Registries.BLOCK.getId(state.getBlock()).getPath();
            for (String entry : blocks.list()) {
                String name = entry.replace("minecraft:", "").trim().toLowerCase();
                if (id.contains(name)) {
                    found.add(pos.toImmutable());
                    break;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = Turik.get().mc();
        if (fullBrightAssist.get() && mc.player != null) {
            mc.options.getGamma().setValue(1.0);
        }
    }

    @Override
    public void onWorldRender(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext ctx) {
        if (found.isEmpty()) {
            return;
        }
        Render3D.begin(ctx, false, 1.0f);
        int argb = (color.get() & 0x00FFFFFF) | (int) (opacity.get() / 100.0 * 255) << 24;
        for (BlockPos pos : found) {
            Render3D.boxOutline(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, argb);
        }
        Render3D.end();
    }
}