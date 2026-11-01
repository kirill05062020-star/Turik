package ru.turik.render.shader;

import ru.turik.Turik;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Owns every GLSL program of the client and hands out free texture ids.
 * Initialized once a live GL context exists (after client start).
 */
public final class ShaderManager {
    private final Map<String, Shader> programs = new LinkedHashMap<>();
    private boolean ready;

    public void init() {
        if (ready) {
            return;
        }
        try {
            put(new Shader("flat", "/assets/turik/shaders/flat.vsh", "/assets/turik/shaders/flat.fsh"));
            put(new Shader("tex", "/assets/turik/shaders/tex.vsh", "/assets/turik/shaders/tex.fsh"));
            put(new Shader("world", "/assets/turik/shaders/world.vsh", "/assets/turik/shaders/world.fsh"));
            put(new Shader("blur", "/assets/turik/shaders/tex.vsh", "/assets/turik/shaders/blur.fsh"));
            put(new Shader("sky", "/assets/turik/shaders/tex.vsh", "/assets/turik/shaders/sky.fsh"));
            ready = true;
            Turik.LOGGER.info("[Turik] shader engine ready: {} programs", programs.size());
        } catch (Throwable t) {
            Turik.LOGGER.error("[Turik] shader engine failed to initialize", t);
        }
    }

    private void put(Shader shader) {
        programs.put(shader.name(), shader);
    }

    public Shader get(String name) {
        return programs.get(name);
    }

    public boolean isReady() {
        return ready;
    }
}
