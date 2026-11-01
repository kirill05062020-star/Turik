package ru.turik.modules.visuals;

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
 * 30. BlockESP — highlights chosen block types with outlines and fills.
 */
public class BlockESP extends Module {
    private final StringSetting blocks = new StringSetting("Blocks",
            "minecraft:diamond_ore, minecraft:emerald_ore, minecraft:ancient_debris, minecraft:spawner");
    private final ColorSetting color = new ColorSetting("Color", 0x992BD9FF);
    private final BooleanSetting outline = new BooleanSetting("Outline", true);
    private final BooleanSetting fill = new BooleanSetting("Fill", true);
    private final NumberSetting radius = new NumberSetting("Radius", 4, 32, 1, 16, "m");
    private final NumberSetting refresh = new NumberSetting("Refresh", 1, 20, 1, 5, "т");
    private final BooleanSetting throughWalls = new BooleanSetting("Through walls", true);

    private List<BlockPos> found = new ArrayList<>();
    private int tickCounter;

    public BlockESP() {
        super("BlockESP", "Выделение выбранных типов блоков",
                Category.VISUALS);
        add(blocks, color, outline, fill, radius, refresh, throughWalls);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) {
            return;
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
    public void onWorldRender(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext ctx) {
        if (found.isEmpty()) {
            return;
        }
        MinecraftClient mc = Turik.get().mc();
        Render3D.begin(ctx, !throughWalls.get(), 1.2f);
        int argb = color.get();
        int fillArgb = (argb & 0x00FFFFFF) | (((argb >>> 24) & 0xFF) / 2) << 24;
        for (BlockPos pos : found) {
            double x = pos.getX(), y = pos.getY(), z = pos.getZ();
            if (outline.get()) {
                Render3D.boxOutline(x - 0.002, y - 0.002, z - 0.002, x + 1.002, y + 1.002, z + 1.002, argb);
            }
            if (fill.get()) {
                Render3D.boxFill(x, y, z, x + 1, y + 1, z + 1, fillArgb);
            }
        }
        Render3D.end();
    }
}