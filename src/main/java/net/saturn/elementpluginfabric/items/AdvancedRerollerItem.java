package net.saturn.elementpluginfabric.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.managers.ElementManager;
import net.saturn.elementpluginfabric.managers.ManaManager;

public class AdvancedRerollerItem extends Item {
    
    public AdvancedRerollerItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (user instanceof ServerPlayer player) {
            ElementManager elementManager = ElementPluginFabric.getInstance().getElementManager();
            ManaManager manaManager = ElementPluginFabric.getInstance().getManaManager();
            
            // Check if player is already rolling
            if (elementManager.isCurrentlyRolling(player)) {
                player.sendSystemMessage(Component.literal("You are already rerolling!")
                    .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
            
            // Check mana cost
            int cost = ElementPluginFabric.getInstance().getConfigManager().getItemUseCost(null);
            if (!manaManager.hasMana(player, cost)) {
                player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
            
            // Spend mana
            if (!manaManager.spend(player, cost)) {
                return InteractionResult.FAIL;
            }
            
            // Roll for a different element
            elementManager.assignRandomDifferentAdvancedElement(player);
            
            // Consume item
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
}

