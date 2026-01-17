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
import net.saturn.elementpluginfabric.data.PlayerData;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ElementManager;

public class Upgrader2Item extends Item {
    
    public Upgrader2Item(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        if (user instanceof ServerPlayer player) {
            ElementManager elementManager = ElementPluginFabric.getInstance().getElementManager();
            ElementType currentElement = elementManager.getPlayerElement(player);
            
            if (currentElement == null) {
                player.sendSystemMessage(Component.literal("You don't have an element assigned!")
                    .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
            
            PlayerData data = ElementPluginFabric.getInstance().getDataStore().getPlayerData(player.getUUID());
            int currentLevel = data.getCurrentElementUpgradeLevel();
            
            if (currentLevel < 1) {
                player.sendSystemMessage(Component.literal("You need upgrade level 1 first!")
                    .withStyle(ChatFormatting.YELLOW));
                return InteractionResult.FAIL;
            }
            
            if (currentLevel >= 2) {
                player.sendSystemMessage(Component.literal("You already have upgrade level 2!")
                    .withStyle(ChatFormatting.YELLOW));
                return InteractionResult.FAIL;
            }
            
            // Set upgrade level to 2
            data.setCurrentElementUpgradeLevel(2);
            ElementPluginFabric.getInstance().getDataStore().save(data);
            
            // Apply new effects
            elementManager.applyUpsides(player);
            
            player.sendSystemMessage(Component.literal("Upgraded to level 2!")
                .withStyle(ChatFormatting.GREEN));
            
            // Consume item
            ItemStack stack = user.getItemInHand(hand);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
}

