package ru.turik.friend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ru.turik.Turik;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Single source of truth for the friend list. Every module (AimAssist,
 * TriggerBot, NoFriendDamage, MiddleClickFriend, ESP filters, ...) asks
 * this manager — no module keeps its own list. Persisted between sessions.
 */
public final class FriendManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file = Path.of("turik", "friends.json");
    private final Set<String> friends = new HashSet<>();
    /** Color friends are rendered with (ESP, nametags, HUD). */
    private int friendColor = 0xFF4CE07B;

    public int friendColor() {
        return friendColor;
    }

    public void setFriendColor(int argb) {
        this.friendColor = argb;
        Turik.get().configs().requestSave();
    }


    public synchronized boolean isFriend(String name) {
        return name != null && friends.contains(name.toLowerCase(Locale.ROOT));
    }

    public synchronized boolean isFriend(net.minecraft.entity.player.PlayerEntity player) {
        return player != null && isFriend(player.getGameProfile().getName());
    }

    public synchronized boolean add(String name) {
        boolean added = name != null && !name.isBlank() && friends.add(name.trim().toLowerCase(Locale.ROOT));
        if (added) {
            save();
            Turik.get().notifications().info("Friends", "Added " + name.trim());
        }
        return added;
    }

    public synchronized boolean remove(String name) {
        boolean removed = name != null && friends.remove(name.trim().toLowerCase(Locale.ROOT));
        if (removed) {
            save();
            Turik.get().notifications().info("Friends", "Removed " + name.trim());
        }
        return removed;
    }

    /** Adds when absent, removes when present. Returns the new state. */
    public synchronized boolean toggle(String name) {
        if (isFriend(name)) {
            remove(name);
            return false;
        }
        add(name);
        return true;
    }

    public synchronized boolean isEmpty() {
        return friends.isEmpty();
    }

    public synchronized List<String> list() {
        return friends.stream().sorted().collect(Collectors.toList());
    }

    public synchronized void clear() {
        friends.clear();
        save();
    }

    public synchronized void load() {
        try {
            if (Files.exists(file)) {
                friends.clear();
                friends.addAll(GSON.fromJson(Files.readString(file), new TypeToken<Set<String>>() {
                }.getType()));
            }
        } catch (Exception e) {
            Turik.LOGGER.warn("[Turik] friends.json is damaged, starting empty", e);
            try {
                Files.copy(file, file.resolveSibling("friends.json.bak"),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
            }
        }
    }

    public synchronized void save() {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(list()));
        } catch (IOException e) {
            Turik.LOGGER.warn("[Turik] failed to save friends.json", e);
        }
    }
}
