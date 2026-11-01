package ru.turik.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import ru.turik.Turik;
import ru.turik.event.Listener;
import ru.turik.event.events.Events;
import ru.turik.gui.ClickGuiScreen;
import ru.turik.module.setting.KeybindSetting;
import ru.turik.util.MouseUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Registry and driver of all modules: registration, lookup, keybinds and
 * per-tick dispatch of lifecycle hooks.
 */
public final class ModuleManager {
    /** GLFW key that opens the ClickGUI (Right Shift by default). */
    private static final int CLICK_GUI_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;

    private final List<Module> modules = new ArrayList<>();
    private final List<Module> readOnly = Collections.unmodifiableList(modules);
    private final boolean[] prevKeyStates = new boolean[512];
    private boolean prevGuiKey;

    public ModuleManager(Turik turik) {
        turik.events().subscribe(this);
        net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents.END_MAIN.register(context -> {
            if (Turik.get() == null || context.world() == null) {
                return;
            }
            for (Module module : readOnly) {
                if (!module.isEnabled()) continue;
                try {
                    module.onWorldRender(context);
                } catch (Throwable t) {
                    Turik.LOGGER.error("[Turik] world render of " + module.name() + " failed", t);
                }
            }
        });
    }

    // ---- registration ---------------------------------------------------

    public void registerBuiltins() {
        register(
                // COMBAT (1-7)
                new ru.turik.modules.combat.AimAssist(),
                new ru.turik.modules.combat.TriggerBot(),
                new ru.turik.modules.combat.Velocity(),
                new ru.turik.modules.combat.AutoTotem(),
                new ru.turik.modules.combat.AutoArmor(),
                new ru.turik.modules.combat.AutoWeapon(),
                new ru.turik.modules.combat.NoFriendDamage(),
                // MOVEMENT (8-15)
                new ru.turik.modules.movement.Spider(),
                new ru.turik.modules.movement.Jesus(),
                new ru.turik.modules.movement.NoSlow(),
                new ru.turik.modules.movement.InvMove(),
                new ru.turik.modules.movement.ElytraSwap(),
                new ru.turik.modules.movement.Flight(),
                new ru.turik.modules.movement.Speed(),
                new ru.turik.modules.movement.SafeWalk(),
                // PLAYER (16-25)
                new ru.turik.modules.player.AutoRespawn(),
                new ru.turik.modules.player.FastPlace(),
                new ru.turik.modules.player.FastBreak(),
                new ru.turik.modules.player.NoFall(),
                new ru.turik.modules.player.AntiAFK(),
                new ru.turik.modules.player.AutoFish(),
                new ru.turik.modules.player.ChestStealer(),
                new ru.turik.modules.player.AutoEat(),
                new ru.turik.modules.player.ItemScroller(),
                new ru.turik.modules.player.FastUse(),
                // VISUALS (26-35)
                new ru.turik.modules.visuals.PlayerESP(),
                new ru.turik.modules.visuals.Nametags(),
                new ru.turik.modules.visuals.ItemESP(),
                new ru.turik.modules.visuals.BlockESP(),
                new ru.turik.modules.visuals.FullBright(),
                new ru.turik.modules.visuals.NoRender(),
                new ru.turik.modules.visuals.Tracers(),
                new ru.turik.modules.visuals.StorageESP(),
                new ru.turik.modules.visuals.EntityESP(),
                new ru.turik.modules.visuals.Trajectories(),
                // WORLD (36-41)
                new ru.turik.modules.world.Ambience(),
                new ru.turik.modules.world.Atmosphere(),
                new ru.turik.modules.world.Weather(),
                new ru.turik.modules.world.TimeChanger(),
                new ru.turik.modules.world.Fog(),
                new ru.turik.modules.world.XRay(),
                // MISC (42-47)
                new ru.turik.modules.misc.AutoGG(),
                new ru.turik.modules.misc.Sounds(),
                new ru.turik.modules.misc.MiddleClickFriend(),
                new ru.turik.modules.misc.Watermark(),
                new ru.turik.modules.misc.AutoSwap(),
                new ru.turik.modules.misc.Friends()
        );
        Turik.get().notifications().info("Turik", modules.size() + " modules loaded");
    }

    /** First enabled module that can override the sky color (Ambience/Atmosphere). */
    public ru.turik.modules.world.SkyOverride skyOverride() {
        for (Module module : readOnly) {
            if (module.isEnabled() && module instanceof ru.turik.modules.world.SkyOverride o) {
                return o;
            }
        }
        return null;
    }

    public void register(Module... list) {
        for (Module module : list) {
            if (modules.stream().noneMatch(m -> m.name().equalsIgnoreCase(module.name()))) {
                modules.add(module);
            }
        }
        modules.sort((a, b) -> {
            int byCategory = a.category().ordinal() - b.category().ordinal();
            return byCategory != 0 ? byCategory : a.name().compareToIgnoreCase(b.name());
        });
    }

    public List<Module> getAll() {
        return readOnly;
    }

    public List<Module> byCategory(ru.turik.module.Category category) {
        return readOnly.stream().filter(m -> m.category() == category).collect(Collectors.toList());
    }

    public Optional<Module> byName(String name) {
        return readOnly.stream().filter(m -> m.name().equalsIgnoreCase(name)).findFirst();
    }

    public <T extends Module> T get(Class<T> type) {
        for (Module module : readOnly) {
            if (type.isInstance(module)) {
                return type.cast(module);
            }
        }
        return null;
    }

    public List<Module> search(String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            return readOnly;
        }
        return readOnly.stream()
                .filter(m -> m.name().toLowerCase(Locale.ROOT).contains(q)
                        || m.description().toLowerCase(Locale.ROOT).contains(q))
                .collect(Collectors.toList());
    }

    public List<Module> enabled() {
        return readOnly.stream().filter(Module::isEnabled).collect(Collectors.toList());
    }

    // ---- keybinds and ticks ---------------------------------------------

    @Listener
    public void onTick(Events.TickEvent event) {
        MinecraftClient mc = event.client();
        if (mc.player == null || mc.world == null) {
            return;
        }

        // ClickGUI toggle (raw poll — must also work with vanilla screens open).
        boolean guiKey = InputUtil.isKeyPressed(mc.getWindow(), CLICK_GUI_KEY);
        if (guiKey && !prevGuiKey) {
            mc.send(() -> {
                if (!(mc.currentScreen instanceof ClickGuiScreen)) {
                    mc.setScreen(new ClickGuiScreen());
                }
            });
        }
        prevGuiKey = guiKey;

        pollModuleKeybinds(mc);
        for (Module module : readOnly) {
            module.animate(0.05f);
            if (module.isEnabled()) {
                try {
                    module.onTick(mc);
                } catch (Throwable t) {
                    Turik.LOGGER.error("[Turik] tick of " + module.name() + " failed", t);
                }
            }
        }
    }

    private void pollModuleKeybinds(MinecraftClient mc) {
        long handle = mc.getWindow().getHandle();
        for (Module module : readOnly) {
            int bind = module.bind();
            if (bind <= 0 || bind >= prevKeyStates.length) {
                continue;
            }
            boolean pressed = InputUtil.isKeyPressed(mc.getWindow(), bind);
            if (pressed && !prevKeyStates[bind]) {
                module.toggle();
            }
            prevKeyStates[bind] = pressed;
        }
    }

    @Listener
    public void onKey(Events.KeyEvent event) {
        // module-specific key handling is done by raw polling in onTick;
        // this listener exists for future screen-independent hotkeys.
    }
}
