package ru.liahim.mist.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

public class ItemMistFloatingMat extends ItemBlock {

	public ItemMistFloatingMat(Block block) {
		super(block);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		RayTraceResult raytraceresult = this.rayTrace(world, player, true);
		if (raytraceresult == null) {
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
		} else {
			if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
				BlockPos pos = raytraceresult.getBlockPos();
				if (!world.isBlockModifiable(player, pos) || !player.canPlayerEdit(pos, raytraceresult.sideHit, itemstack)) {
					return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
				}
				IBlockState state = world.getBlockState(pos);
				if (state.getMaterial().isLiquid() && (state.getValue(BlockLiquid.LEVEL)).intValue() == 0
						&& !world.getBlockState(pos.up()).getMaterial().isLiquid() && raytraceresult.sideHit == EnumFacing.UP) {
					BlockSnapshot blocksnapshot = BlockSnapshot.getBlockSnapshot(world, pos);
					if (ForgeEventFactory.onPlayerBlockPlace(player, blocksnapshot, EnumFacing.UP, hand).isCanceled()) {
						blocksnapshot.restore(true, false);
						return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
					}
					world.setBlockState(pos, this.block.getStateFromMeta(itemstack.getItemDamage()));
					if (player instanceof EntityPlayerMP) {
						CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, itemstack);
					}
					if (!player.capabilities.isCreativeMode) {
						itemstack.shrink(1);
					}
					player.addStat(StatList.getObjectUseStats(this));
					world.playSound(player, pos, SoundEvents.BLOCK_WATERLILY_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
				}
			}
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		RayTraceResult raytraceresult = this.rayTrace(world, player, true);
		if (raytraceresult != null && raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos blockPos = raytraceresult.getBlockPos();
			IBlockState state = world.getBlockState(blockPos);
			if (state.getMaterial().isLiquid() && (state.getValue(BlockLiquid.LEVEL)).intValue() == 0
					&& !world.getBlockState(blockPos.up()).getMaterial().isLiquid() && raytraceresult.sideHit == EnumFacing.UP) {
				BlockSnapshot blocksnapshot = BlockSnapshot.getBlockSnapshot(world, blockPos);
				if (!ForgeEventFactory.onPlayerBlockPlace(player, blocksnapshot, EnumFacing.UP, hand).isCanceled()) {
					return EnumActionResult.PASS;
				}
			}
		}
		return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }
}