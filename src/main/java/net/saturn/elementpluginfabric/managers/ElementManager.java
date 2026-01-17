package net.saturn.elementpluginfabric.managers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.Constants;
import net.saturn.elementpluginfabric.data.DataStore;
import net.saturn.elementpluginfabric.data.PlayerData;
import net.saturn.elementpluginfabric.elements.*;
import net.saturn.elementpluginfabric.services.EffectService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ElementManager {
    private static final ElementType[] BASIC_ELEMENTS = {
            ElementType.AIR, ElementType.WATER, ElementType.FIRE, ElementType.EARTH
    };

    private final ElementPluginFabric plugin;
    private final DataStore store;
    private final ManaManager manaManager;
    private final TrustManager trustManager;
    private final ConfigManager configManager;
    private final EffectService effectService;
    private final Map<ElementType, Element> registry = new EnumMap<>(ElementType.class);
    private final Set<UUID> currentlyRolling = new HashSet<>();
    private final Map<UUID, RollingAnimation> activeAnimations = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public ElementManager(ElementPluginFabric plugin, DataStore store, ManaManager manaManager,
                          TrustManager trustManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.store = store;
        this.manaManager = manaManager;
        this.trustManager = trustManager;
        this.configManager = configManager;
        this.effectService = plugin.getEffectService();
        registerAllElements();
        startAnimationTicker();
    }

    public ElementPluginFabric getPlugin() { return plugin; }
    public EffectService getEffectService() { return effectService; }

    private void registerAllElements() {
        // TODO: Create Fabric versions of all element implementations
        // registry.put(ElementType.AIR, new AirElement(plugin));
        // registry.put(ElementType.WATER, new WaterElement(plugin));
        // registry.put(ElementType.FIRE, new FireElement(plugin));
        // registry.put(ElementType.EARTH, new EarthElement(plugin));
        // registry.put(ElementType.LIFE, new LifeElement(plugin));
        // registry.put(ElementType.DEATH, new DeathElement(plugin));
        // registry.put(ElementType.METAL, new MetalElement(plugin));
        // registry.put(ElementType.FROST, new FrostElement(plugin));
    }

    public PlayerData data(UUID uuid) {
        return store.getPlayerData(uuid);
    }

    public Element get(ElementType type) {
        return registry.get(type);
    }

    public ElementType getPlayerElement(ServerPlayer player) {
        return data(player.getUUID()).getCurrentElement();
    }

    public boolean isCurrentlyRolling(ServerPlayer player) {
        return currentlyRolling.contains(player.getUuid());
    }

    public void cancelRolling(ServerPlayer player) {
        currentlyRolling.remove(player.getUuid());
        activeAnimations.remove(player.getUuid());
    }

    public void rollAndAssign(ServerPlayer player) {
        if (!beginRoll(player)) return;

        player.makeSound(SoundEvents.UI_TOAST_IN, SoundSource.MASTER, 1f, 1.2f);

        RollingAnimation animation = new RollingAnimation(player, BASIC_ELEMENTS);
        activeAnimations.put(player.getUuid(), animation);
        animation.start(() -> {
            assignRandomElement(player);
            endRoll(player);
        });
    }

    private void assignRandomElement(ServerPlayerEntity player) {
        ElementType randomType = BASIC_ELEMENTS[random.nextInt(BASIC_ELEMENTS.length)];
        assignElementInternal(player, randomType, "Element Assigned!");
    }

    public void assignRandomDifferentElement(ServerPlayerEntity player) {
        ElementType current = getPlayerElement(player);
        List<ElementType> available = Arrays.stream(BASIC_ELEMENTS)
                .filter(type -> type != current)
                .toList();

        ElementType newType = available.isEmpty() ?
                BASIC_ELEMENTS[random.nextInt(BASIC_ELEMENTS.length)] :
                available.get(random.nextInt(available.size()));

        assignElementInternal(player, newType, "Element Rerolled!");
    }

    public void assignElement(ServerPlayerEntity player, ElementType type) {
        assignElementInternal(player, type, "Element Chosen!", true);
    }

    public void setElement(ServerPlayerEntity player, ElementType type) {
        PlayerData pd = data(player.getUuid());
        ElementType old = pd.getCurrentElement();

        if (old != null && old != type) {
            handleElementSwitch(player, old);
        }

        pd.setCurrentElement(type);
        store.save(pd);

        player.sendMessage(Text.literal("Your element is now ")
                .formatted(Formatting.GOLD)
                .append(Text.literal(type.name()).formatted(Formatting.AQUA)), false);
        applyUpsides(player);
    }

    private void assignElementInternal(ServerPlayerEntity player, ElementType type, String titleText) {
        assignElementInternal(player, type, titleText, false);
    }

    private void assignElementInternal(ServerPlayerEntity player, ElementType type, String titleText, boolean resetLevel) {
        PlayerData pd = data(player.getUuid());
        ElementType old = pd.getCurrentElement();

        if (old != null && old != type) {
            handleElementSwitch(player, old);
        }

        if (resetLevel) {
            pd.setCurrentElement(type);
        } else {
            int currentUpgrade = pd.getCurrentElementUpgradeLevel();
            pd.setCurrentElementWithoutReset(type);
            pd.setCurrentElementUpgradeLevel(currentUpgrade);
        }

        store.save(pd);
        showElementTitle(player, type, titleText);
        applyUpsides(player);
        player.playSoundToPlayer(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1f, 1f);
    }

    private void handleElementSwitch(ServerPlayerEntity player, ElementType oldElement) {
        returnLifeOrDeathCore(player, oldElement);
        effectService.clearAllElementEffects(player);
    }

    public void applyUpsides(ServerPlayerEntity player) {
        effectService.applyPassiveEffects(player);
    }

    public boolean useAbility1(ServerPlayerEntity player) {
        return useAbility(player, 1);
    }

    public boolean useAbility2(ServerPlayerEntity player) {
        return useAbility(player, 2);
    }

    private boolean useAbility(ServerPlayerEntity player, int number) {
        PlayerData pd = data(player.getUuid());
        ElementType type = pd.getCurrentElement();
        Element element = registry.get(type);

        if (element == null) return false;

        ElementContext ctx = ElementContext.builder()
                .player(player)
                .upgradeLevel(pd.getUpgradeLevel(type))
                .elementType(type)
                .manaManager(manaManager)
                .trustManager(trustManager)
                .configManager(configManager)
                .plugin(plugin)
                .build();

        return number == 1 ? element.ability1(ctx) : element.ability2(ctx);
    }

    public void giveElementItem(ServerPlayerEntity player, ElementType type) {
        // TODO: Implement ElementCoreItem for Fabric
        // var item = ElementCoreItem.createCore(plugin, type);
        // if (item != null) {
        //     player.getInventory().offerOrDrop(item);
        // }
    }

    private void showElementTitle(ServerPlayerEntity player, ElementType type, String title) {
        player.networkHandler.sendPacket(new TitleS2CPacket(
                Text.literal(title).formatted(Formatting.GOLD),
                Text.literal(type.name()).formatted(Formatting.AQUA),
                10, 40, 10
        ));
    }

    private void returnLifeOrDeathCore(ServerPlayerEntity player, ElementType oldElement) {
        if (oldElement != ElementType.LIFE && oldElement != ElementType.DEATH) return;
        if (!data(player.getUuid()).hasElementItem(oldElement)) return;

        // TODO: Implement ElementCoreItem for Fabric
        // var core = ElementCoreItem.createCore(plugin, oldElement);
        // if (core != null) {
        //     player.getInventory().offerOrDrop(core);
        //     player.sendMessage(Text.literal("Your core has been returned!").formatted(Formatting.YELLOW), false);
        // }
    }

    private boolean beginRoll(ServerPlayerEntity player) {
        if (isCurrentlyRolling(player)) {
            player.sendMessage(Text.literal("You are already rerolling!").formatted(Formatting.RED), false);
            return false;
        }
        currentlyRolling.add(player.getUuid());
        return true;
    }

    private void endRoll(ServerPlayerEntity player) {
        currentlyRolling.remove(player.getUuid());
        activeAnimations.remove(player.getUuid());
    }

    private void startAnimationTicker() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (RollingAnimation animation : new ArrayList<>(activeAnimations.values())) {
                animation.tick();
            }
        });
    }

    /**
     * Reusable rolling animation
     */
    private class RollingAnimation {
        private final ServerPlayerEntity player;
        private final ElementType[] elements;
        private int tick = 0;
        private Runnable onComplete;

        RollingAnimation(ServerPlayerEntity player, ElementType[] elements) {
            this.player = player;
            this.elements = elements;
        }

        void start(Runnable onComplete) {
            this.onComplete = onComplete;
        }

        void tick() {
            if (player.isDisconnected() || !isCurrentlyRolling(player)) {
                endRoll(player);
                return;
            }

            if (tick >= Constants.Animation.ROLL_STEPS) {
                if (onComplete != null) onComplete.run();
                endRoll(player);
                return;
            }

            if (tick % Constants.Animation.ROLL_DELAY_TICKS == 0) {
                String name = elements[random.nextInt(elements.length)].name();
                player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Rolling...").formatted(Formatting.GOLD),
                        Text.literal(name).formatted(Formatting.AQUA),
                        0, 10, 0
                ));
            }
            tick++;
        }
    }
}

