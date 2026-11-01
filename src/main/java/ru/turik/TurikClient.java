package ru.turik;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import ru.turik.event.events.TickEvent;

/**
 * Fabric client entrypoint of the Turik client.
 * Wires Fabric lifecycle events into the Turik event bus.
 */
public final class TurikClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            try {
                Turik.bootstrap();
                // Render-engine / shader startup needs a live GL context.
                Turik.get().shaders().init();
            } catch (Throwable t) {
                Turik.LOGGER.error("[Turik] bootstrap failed — client continues without Turik", t);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Turik.get() != null) {
                Turik.get().events().post(new TickEvent(client));
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (Turik.get() != null) {
                Turik.get().configs().saveActive();
                Turik.get().friends().save();
            }
        });
    }
}