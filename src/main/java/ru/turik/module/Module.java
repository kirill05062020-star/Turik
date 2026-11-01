package ru.turik.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import ru.turik.Turik;
import ru.turik.module.setting.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Base class of every Turik feature. Subclasses override the hooks they
 * need; the ModuleManager drives the lifecycle.
 */
public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting> settings = new ArrayList<>();

    private int bind = -1;      // GLFW key code, -1 = unbound
    private boolean enabled;
    /** 0..1 animation factor used by the ClickGUI, driven every tick. */
    private float animation;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    // ---- settings -------------------------------------------------------

    protected final void add(Setting... setting) {
        settings.addAll(Arrays.asList(setting));
    }

    public List<Setting> settings() {
        return settings;
    }

    public Setting setting(String name) {
        for (Setting s : settings) {
            if (s.name().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

    // ---- lifecycle ------------------------------------------------------

    public void onEnable() {
    }

    public void onDisable() {
    }

    /** End-of-client-tick; only called while enabled. */
    public void onTick(MinecraftClient mc) {
    }

    /** World-space rendering (ESP, tracers, particles); only while enabled. */
    public void onWorldRender(WorldRenderContext context) {
    }

    /** HUD-space rendering; only while enabled. */
    public void onHudRender(MatrixStack matrices, float tickDelta) {
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean value) {
        if (enabled == value) {
            return;
        }
        enabled = value;
        try {
            if (enabled) {
                onEnable();
            } else {
                onDisable();
            }
        } catch (Throwable t) {
            Turik.LOGGER.error("[Turik] lifecycle hook of " + name + " failed", t);
        }
        Turik.get().notifications().moduleToggled(this);
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final void animate(float delta) {
        float target = enabled ? 1f : 0f;
        animation += (target - animation) * Math.min(1f, delta * 12f);
        if (Math.abs(animation - target) < 0.005f) {
            animation = target;
        }
    }

    // ---- accessors ------------------------------------------------------

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Category category() {
        return category;
    }

    public int bind() {
        return bind;
    }

    public void setBind(int bind) {
        this.bind = bind;
    }

    public float animation() {
        return animation;
    }

    protected final boolean isFriend(net.minecraft.entity.player.PlayerEntity player) {
        return Turik.get().friends().isFriend(player);
    }

    /** Helper for settings that only make sense in some modes. */
    protected final Supplier<Boolean> whenMode(String mode) {
        return () -> {
            var s = setting("Mode");
            return s != null && mode.equalsIgnoreCase(s.getValue().toString());
        };
    }
}
