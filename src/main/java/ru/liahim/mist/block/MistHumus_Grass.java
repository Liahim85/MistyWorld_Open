package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.api.block.IFarmland;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.init.BlockColoring;
import ru.liahim.mist.util.SoilHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**@author Liahim*/
public class MistHumus_Grass extends MistHumus_Dirt implements IColoredBlock {

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

	public static final PropertyBool SNOWY = BlockGrass.SNOWY;

	public MistHumus_Grass(float hardness, int waterPerm) {
		super(hardness, waterPerm);
		this.setDefaultState(this.blockState.getBaseState().withProperty(WET, true).withProperty(SNOWY, Boolean.valueOf(false)));
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		Block block = worldIn.getBlockState(pos.up()).getBlock();
		return state.withProperty(SNOWY, Boolean.valueOf(block == Blocks.SNOW || block == Blocks.SNOW_LAYER));
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			boolean change = false;
			IBlockState stateUp = world.getBlockState(pos.up());
			if (stateUp.getBlock().isNormalCube(stateUp, world, pos.up()) || (world.getLightFromNeighbors(pos.up()) < 4 && stateUp.getLightOpacity(world, pos.up()) > 2)) {
				change = world.setBlockState(pos, getSoilBlock().getDefaultState().withProperty(WET, state.getValue(WET)), 2);
			}
			if (!change) super.updateTick(world, pos, state, rand);
		}
	}

	@Override
	public boolean doIfWet(World world, BlockPos pos, IBlockState state, Vector2f waterType, boolean fog, Random rand) {
		boolean change = false;
		if (waterType.getY() >= 0 && rand.nextInt(5 - this.getWaterPerm(state)) == 0) {
			IBlockState stateUp = world.getBlockState(pos.up());
			if (stateUp.getBlock() instanceof MistTreeTrunk ? ((MistTreeTrunk)stateUp.getBlock()).getDir(stateUp) != EnumFacing.UP : true) {
				if (waterType.getY() == 0 || MistDirt.getMinHumusLevel(world, pos) < 4)
					change = world.setBlockState(pos, MistBlocks.GRASS_F.getDefaultState().withProperty(MistDirt.HUMUS, 3).withProperty(MistGrass.GROWTH, false), 2);
			}
		}
		if (!change && rand.nextInt(4) == 0 && world.getLightFromNeighbors(pos.up()) >= 9) {
			BlockPos pos1;
			IBlockState state1;
			IBlockState state1Up;
			for (int i = 0; i < 4; ++i) {
				pos1 = pos.add(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1);
				if (pos1.getY() >= 0 && pos1.getY() < 256 && !world.isBlockLoaded(pos1)) continue;
				state1 = world.getBlockState(pos1);
				state1Up = world.getBlockState(pos1.up());
				if (!state1Up.getBlock().isNormalCube(state1Up, world, pos1.up()) && world.getLightFromNeighbors(pos1.up()) >= 4 && state1Up.getLightOpacity(world, pos1.up()) <= 2) {
					if (!(state1.getBlock() instanceof MistGrass) && !(state1.getBlock() instanceof IFarmland) && state1.getBlock() instanceof MistSoil && state1.getValue(WET))
						SoilHelper.setGrass(world, pos1, state1, true, false, 2);
				}
			}
		}
		return change;
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return getSoilBlock().getItemDropped(state, rand, fortune);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { WET, SNOWY });
	}
}