package ru.turik.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import ru.turik.Turik;
import ru.turik.gui.NotificationManager;
import ru.turik.render.RenderEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the HUD: elements, positions, the HUD-editor drag state and the
 * Fabric HUD hook that draws everything through Turik's render engine.
 */
public final class HudManager {
    private final List<HudElement> elements = new ArrayList<>();
    private boolean editorMode;
    private HudElement dragTarget;

    public HudManager(Turik turik) {
        elements.add(new WatermarkElement());
        elements.add(new PlayerInfoElement());
        elements.add(new ActiveModulesElement());
    }

    public void init() {
        // Draw on top of the vanilla HUD via the Fabric HUD registry.
        // The DrawContext parameter is intentionally unused: all pixels
        // come from Turik's own GLSL pipeline.
        net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.addLast(
                Identifier.of("turik", "main_hud"),
                (DrawContext context, RenderTickCounter tickCounter) -> {
                    MinecraftClient mc = Turik.get().mc();
                    RenderEngine engine = RenderEngine.get();
                    if (!engine.isEngineAvailable()) {
                        return;
                    }
                    engine.beginGui(mc);
                    if (editorMode) {
                        engine.blurCapture(mc);
                    }
                    for (HudElement element : elements) {
                        if (element.visible()) {
                            try {
                                element.render();
                            } catch (Throwable t) {
                                Turik.LOGGER.error("[Turik] HUD element failed: " + element.name(), t);
                            }
                        }
                    }
                    if (editorMode) {
                        for (HudElement element : elements) {
                            if (element.visible()) {
                                engine.roundedOutline(element.x() - 2, element.y() - 2,
                                        element.width() + 4, element.height() + 4, 5f, 1f, 0xFF2BD9FF);
                            }
                        }
                    }
                    Turik.get().notifications().render(System.currentTimeMillis());
                    ru.turik.modules.world.Fog fog = Turik.get().modules().get(ru.turik.modules.world.Fog.class);
                    if (fog != null) {
                        fog.renderHud(0f);
                    }
                    engine.endGui(mc);
                });
    }

    public List<HudElement> elements() {
        return elements;
    }

    public boolean isEditorMode() {
        return editorMode;
    }

    public void setEditorMode(boolean editorMode) {
        this.editorMode = editorMode;
        this.dragTarget = null;
    }

    /** Mouse handling used by the HUD editor inside the ClickGUI. */
    public boolean editorClick(float mx, float my) {
        for (HudElement e : elements) {
            if (e.visible() && e.mouseOver(mx, my)) {
                dragTarget = e;
                e.startDrag();
                return true;
            }
        }
        return false;
    }

    public void editorDrag(float mx, float my) {
        if (dragTarget != null) {
            dragTarget.dragTo(mx, my);
        }
    }

    public void editorRelease() {
        if (dragTarget != null) {
            dragTarget.stopDrag();
            dragTarget = null;
        }
    }

    /** HUD positions from config. */
    public void loadPositions(java.util.Map<String, float[]> positions) {
        for (HudElement e : elements) {
            float[] p = positions.get(e.name());
            if (p != null && p.length == 2) {
                e.setPos(p[0], p[1]);
            }
        }
    }

    public java.util.Map<String, float[]> savePositions() {
        java.util.Map<String, float[]> map = new java.util.LinkedHashMap<>();
        for (HudElement e : elements) {
            map.put(e.name(), new float[]{e.x(), e.y()});
        }
        return map;
    }
}