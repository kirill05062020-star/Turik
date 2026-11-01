package ru.turik.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal typed event bus. Subscribers register plain objects; methods
 * annotated with {@link Listener} and taking exactly one event parameter
 * receive events of that parameter type (including subclasses).
 */
public final class EventBus {
    private final Map<Class<?>, List<Invocation>> handlers = new ConcurrentHashMap<>();
    private final Map<Object, List<Invocation>> registered = new ConcurrentHashMap<>();

    public void subscribe(Object object) {
        if (object == null || registered.containsKey(object)) {
            return;
        }
        List<Invocation> invocations = new ArrayList<>();
        for (Class<?> type = object.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(Listener.class) || method.getParameterCount() != 1) {
                    continue;
                }
                method.setAccessible(true);
                Invocation invocation = new Invocation(object, method, method.getParameterTypes()[0]);
                invocations.add(invocation);
                handlers.computeIfAbsent(invocation.eventType, k -> new ArrayList<>()).add(invocation);
            }
        }
        registered.put(object, invocations);
    }

    public void unsubscribe(Object object) {
        List<Invocation> invocations = registered.remove(object);
        if (invocations == null) {
            return;
        }
        for (Invocation invocation : invocations) {
            List<Invocation> list = handlers.get(invocation.eventType);
            if (list != null) {
                list.remove(invocation);
            }
        }
    }

    public void post(Object event) {
        List<Invocation> list = handlers.get(event.getClass());
        if (list == null || list.isEmpty()) {
            return;
        }
        // Copy-on-iterate so handlers may subscribe/unsubscribe while we post.
        for (Invocation invocation : List.copyOf(list)) {
            try {
                invocation.method.invoke(invocation.owner, event);
            } catch (Throwable t) {
                Turik.LOGGER.error("[Turik] event handler failed for " + event.getClass().getSimpleName(), t);
            }
        }
    }

    public List<Class<?>> eventTypes() {
        return Collections.unmodifiableList(new ArrayList<>(handlers.keySet()));
    }

    private record Invocation(Object owner, Method method, Class<?> eventType) {
    }
}
