package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.liahim.mist.api.block.IMistStoneUpper;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.MistStoneMined.EnumStoneStage;
import ru.liahim.mist.block.MistStoneMined.EnumStoneType;
import ru.liahim.mist.capability.handler.ISkillCapaHandler;
import ru.liahim.mist.capability.handler.ISkillCapaHandler.Skill;
import ru.liahim.mist.handlers.ServerEventHandler;
import ru.liahim.mist.init.ModAdvancements;

public class MistStoneUpper extends MistBlock implements IMistStoneUpper {

	public MistStoneUpper() {
		super(Material.ROCK);
		this.setHardness(100.0F);
		this.setResistance(1000);
		this.setHarvestLevel("pickaxe", 0);
	}

	@Override
	public void getDrops(NonNullList ret, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		Random rand = world instanceof World ? ((World)world).rand : RANDOM;
		int count = quantityDropped(state, fortune, rand);
		for (int i = 0; i < count; i++) {
			Item item = this.getItemDropped(state, rand, fortune);
			if (item != null) {
				ret.add(new ItemStack(item, 1, this.damageDropped(state)));
			}
		}
		int skill = Skill.getLevel(harvesters.get(), Skill.MASON);
		fortune += Math.max(skill - 2, 0);
		if (fortune > 3) fortune = 3;
		if (count < 4 && rand.nextInt(32 - fortune * 4) == 0) {
			ret.add(new ItemStack(Items.IRON_NUGGET));
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isRemote) {
			if (!checkConnection(world, pos)) {
				world.destroyBlock(pos, false);
				Block.spawnAsEntity(world, pos, new ItemStack(MistBlocks.STONE_MINED));
				Pair<BlockPos, EntityPlayer> pair = ServerEventHandler.breakPlayer;
				if (pair != null && pair.getKey().equals(fromPos) && pair.getValue() instanceof EntityPlayerMP) {
					ModAdvancements.STONE_MINED.trigger((EntityPlayerMP)pair.getValue(), new ItemStack(MistBlocks.STONE_MINED));
				}
			} else this.checkFireBlock(world, pos);
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		this.checkFireBlock(world, pos);
    }

	private void checkFireBlock(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos.down());
		if (state.getBlock() == MistBlocks.CAMPFIRE || state.getMaterial() == Material.FIRE || state.getMaterial() == Material.LAVA) {
			world.setBlockState(pos, MistBlocks.STONE_MINED.getDefaultState().withProperty(MistStoneMined.TYPE, EnumStoneType.NATURE).withProperty(MistStoneMined.STAGE, EnumStoneStage.NORMAL));
		}
	}

	public static boolean checkConnection(World world, BlockPos pos) {
		for (EnumFacing face : EnumFacing.VALUES) {
			if (world.isSideSolid(pos.offset(face), face.getOpposite())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		ISkillCapaHandler.getHandler(player).addSkill(Skill.MASON, 2);
		super.harvestBlock(world, player, pos, state, te, stack);
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return MistItems.ROCKS;
	}

	@Override
	public int quantityDropped(Random random) {
		return 2 + random.nextInt(3);
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}
}