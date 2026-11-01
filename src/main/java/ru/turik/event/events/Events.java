package ru.turik.event.events;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.FishingBobberEntity;

/** Container of all Turik event types posted on the {@link ru.turik.event.EventBus}. */
public final class Events {
    private Events() {
    }

    /** End of every client tick. */
    public record TickEvent(MinecraftClient client) {
    }

    /** Keyboard event outside Turik's own screens. action: GLFW_PRESS/GLFW_RELEASE/GLFW_REPEAT. */
    public record KeyEvent(int key, int action, int modifiers) {
        public boolean isPress() {
            return action == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        }
    }

    /** Mouse button event outside Turik's own screens. */
    public record MouseButtonEvent(int button, int action, int modifiers) {
        public boolean isPress() {
            return action == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        }
    }

    /** Raised once per tick while the local player's bobber has hooked a fish. */
    public record FishBiteEvent(FishingBobberEntity bobber) {
    }

    /** Raised when the client is about to attack an entity; cancellable. */
    public static final class AttackEvent {
        private final Object target;
        private boolean cancelled;

        public AttackEvent(Object target) {
            this.target = target;
        }

        public Object target() {
            return target;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
