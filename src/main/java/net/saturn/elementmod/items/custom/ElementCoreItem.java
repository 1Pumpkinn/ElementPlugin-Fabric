package net.saturn.elementmod.items.custom;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.items.ModItems;

import java.util.Optional;
import java.util.function.Consumer;

public class ElementCoreItem extends Item {
    private static final String ELEMENT_TAG = "element_type";

    public ElementCoreItem(Settings settings) {
        super(settings);
    }

    public static ItemStack createCore(ElementType type) {
        ItemStack stack = new ItemStack(ModItems.ELEMENT_CORE);
        NbtCompound nbt = new NbtCompound();
        nbt.putString(ELEMENT_TAG, type.name());
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }

    public static ElementType getElementType(ItemStack stack) {
        if (stack.isEmpty()) return null;
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return null;
        
        NbtCompound nbt = nbtComponent.copyNbt();
        if (!nbt.contains(ELEMENT_TAG)) return null;
        
        try {
            Object value = nbt.getString(ELEMENT_TAG);
            if (value instanceof Optional<?> opt) {
                return opt.map(s -> ElementType.valueOf((String) s)).orElse(null);
            }
            return ElementType.valueOf((String) value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user instanceof ServerPlayerEntity player) {
            ElementType type = getElementType(stack);
            
            if (type == null) {
                player.sendMessage(Text.literal("This core is empty!")
                        .formatted(Formatting.RED));
                return ActionResult.FAIL;
            }
            
            ElementMod.getInstance().getElementManager().assignElement(player, type);
            
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent component, Consumer<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, component, tooltip, type);
        ElementType elementType = getElementType(stack);
        if (elementType != null) {
            Formatting color = switch (elementType) {
                case AIR -> Formatting.WHITE;
                case WATER -> Formatting.BLUE;
                case FIRE -> Formatting.RED;
                case EARTH -> Formatting.DARK_GREEN;
                case LIFE -> Formatting.GREEN;
                case DEATH -> Formatting.DARK_PURPLE;
                case METAL -> Formatting.GRAY;
                case FROST -> Formatting.AQUA;
                default -> Formatting.GRAY;
            };
            
            tooltip.accept(Text.literal("Element: ").formatted(Formatting.GRAY)
                    .append(Text.literal(elementType.name()).formatted(color).formatted(Formatting.BOLD)));
        }
        tooltip.accept(Text.literal("Use this to unlock a special element!").formatted(Formatting.DARK_PURPLE).formatted(Formatting.ITALIC));
    }
}
