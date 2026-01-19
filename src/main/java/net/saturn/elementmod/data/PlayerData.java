package net.saturn.elementmod.data;

import net.saturn.elementmod.elements.ElementType;

import java.util.*;

/**
 * Immutable player data holder using modern Java patterns
 */
public final class PlayerData {
    private final UUID uuid;
    private ElementType currentElement;
    private final EnumSet<ElementType> ownedItems;
    private int mana;
    private int currentElementUpgradeLevel;
    private final Set<UUID> trustedPlayers;

    // === CONSTRUCTORS ===

    public PlayerData(UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid, "UUID cannot be null");
        this.ownedItems = EnumSet.noneOf(ElementType.class);
        this.mana = 100;
        this.currentElementUpgradeLevel = 0;
        this.trustedPlayers = new HashSet<>();
    }

    public PlayerData(UUID uuid, Map<String, Object> data) {
        this(uuid);
        if (data != null) {
            loadFromMap(data);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromMap(Map<String, Object> data) {
        // Load element
        Object elem = data.get("element");
        if (elem != null) {
            try {
                this.currentElement = ElementType.valueOf(elem.toString());
            } catch (IllegalArgumentException ignored) {
                // Invalid element, leave as null
            }
        }

        // Load mana
        Object manaObj = data.get("mana");
        if (manaObj instanceof Number) {
            this.mana = ((Number) manaObj).intValue();
        } else {
            this.mana = 100;
        }

        // Load upgrade level
        Object upgradeObj = data.get("currentUpgradeLevel");
        if (upgradeObj instanceof Number) {
            this.currentElementUpgradeLevel = ((Number) upgradeObj).intValue();
        } else {
            this.currentElementUpgradeLevel = 0;
        }

        // Load owned items
        Object itemsObj = data.get("items");
        if (itemsObj instanceof List) {
            for (Object item : (List<?>) itemsObj) {
                try {
                    ElementType type = ElementType.valueOf(item.toString());
                    this.ownedItems.add(type);
                } catch (IllegalArgumentException ignored) {
                    // Skip invalid items
                }
            }
        }

        // Load trust list
        Object trustObj = data.get("trust");
        if (trustObj instanceof Map) {
            Map<String, Object> trustMap = (Map<String, Object>) trustObj;
            for (String key : trustMap.keySet()) {
                try {
                    this.trustedPlayers.add(UUID.fromString(key));
                } catch (IllegalArgumentException ignored) {
                    // Invalid UUID, skip
                }
            }
        }
    }

    // === GETTERS ===

    public UUID getUuid() {
        return uuid;
    }

    public ElementType getCurrentElement() {
        return currentElement;
    }

    public ElementType getElementType() {
        return currentElement;
    }

    public int getCurrentElementUpgradeLevel() {
        return currentElementUpgradeLevel;
    }

    public int getMana() {
        return mana;
    }

    public Set<ElementType> getOwnedItems() {
        return EnumSet.copyOf(ownedItems);
    }

    public Set<UUID> getTrustedPlayers() {
        return new HashSet<>(trustedPlayers);
    }

    // === SETTERS (with validation) ===

    public void setCurrentElement(ElementType element) {
        this.currentElement = element;
        if (element != null) {
            this.currentElementUpgradeLevel = 0;
        }
    }

    public void setCurrentElementWithoutReset(ElementType element) {
        this.currentElement = element;
    }

    public void setCurrentElementUpgradeLevel(int level) {
        this.currentElementUpgradeLevel = Math.max(0, Math.min(2, level));
    }

    public void setMana(int mana) {
        this.mana = Math.max(0, mana);
    }

    public void addMana(int delta) {
        setMana(this.mana + delta);
    }

    // === ELEMENT-SPECIFIC METHODS ===

    public int getUpgradeLevel(ElementType type) {
        if (type != null && type.equals(currentElement)) {
            return currentElementUpgradeLevel;
        }
        return 0;
    }

    public void setUpgradeLevel(ElementType type, int level) {
        if (type != null && type.equals(currentElement)) {
            setCurrentElementUpgradeLevel(level);
        }
    }

    public Map<ElementType, Integer> getUpgradesView() {
        Map<ElementType, Integer> map = new EnumMap<>(ElementType.class);
        if (currentElement != null) {
            map.put(currentElement, currentElementUpgradeLevel);
        }
        return Collections.unmodifiableMap(map);
    }

    // === OWNED ITEMS ===

    public boolean hasElementItem(ElementType type) {
        return ownedItems.contains(type);
    }

    public void addElementItem(ElementType type) {
        ownedItems.add(type);
    }

    public void removeElementItem(ElementType type) {
        ownedItems.remove(type);
    }

    // === TRUST MANAGEMENT ===

    public boolean isTrusted(UUID uuid) {
        return trustedPlayers.contains(uuid);
    }

    public void addTrustedPlayer(UUID uuid) {
        trustedPlayers.add(uuid);
    }

    public void removeTrustedPlayer(UUID uuid) {
        trustedPlayers.remove(uuid);
    }

    public void setTrustedPlayers(Set<UUID> trusted) {
        trustedPlayers.clear();
        if (trusted != null) {
            trustedPlayers.addAll(trusted);
        }
    }

    // === UTILITY ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "uuid=" + uuid +
                ", element=" + currentElement +
                ", mana=" + mana +
                ", upgradeLevel=" + currentElementUpgradeLevel +
                '}';
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("element", currentElement == null ? null : currentElement.name());
        map.put("mana", mana);
        map.put("currentUpgradeLevel", currentElementUpgradeLevel);
        
        List<String> items = new ArrayList<>();
        for (ElementType t : ownedItems) items.add(t.name());
        map.put("items", items);
        
        Map<String, Boolean> trust = new HashMap<>();
        for (UUID uuid : trustedPlayers) {
            trust.put(uuid.toString(), true);
        }
        map.put("trust", trust);
        
        return map;
    }
}

