package ru.turik;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.turik.config.ConfigManager;
import ru.turik.event.EventBus;
import ru.turik.font.FontManager;
import ru.turik.friend.FriendManager;
import ru.turik.gui.NotificationManager;
import ru.turik.hud.HudManager;
import ru.turik.module.ModuleManager;
import ru.turik.render.ShaderManager;

/**
 * Central hub of the Turik client. Every subsystem is created, owned and
 * reachable from here, so no component depends on static globals of another.
 */
public final class Turik {
    public static final String NAME = "Turik";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger("Turik");

    private static Turik instance;

    private final EventBus eventBus = new EventBus();
    private final ModuleManager moduleManager = new ModuleManager(this);
    private final FriendManager friendManager = new FriendManager();
    private final FontManager fontManager = new FontManager();
    private final ShaderManager shaderManager = new ShaderManager();
    private final HudManager hudManager = new HudManager(this);
    private final NotificationManager notificationManager = new NotificationManager(this);
    private final ConfigManager configManager = new ConfigManager(this);

    private Turik() {
    }

    public static Turik get() {
        return instance;
    }

    /** Called once from the Fabric client entrypoint. */
    public static void bootstrap() {
        instance = new Turik();
        Turik.LOGGER.info("[Turik] bootstrapping...");
        Turik.get().eventBus.subscribe(Turik.get().moduleManager);
        Turik.get().moduleManager.registerBuiltins();
        Turik.get().friendManager.load();
        Turik.get().fontManager.init();
        Turik.get().hudManager.init();
        Turik.get().configManager.loadActive();
        Turik.LOGGER.info("[Turik] ready — {} modules loaded", Turik.get().moduleManager.getAll().size());
    }

    public EventBus events() {
        return eventBus;
    }

    public ModuleManager modules() {
        return moduleManager;
    }

    public FriendManager friends() {
        return friendManager;
    }

    public FontManager fonts() {
        return fontManager;
    }

    public ShaderManager shaders() {
        return shaderManager;
    }

    public HudManager hud() {
        return hudManager;
    }

    public NotificationManager notifications() {
        return notificationManager;
    }

    public ConfigManager configs() {
        return configManager;
    }

    public MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }
}
