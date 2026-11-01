package ru.turik.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import ru.turik.Turik;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.turik.render.shader.Shader;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;

/**
 * Turik's world-space renderer: tracers, ESP outlines and fills, trajectory
 * arcs, atmosphere particles. Own GLSL program with a uMVP uniform; depth
 * testing is caller-controlled so ESP works through walls.
 */
public final class Render3D {
    private static Shader worldShader;
    private static int vao = -1, vbo = -1;
    private static Matrix4f mvp;
    private static Vec3d cameraPos;
    private static float lineWidth = 1f;

    private Render3D() {
    }

    public static void init() {
        if (vao != -1) return;
        worldShader = Turik.get().shaders().get("world");
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 28, 0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 28, 12);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    /** Starts a world-space batch. depthTest=false renders through walls. */
    public static void begin(WorldRenderContext context, boolean depthTest, float width) {
        init();
        lineWidth = width;
        cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
        Matrix4f proj = context.projectionMatrix();
        Matrix4f view = context.positionMatrix();
        mvp = new Matrix4f(proj).mul(view);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        if (depthTest) {
            glEnable(GL_DEPTH_TEST);
        } else {
            glDisable(GL_DEPTH_TEST);
        }
        glDepthMask(false);
        glDisable(GL_CULL_FACE);
    }

    public static void end() {
        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glLineWidth(1f);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindVertexArray(0);
        Shader.unbind();
    }

    public static void line(Vec3d a, Vec3d b, int argb) {
        try (var stack = org.lwjgl.system.MemoryStack.stackPush()) {
            ByteBuffer buf = stack.malloc(2 * 28);
            put(buf, a.x - cameraPos.x, a.y - cameraPos.y, a.z - cameraPos.z, argb);
            put(buf, b.x - cameraPos.x, b.y - cameraPos.y, b.z - cameraPos.z, argb);
            buf.flip();
            draw(buf, GL_LINES, 2, lineWidth);
        }
    }

    /** Outlined box in world space. */
    public static void boxOutline(double x1, double y1, double z1, double x2, double y2, double z2, int argb) {
        try (var stack = org.lwjgl.system.MemoryStack.stackPush()) {
            ByteBuffer buf = stack.malloc(24 * 28);
            double[][] c = corners(x1, y1, z1, x2, y2, z2);
            int[][] edges = {{0, 1}, {1, 3}, {3, 2}, {2, 0}, {4, 5}, {5, 7}, {7, 6}, {6, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
            for (int[] e : edges) {
                put(buf, c[e[0]][0] - cameraPos.x, c[e[0]][1] - cameraPos.y, c[e[0]][2] - cameraPos.z, argb);
                put(buf, c[e[1]][0] - cameraPos.x, c[e[1]][1] - cameraPos.y, c[e[1]][2] - cameraPos.z, argb);
            }
            buf.flip();
            draw(buf, GL_LINES, 24, lineWidth);
        }
    }

    /** Translucent filled box in world space. */
    public static void boxFill(double x1, double y1, double z1, double x2, double y2, double z2, int argb) {
        try (var stack = org.lwjgl.system.MemoryStack.stackPush()) {
            ByteBuffer buf = stack.malloc(36 * 28);
            double[][] c = corners(x1, y1, z1, x2, y2, z2);
            // 6 faces (2 triangles each, 3 unique + reuse), CCW not needed (cull off).
            int[][] faces = {{0, 1, 2, 3}, {4, 5, 7, 6}, {0, 1, 5, 4}, {2, 3, 7, 7}, {0, 4, 6, 2}, {1, 5, 7, 3}};
            for (int[] f : faces) {
                int[] idx = {f[0], f[1], f[2], f[0], f[2], f[3]};
                for (int i : idx) {
                    put(buf, c[i][0] - cameraPos.x, c[i][1] - cameraPos.y, c[i][2] - cameraPos.z, argb);
                }
            }
            buf.flip();
            draw(buf, GL_TRIANGLES, 36, lineWidth);
        }
    }

    private static double[][] corners(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new double[][]{
                {x1, y1, z1}, {x2, y1, z1}, {x1, y2, z1}, {x2, y2, z1},
                {x1, y1, z2}, {x2, y1, z2}, {x1, y2, z2}, {x2, y2, z2}};
    }

    private static void put(ByteBuffer buf, double x, double y, double z, int argb) {
        buf.putFloat((float) x).putFloat((float) y).putFloat((float) z)
                .putFloat(((argb >> 16) & 0xFF) / 255f)
                .putFloat(((argb >> 8) & 0xFF) / 255f)
                .putFloat((argb & 0xFF) / 255f)
                .putFloat(((argb >>> 24) & 0xFF) / 255f);
    }

    private static void draw(ByteBuffer buf, int mode, int count, float width) {
        if (worldShader == null || !buf.hasRemaining()) {
            return;
        }
        worldShader.bind();
        worldShader.setMat4("uMVP", mvp.get(new float[16]));
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_STREAM_DRAW);
        glLineWidth(width);
        glDrawArrays(mode, 0, count);
        glBindVertexArray(0);
        Shader.unbind();
    }

    /**
     * Projects a world point to 2D GUI pixels; null when behind the camera.
     * Used by Nametags and Trajectories labels.
     */
    public static float[] project(Vec3d worldPos, WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d cam = mc.gameRenderer.getCamera().getCameraPos();
        Matrix4f m = new Matrix4f(context.projectionMatrix()).mul(context.positionMatrix());
        org.joml.Vector4f clip = new org.joml.Vector4f(
                (float) (worldPos.x - cam.x), (float) (worldPos.y - cam.y),
                (float) (worldPos.z - cam.z), 1f).mul(m);
        if (clip.w <= 0.0f) {
            return null;
        }
        float nx = clip.x / clip.w;
        float ny = clip.y / clip.w;
        float w = RenderEngine.get().viewWidth();
        float h = RenderEngine.get().viewHeight();
        float scale = RenderEngine.get().guiScale();
        return new float[]{(nx * 0.5f + 0.5f) * w / scale, (0.5f - ny * 0.5f) * h / scale};
    }

    /** Left for matrices the caller may want to reuse (render order, glow passes). */
    public static MatrixStack dummyStack() {
        return new MatrixStack();
    }
}
