package net.saturn.elementmod.managers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.component.DataComponentTypes;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.data.DataStore;
import net.saturn.elementmod.data.PlayerData;
import net.saturn.elementmod.elements.*;
import net.saturn.elementmod.items.custom.ElementCoreItem;
import net.saturn.elementmod.services.EffectService;
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

    private final ElementMod plugin;
    private final DataStore store;
    private final ManaManager manaManager;
    private final TrustManager trustManager;
    private final ConfigManager configManager;
    private final EffectService effectService;
    private final Map<ElementType, Element> registry = new EnumMap<>(ElementType.class);
    private final Set<UUID> currentlyRolling = new HashSet<>();
    private final Map<UUID, RollingAnimation> activeAnimations = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public ElementManager(ElementMod plugin, DataStore store, ManaManager manaManager,
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

    public ElementMod getPlugin() { return plugin; }
    public EffectService getEffectService() { return effectService; }

    private void registerAllElements() {
        registry.put(ElementType.AIR, new net.saturn.elementmod.elements.impl.AirElement(plugin));
        registry.put(ElementType.WATER, new net.saturn.elementmod.elements.impl.WaterElement(plugin));
        registry.put(ElementType.FIRE, new net.saturn.elementmod.elements.impl.FireElement(plugin));
        registry.put(ElementType.EARTH, new net.saturn.elementmod.elements.impl.EarthElement(plugin));
        registry.put(ElementType.LIFE, new net.saturn.elementmod.elements.impl.LifeElement(plugin));
        registry.put(ElementType.DEATH, new net.saturn.elementmod.elements.impl.DeathElement(plugin));
        registry.put(ElementType.METAL, new net.saturn.elementmod.elements.impl.MetalElement(plugin));
        registry.put(ElementType.FROST, new net.saturn.elementmod.elements.impl.FrostElement(plugin));
    }

    public PlayerData data(UUID uuid) {
        return store.getPlayerData(uuid);
    }

    public Element get(ElementType type) {
        return registry.get(type);
    }

    public ElementType getPlayerElement(ServerPlayerEntity player) {
        return data(player.getUuid()).getCurrentElement();
    }

    public boolean isCurrentlyRolling(ServerPlayerEntity player) {
        return currentlyRolling.contains(player.getUuid());
    }

    public void cancelRolling(ServerPlayerEntity player) {
        currentlyRolling.remove(player.getUuid());
        activeAnimations.remove(player.getUuid());
    }

    public void rollAndAssign(ServerPlayerEntity player) {
        rollAndAssign(player, false);
    }

    public void rollAndAssign(ServerPlayerEntity player, boolean includeSpecial) {
        if (!beginRoll(player)) return;

        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_IN, SoundCategory.MASTER, 1f, 1.2f);

        ElementType[] pool = includeSpecial ? ElementType.values() : BASIC_ELEMENTS;
        RollingAnimation animation = new RollingAnimation(player, pool);
        activeAnimations.put(player.getUuid(), animation);
        animation.start(type -> {
            assignElementInternal(player, type, "Element Assigned!");
        });
    }


    public void assignRandomDifferentElement(ServerPlayerEntity player) {
        assignRandomDifferentBasicElement(player);
    }

    public void assignRandomDifferentBasicElement(ServerPlayerEntity player) {
        startRollingAnimation(player, BASIC_ELEMENTS, true);
    }

    public void assignRandomDifferentAdvancedElement(ServerPlayerEntity player) {
        startRollingAnimation(player, ADVANCED_ELEMENTS, true);
    }

    private void startRollingAnimation(ServerPlayerEntity player, ElementType[] pool, boolean filterCurrent) {
        if (!beginRoll(player)) return;

        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_IN, SoundCategory.MASTER, 1f, 1.2f);

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
        activeAnimations.put(player.getUuid(), animation);
        animation.start(type -> {
            assignElementInternal(player, type, "Element Rerolled!");
        });
    }

    private void assignRandomDifferentElementFromPool(ServerPlayerEntity player, ElementType[] pool) {
        startRollingAnimation(player, pool, true);
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
                .append(Text.literal(type.name()).formatted(Formatting.AQUA)));
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
        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1f, 1f);
    }

    private void handleElementSwitch(ServerPlayerEntity player, ElementType oldElement) {
        returnLifeOrDeathCore(player, oldElement);
        EffectService service = plugin.getEffectService();
        if (service != null) {
            service.clearAllElementEffects(player);
        }
    }

    public void applyUpsides(ServerPlayerEntity player) {
        EffectService service = plugin.getEffectService();
        if (service != null) {
            service.applyPassiveEffects(player);
        }
    }

    public boolean useAbility1(ServerPlayerEntity player) {
        return useAbility(player, 1);
    }

    public boolean useAbility2(ServerPlayerEntity player) {
        return useAbility(player, 2);
    }

    public boolean canUseAbility1(ServerPlayerEntity player) {
        PlayerData pd = data(player.getUuid());
        return pd.getCurrentElementUpgradeLevel() >= 1;
    }

    public boolean canUseAbility2(ServerPlayerEntity player) {
        PlayerData pd = data(player.getUuid());
        return pd.getCurrentElementUpgradeLevel() >= 2;
    }

    public boolean canUsePassive2(ServerPlayerEntity player) {
        PlayerData pd = data(player.getUuid());
        return pd.getCurrentElementUpgradeLevel() >= 2;
    }

    private boolean useAbility(ServerPlayerEntity player, int number) {
        PlayerData pd = data(player.getUuid());
        ElementType type = pd.getCurrentElement();
        Element element = registry.get(type);

        if (element == null) {
            player.sendMessage(Text.literal("You don't have an element assigned!")
                    .formatted(Formatting.RED));
            return false;
        }

        // Check upgrade levels
        int currentUpgrade = pd.getCurrentElementUpgradeLevel();
        if (number == 1 && currentUpgrade < 1) {
            player.sendMessage(Text.literal("You need Upgrader I to use this ability!")
                    .formatted(Formatting.RED));
            return false;
        }
        if (number == 2 && currentUpgrade < 2) {
            player.sendMessage(Text.literal("You need Upgrader II to use this ability!")
                    .formatted(Formatting.RED));
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

    public void giveElementItem(ServerPlayerEntity player, ElementType type) {
        ItemStack item = ElementCoreItem.createCore(type);
        if (item != null) {
            player.getInventory().insertStack(item);
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
        };
    }

    private void showElementTitle(ServerPlayerEntity player, ElementType type, String title) {
        int color = getElementColor(type);
        // Send title packets (Fabric/Vanilla way)
        player.networkHandler.sendPacket(new TitleS2CPacket(
                Text.literal(title).formatted(Formatting.GOLD, Formatting.BOLD)
        ));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(
                Text.literal(type.name()).styled(style -> style.withColor(TextColor.fromRgb(color)).withBold(true).withItalic(true))
        ));
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 40, 10));
    }

    private void returnLifeOrDeathCore(ServerPlayerEntity player, ElementType oldElement) {
        if (oldElement != ElementType.LIFE && oldElement != ElementType.DEATH) return;
        if (!data(player.getUuid()).hasElementItem(oldElement)) return;

        ItemStack core = ElementCoreItem.createCore(oldElement);
        if (core != null) {
            player.getInventory().insertStack(core);
            player.sendMessage(Text.literal("Your core has been returned!").formatted(Formatting.YELLOW));
        }
    }

    private boolean beginRoll(ServerPlayerEntity player) {
        if (isCurrentlyRolling(player)) {
            player.sendMessage(Text.literal("You are already rerolling!").formatted(Formatting.RED));
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
        
        stack.set(DataComponentTypes.CUSTOM_NAME, 
            Text.literal(type.name()).styled(style -> style.withColor(TextColor.fromRgb(getElementColor(type))).withBold(true)));
            
        return stack;
    }

    /**
     * Reusable rolling animation
     */
    private class RollingAnimation {
        private final ServerPlayerEntity player;
        private final ElementType[] pool;
        private final SimpleInventory container;
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

        RollingAnimation(ServerPlayerEntity player, ElementType[] elements) {
            this.player = player;
            this.pool = elements.clone();
            this.container = new SimpleInventory(27);
            
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
            grayGlass.set(DataComponentTypes.CUSTOM_NAME, Text.empty());
            
            ItemStack redGlass = new ItemStack(Items.RED_STAINED_GLASS_PANE);
            redGlass.set(DataComponentTypes.CUSTOM_NAME, 
                Text.literal("Selection Point").formatted(Formatting.RED));

            for (int i = 0; i < 9; i++) {
                container.setStack(i, grayGlass);
                container.setStack(i + 18, grayGlass);
            }
            
            // Selection pointers
            container.setStack(4, redGlass);
            container.setStack(22, redGlass);
            
            updateDisplay();
        }

        private void updateDisplay() {
            for (int i = 0; i < 9; i++) {
                // Wrap index for looping display
                ElementType type = scrollList.get(i % scrollList.size());
                if (type == null) {
                    ItemStack mystery = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
                    mystery.set(DataComponentTypes.CUSTOM_NAME, 
                        Text.literal("?").formatted(Formatting.DARK_GRAY));
                    container.setStack(i + 9, mystery);
                } else {
                    container.setStack(i + 9, getElementDisplayItem(type));
                }
            }
        }

        void start(java.util.function.Consumer<ElementType> onComplete) {
            this.onComplete = onComplete;
            openGui();
        }

        private void openGui() {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, playerEntity) -> {
                return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, inventory, container, 3) {
                    @Override
                    public ItemStack quickMove(PlayerEntity player, int index) { return ItemStack.EMPTY; }
                    @Override
                    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
                        if (slotIndex >= 0 && slotIndex < 27) return;
                        if (actionType == SlotActionType.QUICK_MOVE) return;
                        super.onSlotClick(slotIndex, button, actionType, player);
                    }
                    @Override
                    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) { return false; }
                    @Override
                    public boolean canUse(PlayerEntity player) { return true; }
                };
            }, Text.literal("Rolling Element...")));
        }

        void tick() {
            if (player.isRemoved() || !isCurrentlyRolling(player)) {
                endRoll(player);
                return;
            }

            // Forced Open logic
            if (player.currentScreenHandler == player.playerScreenHandler) {
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
                    player.closeHandledScreen();
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
                player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 1.2f + (random.nextFloat() * 0.4f));
            }
        }
    }
}