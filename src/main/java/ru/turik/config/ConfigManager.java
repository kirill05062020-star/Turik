package ru.turik.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import ru.turik.Turik;
import ru.turik.module.Module;
import ru.turik.module.setting.Setting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Configuration: multiple profiles, automatic save/load, corruption
 * recovery. Files live under run dir / turik / configs.
 */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String ACTIVE = "active.json";

    private final Turik turik;
    private final Path dir = Path.of("turik", "configs");
    private final AtomicBoolean dirty = new AtomicBoolean(true);
    private long lastAutoSave = System.currentTimeMillis();

    public ConfigManager(Turik turik) {
        this.turik = turik;
        // Auto-save loop: persist 30 s after the last change (also on shutdown).
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (dirty.get() && System.currentTimeMillis() - lastAutoSave > 30_000) {
                saveActive();
            }
        });
    }

    public void requestSave() {
        dirty.set(true);
        lastAutoSave = System.currentTimeMillis();
    }

    // ---- persistence -----------------------------------------------------

    public synchronized void saveActive() {
        saveTo(resolve(ACTIVE));
        dirty.set(false);
    }

    public synchronized void loadActive() {
        loadFrom(resolve(ACTIVE));
    }

    public synchronized void saveTo(Path path) {
        try {
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("version", 1);

            JsonObject modules = new JsonObject();
            for (Module module : turik.modules().getAll()) {
                JsonObject m = new JsonObject();
                m.addProperty("enabled", module.isEnabled());
                m.addProperty("bind", module.bind());
                JsonObject settings = new JsonObject();
                for (Setting setting : module.settings()) {
                    settings.addProperty(setting.name(), String.valueOf(setting.getValue()));
                }
                m.add("settings", settings);
                modules.add(module.name(), m);
            }
            root.add("modules", modules);

            JsonObject hud = new JsonObject();
            for (Map.Entry<String, float[]> e : turik.hud().savePositions().entrySet()) {
                hud.addProperty(e.getKey(), e.getValue()[0] + "," + e.getValue()[1]);
            }
            root.add("hud", hud);

            root.addProperty("friendColor", turik.friends().friendColor());
            Files.writeString(path, GSON.toJson(root));
        } catch (Exception e) {
            Turik.LOGGER.warn("[Turik] failed to save config " + path, e);
        }
    }

    public synchronized void loadFrom(Path path) {
        try {
            if (!Files.exists(path)) {
                return;
            }
            JsonObject root = GSON.fromJson(Files.readString(path), JsonObject.class);
            if (root == null) {
                throw new IllegalStateException("empty config");
            }
            JsonObject modules = root.getAsJsonObject("modules");
            if (modules != null) {
                for (Module module : turik.modules().getAll()) {
                    JsonObject m = modules.getAsJsonObject(module.name());
                    if (m == null) {
                        continue;
                    }
                    module.setBind(m.has("bind") ? m.get("bind").getAsInt() : -1);
                    JsonObject settings = m.getAsJsonObject("settings");
                    if (settings != null) {
                        for (Setting setting : module.settings()) {
                            if (settings.has(setting.name())) {
                                setting.setValueRaw(settings.get(setting.name()).getAsString());
                            }
                        }
                    }
                    module.setEnabled(m.has("enabled") && m.get("enabled").getAsBoolean());
                }
            }
            JsonObject hud = root.getAsJsonObject("hud");
            if (hud != null) {
                Map<String, float[]> positions = new java.util.LinkedHashMap<>();
                for (Map.Entry<String, com.google.gson.JsonElement> e : hud.entrySet()) {
                    String[] parts = e.getValue().getAsString().split(",");
                    if (parts.length == 2) {
                        positions.put(e.getKey(), new float[]{Float.parseFloat(parts[0]), Float.parseFloat(parts[1])});
                    }
                }
                turik.hud().loadPositions(positions);
            }
            if (root.has("friendColor")) {
                turik.friends().setFriendColor(root.get("friendColor").getAsInt());
            }
            Turik.LOGGER.info("[Turik] config loaded: " + path.getFileName());
        } catch (Exception e) {
            Turik.LOGGER.warn("[Turik] config damaged, recovering: " + path, e);
            try {
                Files.copy(path, path.resolveSibling(path.getFileName() + ".damaged"),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
            }
        }
    }

    private Path resolve(String name) {
        return dir.resolve(name);
    }

    // ---- profiles --------------------------------------------------------

    public synchronized List<String> profiles() {
        List<String> list = new ArrayList<>();
        if (Files.isDirectory(dir)) {
            try (var stream = Files.list(dir)) {
                stream.filter(p -> p.getFileName().toString().endsWith(".json"))
                        .filter(p -> !p.getFileName().toString().equals(ACTIVE))
                        .forEach(p -> list.add(baseName(p)));
            } catch (IOException e) {
                Turik.LOGGER.warn("[Turik] cannot list profiles", e);
            }
        }
        return list;
    }

    public synchronized boolean createProfile(String name) {
        String safe = safeName(name);
        if (safe.isEmpty() || Files.exists(resolve(safe + ".json"))) {
            return false;
        }
        saveTo(resolve(safe + ".json"));
        return true;
    }

    public synchronized void loadProfile(String name) {
        loadFrom(resolve(safeName(name) + ".json"));
    }

    public synchronized boolean deleteProfile(String name) {
        try {
            return Files.deleteIfExists(resolve(safeName(name) + ".json"));
        } catch (IOException e) {
            return false;
        }
    }

    private static String safeName(String name) {
        return name == null ? "" : name.replaceAll("[^\\w\\- ]", "").trim();
    }

    private static String baseName(Path p) {
        String n = p.getFileName().toString();
        return n.substring(0, n.length() - ".json".length());
    }
}