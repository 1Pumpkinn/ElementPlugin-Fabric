package net.saturn.elementpluginfabric.managers;

import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
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
        return currentlyRolling.contains(player.getUUID());
    }

    public void cancelRolling(ServerPlayer player) {
        currentlyRolling.remove(player.getUUID());
        activeAnimations.remove(player.getUUID());
    }

    public void rollAndAssign(ServerPlayer player) {
        if (!beginRoll(player)) return;

        player.playNotifySound(SoundEvents.UI_TOAST_IN, SoundSource.MASTER, 1f, 1.2f);

        RollingAnimation animation = new RollingAnimation(player, BASIC_ELEMENTS);
        activeAnimations.put(player.getUUID(), animation);
        animation.start(() -> {
            assignRandomElement(player);
            endRoll(player);
        });
    }

    private void assignRandomElement(ServerPlayer player) {
        ElementType randomType = BASIC_ELEMENTS[random.nextInt(BASIC_ELEMENTS.length)];
        assignElementInternal(player, randomType, "Element Assigned!");
    }

    public void assignRandomDifferentElement(ServerPlayer player) {
        ElementType current = getPlayerElement(player);
        List<ElementType> available = Arrays.stream(BASIC_ELEMENTS)
                .filter(type -> type != current)
                .toList();

        ElementType newType = available.isEmpty() ?
                BASIC_ELEMENTS[random.nextInt(BASIC_ELEMENTS.length)] :
                available.get(random.nextInt(available.size()));

        assignElementInternal(player, newType, "Element Rerolled!");
    }

    public void assignElement(ServerPlayer player, ElementType type) {
        assignElementInternal(player, type, "Element Chosen!", true);
    }

    public void setElement(ServerPlayer player, ElementType type) {
        PlayerData pd = data(player.getUUID());
        ElementType old = pd.getCurrentElement();

        if (old != null && old != type) {
            handleElementSwitch(player, old);
        }

        pd.setCurrentElement(type);
        store.save(pd);

        player.sendSystemMessage(Component.literal("Your element is now ")
                .append(Component.literal(type.name()).withStyle(style -> style.withColor(0x55FFFF))));
        applyUpsides(player);
    }

    private void assignElementInternal(ServerPlayer player, ElementType type, String titleText) {
        assignElementInternal(player, type, titleText, false);
    }

    private void assignElementInternal(ServerPlayer player, ElementType type, String titleText, boolean resetLevel) {
        PlayerData pd = data(player.getUUID());
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
        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1f, 1f);
    }

    private void handleElementSwitch(ServerPlayer player, ElementType oldElement) {
        returnLifeOrDeathCore(player, oldElement);
        EffectService service = plugin.getEffectService();
        if (service != null) {
            service.clearAllElementEffects(player);
        }
    }

    public void applyUpsides(ServerPlayer player) {
        EffectService service = plugin.getEffectService();
        if (service != null) {
            service.applyPassiveEffects(player);
        }
    }

    public boolean useAbility1(ServerPlayer player) {
        return useAbility(player, 1);
    }

    public boolean useAbility2(ServerPlayer player) {
        return useAbility(player, 2);
    }

    private boolean useAbility(ServerPlayer player, int number) {
        PlayerData pd = data(player.getUUID());
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

    public void giveElementItem(ServerPlayer player, ElementType type) {
        // TODO: Implement ElementCoreItem for Fabric
        // var item = ElementCoreItem.createCore(plugin, type);
        // if (item != null) {
        //     player.getInventory().add(item);
        // }
    }

    private void showElementTitle(ServerPlayer player, ElementType type, String title) {
        // Send title packets (Fabric/Vanilla way)
        player.connection.send(new ClientboundSetTitleTextPacket(
                Component.literal(title).withStyle(style -> style.withColor(0xFFAA00))
        ));
        player.connection.send(new ClientboundSetSubtitleTextPacket(
                Component.literal(type.name()).withStyle(style -> style.withColor(0x55FFFF))
        ));
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));
    }

    private void returnLifeOrDeathCore(ServerPlayer player, ElementType oldElement) {
        if (oldElement != ElementType.LIFE && oldElement != ElementType.DEATH) return;
        if (!data(player.getUUID()).hasElementItem(oldElement)) return;

        // TODO: Implement ElementCoreItem for Fabric
        // var core = ElementCoreItem.createCore(plugin, oldElement);
        // if (core != null) {
        //     player.getInventory().add(core);
        //     player.sendSystemMessage(Component.literal("Your core has been returned!").withStyle(style -> style.withColor(0xFFFF55)));
        // }
    }

    private boolean beginRoll(ServerPlayer player) {
        if (isCurrentlyRolling(player)) {
            player.sendSystemMessage(Component.literal("You are already rerolling!").withStyle(style -> style.withColor(0xFF5555)));
            return false;
        }
        currentlyRolling.add(player.getUUID());
        return true;
    }

    private void endRoll(ServerPlayer player) {
        currentlyRolling.remove(player.getUUID());
        activeAnimations.remove(player.getUUID());
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
        private final ServerPlayer player;
        private final ElementType[] elements;
        private int tick = 0;
        private Runnable onComplete;

        RollingAnimation(ServerPlayer player, ElementType[] elements) {
            this.player = player;
            this.elements = elements;
        }

        void start(Runnable onComplete) {
            this.onComplete = onComplete;
        }

        void tick() {
            if (player.hasDisconnected() || !isCurrentlyRolling(player)) {
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
                player.connection.send(new ClientboundSetTitleTextPacket(
                        Component.literal("Rolling...").withStyle(style -> style.withColor(0xFFAA00))
                ));
                player.connection.send(new ClientboundSetSubtitleTextPacket(
                        Component.literal(name).withStyle(style -> style.withColor(0x55FFFF))
                ));
                player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 10, 0));
            }
            tick++;
        }
    }
}