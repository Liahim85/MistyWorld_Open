package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.api.block.IFarmland;
import ru.liahim.mist.init.BlockColoring;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.world.MistWorld;

/**@author Liahim*/
public class MistGrass extends MistDirt implements IColoredBlock, IGrowable {

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

	public static final PropertyBool GROWTH = PropertyBool.create("growth");
	public static final PropertyBool SNOWY = BlockGrass.SNOWY;

	public MistGrass(float hardness, int waterPerm) {
		super(Material.GRASS, hardness, waterPerm);
		this.setDefaultState(this.blockState.getBaseState().withProperty(HUMUS, 0).withProperty(WET, true).withProperty(GROWTH, true).withProperty(SNOWY, Boolean.valueOf(false)));
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		Block block = world.getBlockState(pos.up()).getBlock();
		return state.withProperty(SNOWY, Boolean.valueOf(block == Blocks.SNOW || block == Blocks.SNOW_LAYER));
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			boolean change = false;
			IBlockState stateUp = world.getBlockState(pos.up());
			if (stateUp.getBlockFaceShape(world, pos.up(), EnumFacing.DOWN) == BlockFaceShape.SOLID || (world.getLightFromNeighbors(pos.up()) < 4 && stateUp.getLightOpacity(world, pos.up()) > 2)) {
				if (state.getValue(GROWTH)) change = world.setBlockState(pos, state.withProperty(GROWTH, false), 2);
				else change = world.setBlockState(pos, this.getSoilBlock().getDefaultState().withProperty(HUMUS, state.getValue(HUMUS)).withProperty(WET, state.getValue(WET)), 2);
			}
			if (!change) super.updateTick(world, pos, state, rand);
		}
	}

	@Override
	public EnumFacing checkAir(World world, BlockPos pos, Random rand) {
		EnumFacing face = EnumFacing.HORIZONTALS[rand.nextInt(4)];
		BlockPos checkPos = pos.offset(face);
		if (!world.isSideSolid(checkPos, face.getOpposite()) && !world.isSideSolid(checkPos.down(), EnumFacing.UP)) return face;
		return EnumFacing.DOWN;
	}

	@Override
	public boolean doIfWet(World world, BlockPos pos, IBlockState state, Vector2f waterType, boolean fog, Random rand) {
		if (!super.doIfWet(world, pos, state, waterType, fog, rand)) {
			if (rand.nextInt(4) == 0 && world.getLightFromNeighbors(pos.up()) >= 9) {
				if (state.getValue(GROWTH)) {
					BlockPos pos1;
					IBlockState state1;
					IBlockState state1Up;
					for (int i = 0; i < 4; ++i) {
						pos1 = pos.add(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1);
						if (pos1.getY() >= 0 && pos1.getY() < 256 && !world.isBlockLoaded(pos1)) continue;
						state1 = world.getBlockState(pos1);
						state1Up = world.getBlockState(pos1.up());
						if (state1Up.getBlockFaceShape(world, pos1.up(), EnumFacing.DOWN) != BlockFaceShape.SOLID && world.getLightFromNeighbors(pos1.up()) >= 4 && state1Up.getLightOpacity(world, pos1.up()) <= 2) {
							if (!(state1.getBlock() instanceof MistGrass) && !(state1.getBlock() instanceof IFarmland) && state1.getBlock() instanceof MistSoil && state1.getValue(WET))
								SoilHelper.setGrass(world, pos1, state1, true, false, 2);
						}
					}
				} else {
					return world.setBlockState(pos, state.withProperty(GROWTH, true), 2);
				}
			}
		}
		return false;
	}

	@Override
	public boolean doIfDry(World world, BlockPos pos, IBlockState state, boolean lava, Random rand) {
		if (rand.nextInt(16) == 0 && state.getValue(GROWTH) && (lava || MistWorld.getHumi(world, pos.up(), 0) < 5)) {
			return world.setBlockState(pos, state.withProperty(GROWTH, false), 2);
		}
		return false;
	}

	@Override
	public boolean setAcid(World world, BlockPos pos, IBlockState state, int waterDist, Random rand) {
		if (waterDist < 0 && state.getValue(GROWTH)) {
			return world.setBlockState(pos, state.withProperty(GROWTH, false), 2);
		} else return world.setBlockState(pos, this.getAcidBlock(state).getDefaultState().withProperty(WET, true), 2);
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(this.getSoilBlock());
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state) % (getHumSize() * 2);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		//if (!state.getValue(GROWTH)) return layer == BlockRenderLayer.CUTOUT;
		return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(HUMUS) + (state.getValue(WET) ? 0 : getHumSize()) + (state.getValue(GROWTH) ? 0 : getHumSize() * 2);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(HUMUS, meta % getHumSize()).withProperty(WET, meta % (getHumSize() * 2) < getHumSize()).withProperty(GROWTH, meta < getHumSize() * 2);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { HUMUS, WET, GROWTH, SNOWY });
	}

	@Override
	public int getTopProtectPercent(IBlockState state, boolean isWet) {
		if (state.getValue(GROWTH)) return isWet ? 30 : 120;
		return super.getTopProtectPercent(state, isWet);
	}

	@Override
	public int getSideProtectPercent(IBlockState state, boolean isWet) {
		return super.getTopProtectPercent(state, isWet);
	}

	@Override
	public int getCloseProtectPercent(IBlockState state, boolean isWet) {
		return isWet ? 30 : 120;
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (state.getValue(GROWTH))
			return true;
		else return false;
	}

	@Override
	public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
		return state.getValue(WET) && state.getValue(GROWTH) && SoilHelper.getHumus(state) > 0;
	}

	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) { return true; }

	@Override
	public void grow(World world, Random rand, BlockPos posIn, IBlockState state) {
		IBlockState checkState;
		BlockPos pos;
		for (int i = 0; i < 128; ++i) {
			pos = posIn.up();
			int j = 0;
			while (true) {
				if (j >= i / 16) {
					if (world.isAirBlock(pos)) {
						if (rand.nextInt(8) == 0) {
							world.getBiome(pos).plantFlower(world, rand, pos);
						} else {
							checkState = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);
							if (Blocks.TALLGRASS.canBlockStay(world, pos, checkState)) {
								world.setBlockState(pos, checkState);
								if (rand.nextInt(6) > 2) {
									checkState = world.getBlockState(pos.down());
									SoilHelper.setSoil(world, pos.down(), checkState, SoilHelper.getHumus(checkState) - 1, 2);
								}
							}
						}
					}
					break;
				}
				pos = pos.add(rand.nextInt(3) - 1, (rand.nextInt(3) - 1) * rand.nextInt(3) / 2, rand.nextInt(3) - 1);
				checkState = world.getBlockState(pos.down());
				if (((!(checkState.getBlock() instanceof MistGrass) || !checkState.getValue(WET) || !checkState.getValue(GROWTH) || SoilHelper.getHumus(checkState) == 0) &&
						checkState.getBlock() != Blocks.GRASS) || world.getBlockState(pos).isNormalCube()) { break; }
				++j;
			}
		}
	}
}