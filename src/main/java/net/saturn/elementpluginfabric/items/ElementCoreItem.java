package net.saturn.elementpluginfabric.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Optional;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.saturn.elementpluginfabric.ElementPluginFabric;

public class ElementCoreItem extends Item {
    private static final String ELEMENT_TAG = "element_type";

    public ElementCoreItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createCore(ElementType type) {
        ItemStack stack = new ItemStack(ItemRegistry.ELEMENT_CORE);
        CompoundTag tag = new CompoundTag();
        tag.putString(ELEMENT_TAG, type.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    @SuppressWarnings("unchecked")
    public static ElementType getElementType(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) return null;
        
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(ELEMENT_TAG)) return null;
        
        try {
            // Handling the case where getString might return Optional<String> in this environment
            Object result = tag.getString(ELEMENT_TAG);
            String typeName = "";
            if (result instanceof Optional) {
                typeName = ((Optional<String>) result).orElse("");
            } else if (result instanceof String s) {
                typeName = s;
            }
            return ElementType.valueOf(typeName);
        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
        }
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (user instanceof ServerPlayer player) {
            ElementType type = getElementType(stack);
            
            if (type == null) {
                player.sendSystemMessage(Component.literal("This core is empty!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
            
            ElementPluginFabric.getInstance().getElementManager().assignElement(player, type);
            
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltipAdder, TooltipFlag flag) {
        ElementType type = getElementType(stack);
        if (type != null) {
            ChatFormatting color = switch (type) {
                case AIR -> ChatFormatting.WHITE;
                case WATER -> ChatFormatting.BLUE;
                case FIRE -> ChatFormatting.RED;
                case EARTH -> ChatFormatting.DARK_GREEN;
                case LIFE -> ChatFormatting.GREEN;
                case DEATH -> ChatFormatting.DARK_PURPLE;
                case METAL -> ChatFormatting.GRAY;
                case FROST -> ChatFormatting.AQUA;
                default -> ChatFormatting.GRAY;
            };
            
            tooltipAdder.accept(Component.literal("Element: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(type.name()).withStyle(color).withStyle(ChatFormatting.BOLD)));
        }
        tooltipAdder.accept(Component.literal("Use this to unlock a special element!").withStyle(ChatFormatting.DARK_PURPLE).withStyle(ChatFormatting.ITALIC));
    }
}
