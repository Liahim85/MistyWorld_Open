package ru.liahim.mist.item;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.world.MistWorld;

public class ItemMistCentrometer extends ItemMist {
	
	public ItemMistCentrometer() {
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.addPropertyOverride(new ResourceLocation("mist:angle"), new IItemPropertyGetter() {

			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entityIn) {
				if (entityIn == null && !stack.isOnItemFrame()) {
					return 0;
				} else {
					boolean main = stack.getItemDamage() == 1;
					boolean flag = entityIn != null;
					Entity entity = flag ? entityIn : stack.getItemFrame();
					if (world == null) world = entity.world;
					if (world.provider.getDimension() != Mist.getID()) return 0;
					BlockPos pos = new BlockPos(MathHelper.floor(entity.posX), entity.posY + (flag ? 1 : 0), MathHelper.floor(entity.posZ));
					if (!MistWorld.isPosInFog(world, pos)) return 0.0F;
					BlockPos centralPos = MistWorld.getCenterPos(pos, main);
					if (centralPos.distanceSq(pos) < 10000) return 33 + (main ? 33 : 0);					
					double d0 = 0;
					double d1 = flag ? (double)entity.rotationYaw : this.getFrameRotation((EntityItemFrame)entity);
					d1 = d1 % 360.0D;
					double d2 = Math.atan2(centralPos.getZ() - pos.getZ(), centralPos.getX() - pos.getX());
					d0 = (90.0D - d1 + d2 / 0.01745329238474369D) * 0.08888888888888889D;
					return MathHelper.positiveModulo((float)d0, 32) + 1 + (main ? 33 : 0);
				}
			}

			@SideOnly(Side.CLIENT)
			private double getFrameRotation(EntityItemFrame frame) {
				return MathHelper.wrapDegrees(180 + frame.facingDirection.getHorizontalIndex() * 90);
			}
		});
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		int i = player.getHeldItem(hand).getMetadata() == 0 ? 1 : 0;
		player.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, new ItemStack(this, 1, i));
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.EPIC;
	}
}