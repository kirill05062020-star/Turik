package ru.turik.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.GlTexture;
import ru.turik.Turik;
import ru.turik.render.shader.Shader;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.*;

/**
 * Turik's own blur: samples the game's main framebuffer into a private
 * framebuffer at reduced resolution, runs a separable gaussian blur and
 * hands the result to the GUI renderer for frosted-glass panels.
 */
public final class BlurRenderer {
    private static final int SCALE_DIV = 4; // quarter resolution

    private int fboA = -1, fboB = -1;
    private int texA = -1, texB = -1;
    private int width = -1, height = -1;
    private boolean active;
    private int mainFboId = 0;

    private static int mainFramebufferId(MinecraftClient mc) {
        Framebuffer fb = mc.getFramebuffer();
        // Prefer the public accessor, fall back to the field when absent.
        try {
            var method = fb.getClass().getMethod("getFbo");
            Object v = method.invoke(fb);
            return v instanceof Integer i ? i : 0;
        } catch (ReflectiveOperationException ignored) {
            // fall through
        }
        for (Class<?> c = fb.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField("fbo");
                f.setAccessible(true);
                return f.getInt(fb);
            } catch (ReflectiveOperationException ignored) {
                // next class up
            }
        }
        return 0;
    }

    public void capture(MinecraftClient mc) {
        if (!Turik.get().shaders().isReady()) {
            return;
        }
        int fbW = mc.getWindow().getFramebufferWidth();
        int fbH = mc.getWindow().getFramebufferHeight();
        int w = Math.max(1, fbW / SCALE_DIV);
        int h = Math.max(1, fbH / SCALE_DIV);
        if (w != width || h != height) {
            release();
            width = w;
            height = h;
            texA = createTexture(w, h);
            texB = createTexture(w, h);
            fboA = createFramebuffer(texA);
            fboB = createFramebuffer(texB);
        }
        mainFboId = mainFramebufferId(mc);
        if (mainFboId == 0) {
            return;
        }

        Shader blur = Turik.get().shaders().get("blur");
        Shader tex = Turik.get().shaders().get("tex");
        if (blur == null || tex == null) {
            return;
        }

        int prevFbo = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);

        // Pass 1: main framebuffer -> texA (downsampled, horizontal blur)
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboA);
        glViewport(0, 0, width, height);
        if (!(mc.getFramebuffer().getColorAttachment() instanceof GlTexture colorTexture)) {
            active = false;
            return;
        }
        drawFullscreen(blur, colorTexture.getGlId(), 1f / width, 0f, width, height);

        // Pass 2: texA -> texB (vertical blur)
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboB);
        drawFullscreen(blur, texA, 0f, 1f / height, width, height);

        // Restore game state.
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, prevFbo);
        active = true;
    }

    private void drawFullscreen(Shader blur, int sourceTexture, float dx, float dy, int w, int h) {
        blur.bind();
        blur.set("uView", (float) w, (float) h);
        blur.set("uDirection", dx, dy);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sourceTexture);
        glUniform1i(blur.uniform("uTexture"), 0);
        drawQuad(w, h);
    }

    /** Draws one quad covering the whole target using the tex shader layout. */
    private void drawQuad(int w, int h) {
        try (var stack = org.lwjgl.system.MemoryStack.stackPush()) {
            ByteBuffer buf = stack.malloc(6 * 32);
            float white = 1f;
            putVertex(buf, -1, -1, 0, 1, white, white, white, 1);
            putVertex(buf, 1, -1, 1, 1, white, white, white, 1);
            putVertex(buf, 1, 1, 1, 0, white, white, white, 1);
            putVertex(buf, -1, -1, 0, 1, white, white, white, 1);
            putVertex(buf, 1, 1, 1, 0, white, white, white, 1);
            putVertex(buf, -1, 1, 0, 0, white, white, white, 1);
            buf.flip();

            // Minimal own VAO for fullscreen passes.
            int vao = glGenVertexArrays();
            int vbo = glGenBuffers();
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, buf, GL_STREAM_DRAW);
            int stride = 32;
            glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 8);
            glVertexAttribPointer(2, 4, GL_FLOAT, false, stride, 16);
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            glEnableVertexAttribArray(2);
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glBindVertexArray(0);
            glDeleteVertexArrays(vao);
            glDeleteBuffers(vbo);
        }
    }

    private void putVertex(ByteBuffer buf, float x, float y, float u, float v,
                           float r, float g, float b, float a) {
        buf.putFloat(x).putFloat(y).putFloat(u).putFloat(v)
                .putFloat(r).putFloat(g).putFloat(b).putFloat(a);
    }

    /** Returns the blurred scene texture, or -1 when unavailable. */
    public int texture() {
        return active ? texB : -1;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean isActive() {
        return active;
    }

    private static int createTexture(int w, int h) {
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        return tex;
    }

    private static int createFramebuffer(int texture) {
        int fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return fbo;
    }

    public void release() {
        if (fboA != -1) glDeleteFramebuffers(fboA);
        if (fboB != -1) glDeleteFramebuffers(fboB);
        if (texA != -1) glDeleteTextures(texA);
        if (texB != -1) glDeleteTextures(texB);
        fboA = fboB = texA = texB = -1;
        active = false;
    }
}
