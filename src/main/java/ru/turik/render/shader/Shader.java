package ru.turik.render.shader;

import ru.turik.Turik;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

/**
 * Wrapper around a linked GLSL program (vertex + fragment) compiled from
 * resources shipped inside the client. Uniform locations are cached.
 */
public final class Shader {
    private final String name;
    private final int program;
    private final Map<String, Integer> uniforms = new HashMap<>();

    public Shader(String name, String vertexPath, String fragmentPath) {
        this.name = name;
        int vs = compile(GL_VERTEX_SHADER, vertexPath);
        int fs = compile(GL_FRAGMENT_SHADER, fragmentPath);

        program = glCreateProgram();
        glAttachShader(program, vs);
        glAttachShader(program, fs);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(program, 4096);
            throw new IllegalStateException("[Turik] shader link failed (" + name + "): " + log);
        }
        glDeleteShader(vs);
        glDeleteShader(fs);
        Turik.LOGGER.info("[Turik] shader program '{}' linked", name);
    }

    private int compile(int type, String path) {
        String source = read(path);
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader, 4096);
            throw new IllegalStateException("[Turik] shader compile failed (" + path + "): " + log);
        }
        return shader;
    }

    private static String read(String path) {
        try (InputStream in = Shader.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("shader resource missing: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("[Turik] cannot read shader " + path, e);
        }
    }

    public String name() {
        return name;
    }

    public int id() {
        return program;
    }

    public void bind() {
        glUseProgram(program);
    }

    public static void unbind() {
        glUseProgram(0);
    }

    public int uniform(String name) {
        return uniforms.computeIfAbsent(name, n -> glGetUniformLocation(program, n));
    }

    public void set(String name, float x) {
        glUniform1f(uniform(name), x);
    }

    public void set(String name, float x, float y) {
        glUniform2f(uniform(name), x, y);
    }

    public void set(String name, float x, float y, float z) {
        glUniform3f(uniform(name), x, y, z);
    }

    public void set(String name, float x, float y, float z, float w) {
        glUniform4f(uniform(name), x, y, z, w);
    }

    public void setMat4(String name, float[] columnMajor16) {
        try (var stack = org.lwjgl.system.MemoryStack.stackPush()) {
            FloatBuffer buf = stack.mallocFloat(16);
            buf.put(columnMajor16).flip();
            glUniformMatrix4fv(uniform(name), false, buf);
        }
    }
}
