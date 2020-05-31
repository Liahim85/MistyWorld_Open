package ru.liahim.mist.entity.ai;

import java.util.Set;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.item.ItemStack;

public class EntityAITemptStack extends EntityAITempt {
	
    private final Set<ItemStack> temptStacks;

	public EntityAITemptStack(EntityCreature temptedEntity, double speed, boolean scaredByPlayerMovement, Set<ItemStack> temptStacks) {
		super(temptedEntity, speed, scaredByPlayerMovement, null);
		this.temptStacks = temptStacks;
	}
	
	@Override
	protected boolean isTempting(ItemStack stack) {
		for (ItemStack st : this.temptStacks) {
			if (stack.getItem() == st.getItem() && stack.getItemDamage() == st.getItemDamage()) return true;
		}
        return false;
    }
}