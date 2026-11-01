package ru.turik.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import ru.turik.Turik;
import ru.turik.render.shader.Shader;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;

/**
 * Turik's own 2D render engine. All GUI rendering goes through this class:
 * raw OpenGL with private shaders, buffers and state — no Minecraft
 * DrawContext, no vanilla font renderer, no vanilla GUI pipeline.
 *
 * Coordinate space: "GUI pixels" (framebuffer / Minecraft GUI scale), y down.
 */
public final class RenderEngine {
    private static final RenderEngine INSTANCE = new RenderEngine();
    private static final int FLAT_STRIDE = 14 * 4;   // pos2 + color4 + aux1_4 + aux2_4
    private static final int TEX_STRIDE = 8 * 4;     // pos2 + uv2 + color4
    private static final int MAX_BYTES = 1 << 20;

    private Shader flat;
    private Shader tex;

    private int flatVao = -1, flatVbo = -1;
    private int texVao = -1, texVbo = -1;
    private ByteBuffer flatBuffer;
    private ByteBuffer texBuffer;

    private int viewWidth, viewHeight;
    private float guiScale = 1f;
    private boolean inFrame;
    private final Deque<int[]> scissors = new ArrayDeque<>();

    private final BlurRenderer blur = new BlurRenderer();

    /** Global UI scale multiplier (ClickGUI scale slider), applied in beginGui. */
    public static float uiScaleExtra = 1f;

    public static RenderEngine get() {
        return INSTANCE;
    }

    private RenderEngine() {
    }

    // ---- lifecycle -------------------------------------------------------

    /** Creates shaders and buffers; requires a live GL context. */
    public void init() {
        if (flatVao != -1) {
            return;
        }
        Turik.get().shaders().init();
        flat = Turik.get().shaders().get("flat");
        tex = Turik.get().shaders().get("tex");

        flatVao = glGenVertexArrays();
        flatVbo = glGenBuffers();
        flatBuffer = ByteBuffer.allocateDirect(MAX_BYTES);
        glBindVertexArray(flatVao);
        glBindBuffer(GL_ARRAY_BUFFER, flatVbo);
        setupLayout(new int[]{2, 4, 4, 4});
        glBindVertexArray(0);

        texVao = glGenVertexArrays();
        texVbo = glGenBuffers();
        texBuffer = ByteBuffer.allocateDirect(MAX_BYTES);
        glBindVertexArray(texVao);
        glBindBuffer(GL_ARRAY_BUFFER, texVbo);
        setupLayout(new int[]{2, 2, 4});
        glBindVertexArray(0);

        Turik.LOGGER.info("[Turik] render engine ready");
    }

    private void setupLayout(int[] attribs) {
        int offset = 0;
        for (int i = 0; i < attribs.length; i++) {
            int size = attribs[i];
            glVertexAttribPointer(i, size, GL_FLOAT, false, strideOf(attribs), offset * 4);
            glEnableVertexAttribArray(i);
            offset += size;
        }
        int expected = strideOf(attribs);
        if (offset != expected) {
            throw new IllegalStateException("layout mismatch");
        }
    }

    private static int strideOf(int[] attribs) {
        int sum = 0;
        for (int a : attribs) {
            sum += a;
        }
        return sum * 4;
    }

    /** Starts a GUI-space frame (own state: blend on, depth off). */
    public void beginGui(MinecraftClient mc) {
        init();
        viewWidth = mc.getWindow().getFramebufferWidth();
        viewHeight = mc.getWindow().getFramebufferHeight();
        guiScale = mc.getWindow().getScaleFactor() * uiScaleExtra;
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA); // shaders output premultiplied
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glDisable(GL_CULL_FACE);
        inFrame = true;
    }

    /** Flushes batches and restores vanilla-friendly state. */
    public void endGui(MinecraftClient mc) {
        flushFlat();
        flushTex();
        scissors.clear();
        glDisable(GL_SCISSOR_TEST);
        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glDisable(GL_BLEND);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindVertexArray(0);
        inFrame = false;
        mc.getFramebuffer().beginWrite(true);
    }

    public float guiScale() {
        return guiScale;
    }

    public int viewWidth() {
        return viewWidth;
    }

    public int viewHeight() {
        return viewHeight;
    }

    public boolean inFrame() {
        return inFrame;
    }

    /** True once the GL objects are created (safe to render). */
    public boolean isEngineAvailable() {
        return flatVao != -1 && flat != null && tex != null;
    }

    // ---- batch -----------------------------------------------------------

    private void flushFlat() {
        if (flatBuffer == null || flatBuffer.position() == 0 || flat == null) {
            return;
        }
        flatBuffer.flip();
        flat.bind();
        flat.set("uView", viewWidth / guiScale, viewHeight / guiScale);
        glBindVertexArray(flatVao);
        glBindBuffer(GL_ARRAY_BUFFER, flatVbo);
        glBufferData(GL_ARRAY_BUFFER, flatBuffer, GL_STREAM_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, flatBuffer.position() / FLAT_STRIDE);
        flatBuffer.clear();
        glBindVertexArray(0);
        Shader.unbind();
    }

    private void flushTex() {
        if (texBuffer == null || texBuffer.position() == 0 || tex == null) {
            return;
        }
        texBuffer.flip();
        tex.bind();
        tex.set("uView", viewWidth / guiScale, viewHeight / guiScale);
        glBindVertexArray(texVao);
        glBindBuffer(GL_ARRAY_BUFFER, texVbo);
        glBufferData(GL_ARRAY_BUFFER, texBuffer, GL_STREAM_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, texBuffer.position() / TEX_STRIDE);
        texBuffer.clear();
        glBindVertexArray(0);
        Shader.unbind();
    }

    // ---- shapes ----------------------------------------------------------

    public void rect(float x, float y, float w, float h, int argb) {
        quadCustom(x, y, w, h, argb, argb, argb, argb, 0f, 0f, x + w / 2f, y + h / 2f);
    }

    /** Horizontal gradient: left → right. */
    public void gradientH(float x, float y, float w, float h, int left, int right) {
        quadCustom(x, y, w, h, left, right, left, right, 0f, 0f, x + w / 2f, y + h / 2f);
    }

    /** Vertical gradient: top → bottom. */
    public void gradientV(float x, float y, float w, float h, int top, int bottom) {
        quadCustom(x, y, w, h, top, top, bottom, bottom, 0f, 0f, x + w / 2f, y + h / 2f);
    }

    public void roundedRect(float x, float y, float w, float h, float radius, int argb) {
        quadSdf(x, y, w, h, radius, 0f, argb, argb, argb, argb);
    }

    /** Rounded rect with per-corner gradient (same color both stops = flat). */
    public void roundedRectGradient(float x, float y, float w, float h, float radius, int topColor, int bottomColor) {
        quadSdf(x, y, w, h, radius, 0f, topColor, topColor, bottomColor, bottomColor);
    }

    /** SDF outline ring: crisp rounded border of the given thickness. */
    public void roundedOutline(float x, float y, float w, float h, float radius, float thickness, int argb) {
        quadSdf(x, y, w, h, radius, thickness, argb, argb, argb, argb);
    }

    public void circle(float cx, float cy, float radius, int argb) {
        quadSdf(cx - radius, cy - radius, radius * 2, radius * 2, radius, 0f, argb, argb, argb, argb);
    }

    /** Bordered rounded panel: outline ring + fill. */
    public void panel(float x, float y, float w, float h, float radius, int fill, int border) {
        roundedRect(x, y, w, h, radius, fill);
        roundedOutline(x, y, w, h, radius, 1f, border);
    }

    /** Soft drop shadow behind a rounded panel. */
    public void shadow(float x, float y, float w, float h, float radius, float size, int argb) {
        int a = (argb >>> 24) & 0xFF;
        for (int i = 3; i >= 1; i--) {
            float o = size * i / 3f;
            int alpha = (int) (a * (0.25f / i));
            if (alpha <= 1) continue;
            roundedRect(x - o / 2f, y - o / 2f, w + o, h + o, radius + o / 2f,
                    (alpha << 24) | (argb & 0xFFFFFF));
        }
    }

    /** Blurred, darkened glass panel sampled from the captured game frame. */
    public void blurPanel(float x, float y, float w, float h, float radius, float dim) {
        int texId = blur.texture();
        if (texId == -1 || !blur.isActive()) {
            roundedRect(x, y, w, h, radius, (int) (dim * 200) << 24);
            return;
        }
        float bw = blur.width(), bh = blur.height();
        // Map this rect from GUI px into the blurred texture space.
        float u0 = x / viewWidth;
        float v0 = y / viewHeight;
        float u1 = (x + w) / viewWidth;
        float v1 = (y + h) / viewHeight;
        drawTextured(texId, x, y, w, h, u0, v0, u1, v1, (0xC8 << 24) | 0xFFFFFF, true);
        if (dim > 0) {
            roundedRect(x, y, w, h, radius, (int) (Math.min(1f, dim) * 255) << 24);
        }
    }

    public void blurCapture(MinecraftClient mc) {
        blur.capture(mc);
    }

    // ---- texturing -------------------------------------------------------

    /** Draws a textured quad (glyph or blur sample) through the tex program. */
    public void drawTextured(int textureId, float x, float y, float w, float h,
                             float u0, float v0, float u1, float v1, int argb, boolean flushFlatFirst) {
        if (flushFlatFirst) {
            flushFlat();
        }
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        float a = ((argb >>> 24) & 0xFF) / 255f;
        if (texBuffer.position() + 6 * TEX_STRIDE > MAX_BYTES) {
            flushTex();
        }
        putTex(x, y, u0, v0, r, g, b, a);
        putTex(x + w, y, u1, v0, r, g, b, a);
        putTex(x + w, y + h, u1, v1, r, g, b, a);
        putTex(x, y, u0, v0, r, g, b, a);
        putTex(x + w, y + h, u1, v1, r, g, b, a);
        putTex(x, y + h, u0, v1, r, g, b, a);
        glActiveTexture(GL_TEXTURE0);
        GlStateManager._bindTexture(textureId);
        flushTex();
    }

    private void putTex(float x, float y, float u, float v, float r, float g, float b, float a) {
        texBuffer.putFloat(x).putFloat(y).putFloat(u).putFloat(v)
                .putFloat(r).putFloat(g).putFloat(b).putFloat(a);
    }

    // ---- internals -------------------------------------------------------

    private void quadSdf(float x, float y, float w, float h, float radius, float outline,
                         int tl, int tr, int bl, int br) {
        quadCustom(x, y, w, h, tl, tr, bl, br, radius, outline, x + w / 2f, y + h / 2f);
    }

    private void quadCustom(float x, float y, float w, float h,
                            int tl, int tr, int bl, int br,
                            float radius, float outline, float cx, float cy) {
        float halfW = w / 2f;
        float halfH = h / 2f;
        float rad = Math.min(radius, Math.min(halfW, halfH));

        if (flatBuffer.position() + 6 * FLAT_STRIDE > MAX_BYTES) {
            flushFlat();
        }
        putFlat(x, y, tl, halfW, halfH, rad, outline, cx, cy);
        putFlat(x + w, y, tr, halfW, halfH, rad, outline, cx, cy);
        putFlat(x + w, y + h, br, halfW, halfH, rad, outline, cx, cy);
        putFlat(x, y, tl, halfW, halfH, rad, outline, cx, cy);
        putFlat(x + w, y + h, br, halfW, halfH, rad, outline, cx, cy);
        putFlat(x, y + h, bl, halfW, halfH, rad, outline, cx, cy);
    }

    private void putFlat(float x, float y, int argb, float hw, float hh, float radius, float outline, float cx, float cy) {
        flatBuffer.putFloat(x).putFloat(y)
                .putFloat(((argb >> 16) & 0xFF) / 255f)
                .putFloat(((argb >> 8) & 0xFF) / 255f)
                .putFloat((argb & 0xFF) / 255f)
                .putFloat(((argb >>> 24) & 0xFF) / 255f)
                .putFloat(hw).putFloat(hh).putFloat(radius).putFloat(outline)
                .putFloat(cx).putFloat(cy).putFloat(0f).putFloat(0f);
    }

    // ---- scissor ---------------------------------------------------------

    public void pushScissor(float x, float y, float w, float h) {
        int sx = Math.round(x * guiScale);
        int sy = Math.round((viewHeight / guiScale - (y + h)) * guiScale);
        int sw = Math.round(w * guiScale);
        int sh = Math.round(h * guiScale);
        if (!scissors.isEmpty()) {
            int[] p = scissors.peek();
            int nx = Math.max(p[0], sx), ny = Math.max(p[1], sy);
            int nw = Math.min(p[0] + p[2], sx + sw) - nx;
            int nh = Math.min(p[1] + p[3], sy + sh) - ny;
            sx = nx;
            sy = ny;
            sw = Math.max(0, nw);
            sh = Math.max(0, nh);
        }
        scissors.push(new int[]{sx, sy, sw, sh});
        glEnable(GL_SCISSOR_TEST);
        glScissor(sx, sy, sw, sh);
    }

    public void popScissor() {
        scissors.pop();
        if (scissors.isEmpty()) {
            glDisable(GL_SCISSOR_TEST);
        } else {
            int[] p = scissors.peek();
            glScissor(p[0], p[1], p[2], p[3]);
        }
    }

    public void release() {
        blur.release();
        if (flatVao != -1) glDeleteVertexArrays(flatVao);
        if (flatVbo != -1) glDeleteBuffers(flatVbo);
        if (texVao != -1) glDeleteVertexArrays(texVao);
        if (texVbo != -1) glDeleteBuffers(texVbo);
        flatVao = flatVbo = texVao = texVbo = -1;
    }
}
