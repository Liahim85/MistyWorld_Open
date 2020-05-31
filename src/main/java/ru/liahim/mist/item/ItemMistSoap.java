package ru.liahim.mist.item;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.World;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.init.ModAdvancements;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.init.ModParticle;
import ru.liahim.mist.world.MistWorld;

public class ItemMistSoap extends ItemMist {

	public static Item mudArmor = null;
	
	public ItemMistSoap() {
		super();
		this.setMaxDamage(10);
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
        return !stack.isItemDamaged() ? super.maxStackSize : 1;
    }

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack soap = player.getHeldItem(hand);
		if (soap.getCount() == 1 && player.isWet() && !MistWorld.isPosInFog(world, player.getPosition().getY()) && !this.isInAsid(player)) {
			player.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, soap);
		}
		return new ActionResult<ItemStack>(EnumActionResult.FAIL, soap);
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		if (player.ticksExisted % 3 == 0) player.world.playSound(null, player.getPosition(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.PLAYERS, 0.2F, 1);
		if (player.world.isRemote) {
			player.swingArm(player.getActiveHand());
			Random rand = player.world.rand;
			double x = player.posX + rand.nextDouble() - 0.5D;
			double y = player.posY + rand.nextDouble() * 2;
			double z = player.posZ + rand.nextDouble() - 0.5D;
			player.world.spawnParticle(ModParticle.MIST_BUBBLE, x, y, z, 0, -rand.nextFloat() * 0.2F, 0);
		}
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)entity;
			if (!player.capabilities.isCreativeMode) {
				if (player instanceof EntityPlayerMP) ModAdvancements.CLEAN_UP.trigger((EntityPlayerMP)player, stack);
				stack.damageItem(1, entity);
				int i = 1000;
				ItemStack armor;
				for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
					if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
						armor = player.getItemStackFromSlot(slot);
						if (armor.getItem() instanceof ItemArmor) {
							if (slot == EntityEquipmentSlot.HEAD || slot == EntityEquipmentSlot.FEET) i -= 100;
							else i -= 200;
							if (ModConfig.player.soapWashingArmor && ((ItemArmor)armor.getItem()).hasColor(armor)) ((ItemArmor)armor.getItem()).removeColor(armor);
							if (mudArmor != null && mudArmor.getClass().isAssignableFrom(armor.getItem().getClass())) armor.damageItem(armor.getMaxDamage() / 5, entity);
						}
					}
				}
				IMistCapaHandler capa = IMistCapaHandler.getHandler(player);
				if (ModConfig.player.soapWashingArmor) {
					armor = capa.getMask();
					if (armor.getItem() instanceof ItemArmor&& ((ItemArmor)armor.getItem()).hasColor(armor)) ((ItemArmor)armor.getItem()).removeColor(armor);
				}
				capa.addPollution(-i);
			}
		}
		return stack;
	}

	private boolean isInAsid(Entity entity) {
		if (!(entity.getRidingEntity() instanceof EntityBoat)) {
			AxisAlignedBB bb = entity.getEntityBoundingBox().grow(-0.1D, -0.4D, -0.1D);
			PooledMutableBlockPos pos = PooledMutableBlockPos.retain();
			pos.setPos(entity.posX, entity.posY, entity.posZ);
			int minX = MathHelper.floor(bb.minX);
			int maxX = MathHelper.ceil(bb.maxX);
			int minY = MathHelper.floor(bb.minY);
			int maxY = MathHelper.ceil(bb.maxY);
			int minZ = MathHelper.floor(bb.minZ);
			int maxZ = MathHelper.ceil(bb.maxZ);
			for (int x = minX; x < maxX; ++x) {
				for (int y = minY; y < maxY; ++y) {
					for (int z = minZ; z < maxZ; ++z) {
						if (entity.world.getBlockState(pos.setPos(x, y, z)).getBlock() == MistBlocks.ACID_BLOCK) {
							return true;
						}
					}
				}
			}
			pos.release();
		}
		return false;
	}
}