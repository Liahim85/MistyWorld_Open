package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.api.block.IMossable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.init.BlockColoring;
import ru.liahim.mist.util.FacingHelper;
import ru.liahim.mist.world.MistWorld;

public class MistBlockFenceStone extends BlockWall implements IColoredBlock, IMossable {

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColor() {
		return BlockColoring.GRASS_COLORING_1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return BlockColoring.BLOCK_ITEM_COLORING;
	}

	public MistBlockFenceStone(Block modelBlock) {
		super(modelBlock);
		this.setTickRandomly(true);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			if (state.getValue(VARIANT) == EnumType.MOSSY) {
				if (rand.nextInt(4) == 0 && MistWorld.isPosInFog(world, pos.getY())) {
					world.setBlockState(pos, this.getDefaultState());
				}
			} else if (rand.nextInt(500) == 0 && !MistWorld.isPosInFog(world, pos.getY()) && world.getBiome(pos).getRainfall() >= 0.3F) {
				for (EnumFacing face : FacingHelper.NOTDOWN) {
					if (world.getBlockState(pos.offset(face)).getBlock() == MistBlocks.ACID_BLOCK) {
						return;
					}
				}
				if (world.getBlockState(pos.down()).getBlock() instanceof MistGrass) {
					world.setBlockState(pos, this.getDefaultState().withProperty(VARIANT, EnumType.MOSSY));
				}
			}
		}
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		if (this == MistBlocks.COBBLESTONE_FENCE) return MistItems.ROCKS;
		else if (this == MistBlocks.STONE_BRICK_FENCE) return MistItems.BRICK;
		return super.getItemDropped(state, rand, fortune);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}

	@Override
	public int quantityDropped(Random random) {
		return 6;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(this, 1, state.getValue(VARIANT).getMetadata());
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return state.getValue(VARIANT) == EnumType.MOSSY ? layer == BlockRenderLayer.CUTOUT_MIPPED : layer == BlockRenderLayer.SOLID;
	}

	@Override
	public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
		return true;
	}

	@Override
	public boolean setMossy(IBlockState state, World world, BlockPos pos) {
		if (!(state.getBlock() instanceof IMossable) || state.getValue(VARIANT) == EnumType.MOSSY) return false;
		return world.setBlockState(pos, state.withProperty(VARIANT, EnumType.MOSSY));
	}
}