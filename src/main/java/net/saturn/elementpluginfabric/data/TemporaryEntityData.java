package net.saturn.elementpluginfabric.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporary data storage for entity flags and timestamps.
 * Used for entities that need temporary state (e.g., summoned mobs, charmed mobs).
 */
public class TemporaryEntityData {
    private static final Map<UUID, Map<String, String>> entityStrings = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Long>> entityLongs = new ConcurrentHashMap<>();

    public static String getString(UUID entityId, String key) {
        Map<String, String> strings = entityStrings.get(entityId);
        if (strings == null) {
            return null;
        }
        return strings.get(key);
    }

    public static void putString(UUID entityId, String key, String value) {
        entityStrings.computeIfAbsent(entityId, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    public static long getLong(UUID entityId, String key) {
        Map<String, Long> longs = entityLongs.get(entityId);
        if (longs == null) {
            return 0;
        }
        return longs.getOrDefault(key, 0L);
    }

    public static void putLong(UUID entityId, String key, long value) {
        entityLongs.computeIfAbsent(entityId, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    public static void remove(UUID entityId, String key) {
        Map<String, String> strings = entityStrings.get(entityId);
        if (strings != null) {
            strings.remove(key);
            if (strings.isEmpty()) {
                entityStrings.remove(entityId);
            }
        }
        Map<String, Long> longs = entityLongs.get(entityId);
        if (longs != null) {
            longs.remove(key);
            if (longs.isEmpty()) {
                entityLongs.remove(entityId);
            }
        }
    }

    public static void clear(UUID entityId) {
        entityStrings.remove(entityId);
        entityLongs.remove(entityId);
    }
}

