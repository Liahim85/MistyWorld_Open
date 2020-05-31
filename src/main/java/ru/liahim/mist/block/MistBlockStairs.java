package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.IDividable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.util.FacingHelper;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MistBlockStairs extends BlockStairs implements IDividable {

	private final Block full_Block;

	public MistBlockStairs(IBlockState modelState, boolean tick) {
		super(modelState);
		this.useNeighborBrightness = true;
		this.full_Block = modelState.getBlock();
		this.setTickRandomly(tick);
	}

	public MistBlockStairs(IBlockState modelState) {
		this(modelState, false);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public Block getFullBlock() {
		return this.full_Block;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			if (this == MistBlocks.COBBLESTONE_MOSS_STAIRS) {
				if (rand.nextInt(4) == 0 && MistWorld.isPosInFog(world, pos.getY())) {
					world.setBlockState(pos, this.getDefaultState());
				}
			} else if (this == MistBlocks.COBBLESTONE_STAIRS && rand.nextInt(500) == 0 &&
				!MistWorld.isPosInFog(world, pos.getY()) && world.getBiome(pos).getRainfall() >= 0.3F) {
				for (EnumFacing side : FacingHelper.NOTDOWN) {
					if (world.getBlockState(pos.offset(side)).getBlock() == MistBlocks.ACID_BLOCK) {
						return;
					}
				}
				boolean check = state.getValue(HALF) != EnumHalf.BOTTOM && world.getBlockState(pos.down()).getBlock() instanceof MistGrass;
				if (!check && !world.isSideSolid(pos.up(), EnumFacing.DOWN)) {
					for (EnumFacing side : EnumFacing.HORIZONTALS) {
						if (world.getBlockState(pos.offset(side)).getBlock() instanceof MistGrass) {
							check = true;
							break;
						}
					}
				}
				if (!check) {
					for (EnumFacing side : EnumFacing.HORIZONTALS) {
						if (!world.isSideSolid(pos.offset(side), side.getOpposite()) && world.getBlockState(pos.offset(side).down()).getBlock() instanceof MistGrass) {
							check = true;
							break;
						}
					}
				}
				if (check) world.setBlockState(pos, MistBlocks.COBBLESTONE_MOSS_STAIRS.getDefaultState().withProperty(FACING, state.getValue(FACING)).withProperty(HALF, state.getValue(HALF)).withProperty(SHAPE, state.getValue(SHAPE)));
			}
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote && this == MistBlocks.COBBLESTONE_MOSS_STAIRS) {
			for (EnumFacing side : FacingHelper.NOTDOWN) {
				if (world.getBlockState(pos.offset(side)).getBlock() == MistBlocks.ACID_BLOCK) {
					world.setBlockState(pos, MistBlocks.COBBLESTONE_STAIRS.getDefaultState().withProperty(FACING, state.getValue(FACING)).withProperty(HALF, state.getValue(HALF)).withProperty(SHAPE, state.getValue(SHAPE)));
					break;
				}
			}
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isRemote && this == MistBlocks.COBBLESTONE_MOSS_STAIRS) {
			for (EnumFacing side : FacingHelper.NOTDOWN) {
				if (world.getBlockState(pos.offset(side)).getBlock() == MistBlocks.ACID_BLOCK) {
					world.setBlockState(pos, MistBlocks.COBBLESTONE_STAIRS.getDefaultState().withProperty(FACING, state.getValue(FACING)).withProperty(HALF, state.getValue(HALF)).withProperty(SHAPE, state.getValue(SHAPE)));
					break;
				}
			}
		}
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random) {
		return 3;
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return this.getStepBlock(state).getItemDropped(state, rand, fortune);
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.full_Block.getFlammability(world, pos, face);
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.full_Block.getFireSpreadSpeed(world, pos, face);
	}
}