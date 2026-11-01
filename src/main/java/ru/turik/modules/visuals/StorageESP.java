package ru.turik.modules.visuals;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ColorSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.render.Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * 34. StorageESP — outlines storage blocks (chests, barrels, shulkers,
 * hoppers, furnaces).
 */
public class StorageESP extends Module {
    private final ColorSetting chestColor = new ColorSetting("Chest color", 0xA0FFB74D);
    private final ColorSetting shulkerColor = new ColorSetting("Shulker color", 0xA0B388FF);
    private final ColorSetting otherColor = new ColorSetting("Other color", 0xA026C6DA);
    private final BooleanSetting fill = new BooleanSetting("Fill", true);
    private final BooleanSetting throughWalls = new BooleanSetting("Through walls", true);
    private final NumberSetting distance = new NumberSetting("Distance", 8, 128, 1, 64, "m");

    public StorageESP() {
        super("StorageESP", "Подсветка сундуков, бочек и других хранилищ",
                Category.VISUALS);
        add(chestColor, shulkerColor, otherColor, fill, throughWalls, distance);
    }

    @Override
    public void onWorldRender(net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext ctx) {
        MinecraftClient mc = Turik.get().mc();
        if (mc.player == null || mc.world == null) {
            return;
        }
        Render3D.begin(ctx, !throughWalls.get(), 1.2f);
        for (BlockEntity be : nearbyBlockEntities(mc)) {
            BlockPos pos = be.getPos();
            if (mc.player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ())
                    > distance.get() * distance.get()) {
                continue;
            }
            int argb;
            if (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity) {
                argb = chestColor.get();
            } else if (be instanceof ShulkerBoxBlockEntity) {
                argb = shulkerColor.get();
            } else if (be instanceof HopperBlockEntity || be instanceof FurnaceBlockEntity) {
                argb = otherColor.get();
            } else {
                continue;
            }
            int fillArgb = (argb & 0x00FFFFFF) | (((argb >>> 24) & 0xFF) / 2) << 24;
            Render3D.boxOutline(pos.getX() - 0.002, pos.getY() - 0.002, pos.getZ() - 0.002,
                    pos.getX() + 1.002, pos.getY() + 1.002, pos.getZ() + 1.002, argb);
            if (fill.get()) {
                Render3D.boxFill(pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, fillArgb);
            }
        }
        Render3D.end();
    }

    private List<BlockEntity> nearbyBlockEntities(MinecraftClient mc) {
        int chunkRadius = (int) Math.ceil(distance.getFloat() / 16.0);
        int centerX = mc.player.getBlockX() >> 4;
        int centerZ = mc.player.getBlockZ() >> 4;
        List<BlockEntity> result = new ArrayList<>();
        for (int x = centerX - chunkRadius; x <= centerX + chunkRadius; x++) {
            for (int z = centerZ - chunkRadius; z <= centerZ + chunkRadius; z++) {
                var chunk = mc.world.getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk instanceof WorldChunk worldChunk) {
                    result.addAll(worldChunk.getBlockEntities().values());
                }
            }
        }
        return result;
    }
}
