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
import net.saturn.elementmod.managers.ElementManager;
import net.saturn.elementmod.managers.ManaManager;

public class RerollerItem extends Item {
    
    public RerollerItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user instanceof ServerPlayerEntity player) {
            ElementManager elementManager = ElementMod.getInstance().getElementManager();
            ManaManager manaManager = ElementMod.getInstance().getManaManager();
            
            // Check if player is already rolling
            if (elementManager.isCurrentlyRolling(player)) {
                player.sendMessage(Text.literal("You are already rerolling!")
                    .formatted(Formatting.RED));
                return ActionResult.FAIL;
            }
            
            // Check mana cost
            int cost = ElementMod.getInstance().getConfigManager().getItemUseCost(null);
            if (!manaManager.hasMana(player, cost)) {
                player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
                return ActionResult.FAIL;
            }
            
            // Spend mana
            if (!manaManager.spend(player, cost)) {
                return ActionResult.FAIL;
            }
            
            // Roll for a different element
            elementManager.assignRandomDifferentBasicElement(player);
            
            // Consume item
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            
            return ActionResult.SUCCESS;
        }
        
        return ActionResult.PASS;
    }
}

