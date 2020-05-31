package ru.liahim.mist.entity.ai;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import ru.liahim.mist.entity.EntityAnimalMist;

public class EntityAITemptMeat extends EntityAITemptTamed {

    public EntityAITemptMeat(EntityAnimalMist temptedEntity, double speed, double tamedSpeed, boolean scaredByPlayerMovement) {
    	this(temptedEntity, speed, tamedSpeed, scaredByPlayerMovement, 10.0D);
    }

    public EntityAITemptMeat(EntityAnimalMist temptedEntity, double speed, double tamedSpeed, boolean scaredByPlayerMovement, double distance) {
       super(temptedEntity, speed, tamedSpeed, scaredByPlayerMovement, null, distance);
    }

    @Override
	public boolean isTempting(ItemStack stack) {
    	Item item = stack.getItem();
		if (item == Items.ROTTEN_FLESH) return false;
        return item instanceof ItemFood && ((ItemFood)item).isWolfsFavoriteMeat();
    }
}