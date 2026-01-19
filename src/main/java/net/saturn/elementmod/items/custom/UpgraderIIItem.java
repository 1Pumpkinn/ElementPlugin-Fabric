package net.saturn.elementmod.items.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.data.PlayerData;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ElementManager;

public class UpgraderIIItem extends Item {
    
    public UpgraderIIItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user instanceof ServerPlayerEntity player) {
            ElementManager elementManager = ElementMod.getInstance().getElementManager();
            ElementType currentElement = elementManager.getPlayerElement(player);
            
            if (currentElement == null) {
                player.sendMessage(Text.literal("You don't have an element assigned!")
                    .formatted(Formatting.RED));
                return ActionResult.FAIL;
            }
            
            PlayerData data = ElementMod.getInstance().getDataStore().getPlayerData(player.getUuid());
            int currentLevel = data.getCurrentElementUpgradeLevel();
            
            if (currentLevel < 1) {
                player.sendMessage(Text.literal("You need upgrade level 1 first!")
                    .formatted(Formatting.YELLOW));
                return ActionResult.FAIL;
            }
            
            if (currentLevel >= 2) {
                player.sendMessage(Text.literal("You already have upgrade level 2!")
                    .formatted(Formatting.YELLOW));
                return ActionResult.FAIL;
            }
            
            // Set upgrade level to 2
            data.setCurrentElementUpgradeLevel(2);
            ElementMod.getInstance().getDataStore().save(data);
            
            // Apply new effects
            elementManager.applyUpsides(player);
            
            player.sendMessage(Text.literal("Upgraded to level 2!")
                .formatted(Formatting.GREEN));
            
            // Consume item
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            
            return ActionResult.SUCCESS;
        }
        
        return ActionResult.PASS;
    }
}

