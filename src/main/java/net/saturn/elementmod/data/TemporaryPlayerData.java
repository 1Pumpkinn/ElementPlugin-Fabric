package net.saturn.elementmod.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporary data storage for player flags and timestamps.
 * Used for abilities that need temporary state (e.g., charm ready, frozen punch ready).
 */
public class TemporaryPlayerData {
    private static final Map<UUID, Map<String, Long>> playerFlags = new ConcurrentHashMap<>();

    public static long getLong(UUID playerId, String key) {
        Map<String, Long> flags = playerFlags.get(playerId);
        if (flags == null) {
            return 0;
        }
        return flags.getOrDefault(key, 0L);
    }

    public static void putLong(UUID playerId, String key, long value) {
        playerFlags.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    public static boolean getBoolean(UUID playerId, String key) {
        return getLong(playerId, key) == 1L;
    }

    public static void putBoolean(UUID playerId, String key, boolean value) {
        putLong(playerId, key, value ? 1L : 0L);
    }

    public static void remove(UUID playerId, String key) {
        Map<String, Long> flags = playerFlags.get(playerId);
        if (flags != null) {
            flags.remove(key);
            if (flags.isEmpty()) {
                playerFlags.remove(playerId);
            }
        }
    }

    public static void clear(UUID playerId) {
        playerFlags.remove(playerId);
    }
}

