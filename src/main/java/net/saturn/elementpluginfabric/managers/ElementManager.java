package net.saturn.elementpluginfabric.managers;

import com.jcraft.jorbis.Block;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    private static final ElementType[] ADVANCED_ELEMENTS = {
            ElementType.LIFE, ElementType.DEATH, ElementType.FROST, ElementType.METAL
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
        registry.put(ElementType.AIR, new net.saturn.elementpluginfabric.elements.impl.AirElement(plugin));
        registry.put(ElementType.WATER, new net.saturn.elementpluginfabric.elements.impl.WaterElement(plugin));
        registry.put(ElementType.FIRE, new net.saturn.elementpluginfabric.elements.impl.FireElement(plugin));
        registry.put(ElementType.EARTH, new net.saturn.elementpluginfabric.elements.impl.EarthElement(plugin));
        registry.put(ElementType.LIFE, new net.saturn.elementpluginfabric.elements.impl.LifeElement(plugin));
        registry.put(ElementType.DEATH, new net.saturn.elementpluginfabric.elements.impl.DeathElement(plugin));
        registry.put(ElementType.METAL, new net.saturn.elementpluginfabric.elements.impl.MetalElement(plugin));
        registry.put(ElementType.FROST, new net.saturn.elementpluginfabric.elements.impl.FrostElement(plugin));
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
        rollAndAssign(player, false);
    }

    public void rollAndAssign(ServerPlayer player, boolean includeSpecial) {
        if (!beginRoll(player)) return;

        player.playNotifySound(SoundEvents.UI_TOAST_IN, SoundSource.MASTER, 1f, 1.2f);

        ElementType[] pool = includeSpecial ? ElementType.values() : BASIC_ELEMENTS;
        RollingAnimation animation = new RollingAnimation(player, pool);
        activeAnimations.put(player.getUUID(), animation);
        animation.start(type -> {
            assignElementInternal(player, type, "Element Assigned!");
        });
    }


    public void assignRandomDifferentElement(ServerPlayer player) {
        assignRandomDifferentBasicElement(player);
    }

    public void assignRandomDifferentBasicElement(ServerPlayer player) {
        startRollingAnimation(player, BASIC_ELEMENTS, true);
    }

    public void assignRandomDifferentAdvancedElement(ServerPlayer player) {
        startRollingAnimation(player, ADVANCED_ELEMENTS, true);
    }

    private void startRollingAnimation(ServerPlayer player, ElementType[] pool, boolean filterCurrent) {
        if (!beginRoll(player)) return;

        player.playNotifySound(SoundEvents.UI_TOAST_IN, SoundSource.MASTER, 1f, 1.2f);

        final ElementType[] actualPool;
        if (filterCurrent) {
            ElementType current = getPlayerElement(player);
            ElementType[] filtered = Arrays.stream(pool)
                    .filter(type -> type != current)
                    .toArray(ElementType[]::new);
            actualPool = filtered.length == 0 ? pool : filtered;
        } else {
            actualPool = pool;
        }

        RollingAnimation animation = new RollingAnimation(player, actualPool);
        activeAnimations.put(player.getUUID(), animation);
        animation.start(type -> {
            assignElementInternal(player, type, "Element Rerolled!");
        });
    }

    private void assignRandomDifferentElementFromPool(ServerPlayer player, ElementType[] pool) {
        startRollingAnimation(player, pool, true);
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

        if (element == null) {
            player.sendSystemMessage(Component.literal("You don't have an element assigned!")
                    .withStyle(net.minecraft.ChatFormatting.RED));
            return false;
        }

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
        ItemStack item = net.saturn.elementpluginfabric.items.ElementCoreItem.createCore(type);
        if (item != null) {
            player.getInventory().add(item);
        }
    }

    private int getElementColor(ElementType type) {
        return switch (type) {
            case AIR -> 0xFFFFFF;    // White
            case WATER -> 0x5555FF;  // Blue
            case FIRE -> 0xFF5555;   // Red
            case EARTH -> 0x00AA00;  // Dark Green
            case LIFE -> 0x55FF55;   // Green
            case DEATH -> 0xAA00AA;  // Dark Purple
            case METAL -> 0xAAAAAA;  // Gray
            case FROST -> 0x55FFFF;  // Aqua
            default -> 0xFFFFFF;
        };
    }

    private void showElementTitle(ServerPlayer player, ElementType type, String title) {
        int color = getElementColor(type);
        // Send title packets (Fabric/Vanilla way)
        player.connection.send(new ClientboundSetTitleTextPacket(
                Component.literal(title).withStyle(style -> style.withColor(0xFFAA00).withBold(true))
        ));
        player.connection.send(new ClientboundSetSubtitleTextPacket(
                Component.literal(type.name()).withStyle(style -> style.withColor(color).withBold(true).withItalic(true))
        ));
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));
    }

    private void returnLifeOrDeathCore(ServerPlayer player, ElementType oldElement) {
        if (oldElement != ElementType.LIFE && oldElement != ElementType.DEATH) return;
        if (!data(player.getUUID()).hasElementItem(oldElement)) return;

        ItemStack core = net.saturn.elementpluginfabric.items.ElementCoreItem.createCore(oldElement);
        if (core != null) {
            player.getInventory().add(core);
            player.sendSystemMessage(Component.literal("Your core has been returned!").withStyle(style -> style.withColor(0xFFFF55)));
        }
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

    private ItemStack getElementDisplayItem(ElementType type) {
        ItemStack stack = switch (type) {
            case AIR -> new ItemStack(Items.WIND_CHARGE);
            case WATER -> new ItemStack(Items.WATER_BUCKET);
            case FIRE -> new ItemStack(Items.MAGMA_BLOCK);
            case EARTH -> new ItemStack(Items.GRASS_BLOCK);
            case LIFE -> new ItemStack(Items.REDSTONE_BLOCK);
            case DEATH -> new ItemStack(Items.WITHER_SKELETON_SKULL);
            case METAL -> new ItemStack(Items.IRON_BLOCK);
            case FROST -> new ItemStack(Items.SNOW_BLOCK);
            default -> new ItemStack(Items.BARRIER);
        };
        
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, 
            Component.literal(type.name()).withStyle(style -> style.withColor(getElementColor(type)).withBold(true)));
            
        return stack;
    }

    /**
     * Reusable rolling animation
     */
    private class RollingAnimation {
        private final ServerPlayer player;
        private final ElementType[] pool;
        private final SimpleContainer container;
        private int tick = 0;
        private java.util.function.Consumer<ElementType> onComplete;
        private final List<ElementType> scrollList = new ArrayList<>();
        private final ElementType targetElement;
        
        private double currentDelay = 1.0;
        private final double maxDelay = 15.0;
        private final double deceleration = 1.08;
        private int totalShifts = 0;
        private final int minShifts = 30;
        private boolean finished = false;
        private int waitAfterFinish = 0;

        RollingAnimation(ServerPlayer player, ElementType[] elements) {
            this.player = player;
            this.pool = elements.clone();
            this.container = new SimpleContainer(27);
            
            // The "Winner" is decided at the very beginning
            this.targetElement = this.pool[random.nextInt(this.pool.length)];
            
            // Initialize the looping scroll list
            // We want exactly ONE of each element, spaced out with blanks
            List<ElementType> items = new ArrayList<>(Arrays.asList(this.pool));
            Collections.shuffle(items);
            
            for (ElementType type : items) {
                scrollList.add(type);
                // Add 2 blanks after each element to make the "wheel" larger and smoother
                scrollList.add(null);
                scrollList.add(null);
            }
            
            setupInventory();
        }

        private void setupInventory() {
            ItemStack grayGlass = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
            grayGlass.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.empty());
            
            ItemStack redGlass = new ItemStack(Items.RED_STAINED_GLASS_PANE);
            redGlass.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, 
                Component.literal("Selection Point").withStyle(net.minecraft.ChatFormatting.RED));

            for (int i = 0; i < 9; i++) {
                container.setItem(i, grayGlass);
                container.setItem(i + 18, grayGlass);
            }
            
            // Selection pointers
            container.setItem(4, redGlass);
            container.setItem(22, redGlass);
            
            updateDisplay();
        }

        private void updateDisplay() {
            for (int i = 0; i < 9; i++) {
                // Wrap index for looping display
                ElementType type = scrollList.get(i % scrollList.size());
                if (type == null) {
                    ItemStack mystery = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
                    mystery.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, 
                        Component.literal("?").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
                    container.setItem(i + 9, mystery);
                } else {
                    container.setItem(i + 9, getElementDisplayItem(type));
                }
            }
        }

        void start(java.util.function.Consumer<ElementType> onComplete) {
            this.onComplete = onComplete;
            openGui();
        }

        private void openGui() {
            player.openMenu(new SimpleMenuProvider((syncId, inventory, playerEntity) -> {
                return new ChestMenu(MenuType.GENERIC_9x3, syncId, inventory, container, 3) {
                    @Override
                    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
                    @Override
                    public void clicked(int slotIndex, int button, ClickType clickType, Player player) {
                        if (slotIndex >= 0 && slotIndex < 27) return;
                        if (clickType == ClickType.QUICK_MOVE) return;
                        super.clicked(slotIndex, button, clickType, player);
                    }
                    @Override
                    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) { return false; }
                    @Override
                    public boolean stillValid(Player player) { return true; }
                };
            }, Component.literal("Rolling Element...")));
        }

        void tick() {
            if (player.hasDisconnected() || !isCurrentlyRolling(player)) {
                endRoll(player);
                return;
            }

            // Forced Open logic
            if (player.containerMenu == player.inventoryMenu) {
                if (!finished) {
                    openGui();
                } else {
                    endRoll(player);
                    return;
                }
            }

            if (finished) {
                waitAfterFinish++;
                if (waitAfterFinish >= 40) { // 2 second pause at the end
                    player.closeContainer();
                    endRoll(player);
                }
                return;
            }

            tick++;
            if (tick >= currentDelay) {
                tick = 0;
                
                // Shift elements: Circular Loop
                ElementType front = scrollList.remove(0);
                scrollList.add(front);
                totalShifts++;

                // Deceleration logic
                if (totalShifts >= minShifts) {
                    // Check if we can stop: Current item in center (index 4) must be targetElement
                    if (scrollList.get(4) == targetElement && currentDelay >= maxDelay - 2) {
                        finished = true;
                        if (onComplete != null) {
                            onComplete.accept(targetElement);
                            onComplete = null;
                        }
                    } else {
                        // Keep slowing down until we hit the target
                        if (currentDelay < maxDelay) {
                            currentDelay *= deceleration;
                        }
                    }
                }

                updateDisplay();
                player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 0.5f, 1.2f + (random.nextFloat() * 0.4f));
            }
        }
    }
}