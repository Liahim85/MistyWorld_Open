package ru.liahim.mist.api.block;

import java.util.Random;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import ru.liahim.mist.block.MistBlockWettable;
import ru.liahim.mist.util.FacingHelper;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSponge;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**@author Liahim*/
public interface IWettable {

	public static final PropertyBool WET = PropertyBool.create("wet");

	/**Open soil:				dries at <50%, gets wet at >100%,
	 * grass and closed soil:	dries at <30%, gets wet at >120%,
	 * open farmland:			dries at <50%, gets wet at >100%,
	 * mulched farmland:		dries at <10%, gets wet at >140%,
	 * sand:					dries at <60%, gets wet at >145%,
	 * clay:					dries at <30%, gets wet at >120%,
	 * floating mat:			dries at <30%,
	 * grass:					dies at <5% */
	public default boolean update(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			boolean fog = MistWorld.isPosInFog(world, pos.up(2));
			boolean lava = checkLava(world, pos);
			boolean rain = isRainUp(world, pos.up(), rand);
			boolean rand2 = isChance(state, rand);
			Vector2f waterType = checkFluidDist(world, pos, EnumFacing.UP, getWaterPerm(state), 0);
			int waterDist = (int)waterType.getY();
			if (!state.getValue(WET)) {
				boolean checkWet = false;
				if (waterDist == 0 || rain || (!lava && (waterDist > 0 || (fog && rand2)))) checkWet = true;
				else if (rand2 && !lava) {
					IBlockState stateUp = world.getBlockState(pos.up());
					if (isUpWet(stateUp)) checkWet = true;
					else {
						boolean isClose = true;
						if (!world.isSideSolid(pos.up(), EnumFacing.DOWN)) {
							isClose = false;
							if (MistWorld.getHumi(world, pos.up(), 0) >= getTopProtectPercent(state, false)) {
								checkWet = true;
							}
						}
						if (!checkWet && (isClose || getTopProtectPercent(state, false) > getSideProtectPercent(state, false))) {
							EnumFacing side = checkAir(world, pos, rand);
							if (side != EnumFacing.DOWN) {
								isClose = false;
								if (MistWorld.getHumi(world, pos.offset(side), 0) >= getSideProtectPercent(state, false)) {
									checkWet = true;
								}
							}
						}
						if (isClose && MistWorld.getHumi(world, pos, 0) >= getCloseProtectPercent(state, false)) {
							checkWet = true;
						}
					}
				}
				if (!checkWet) return doIfDry(world, pos, state, lava, rand);
				else if ((fog || waterType.getX() < 0) && state.getBlock() != getAcidBlock(state)) return setAcid(world, pos, state, waterDist, rand);
				else return setWet(world, pos, state, waterDist, rand);
			} else {
				boolean checkDry = false;
				if (waterDist == 0 || rain || (!lava && (waterDist > 0 || (fog && rand2)))) {}
				else if (rand2) {
					if (lava) checkDry = true;
					else if (!world.isRaining() && !isUpWet(world.getBlockState(pos.up()))) {
						boolean isClose = true;
						if (!world.isSideSolid(pos.up(), EnumFacing.DOWN)) {
							isClose = false;
							if (MistWorld.getHumi(world, pos.up(), 0) <= getTopProtectPercent(state, true)) {
								checkDry = true;
							}
						}
						if (!checkDry && (isClose || getTopProtectPercent(state, true) < getSideProtectPercent(state, true))) {
							EnumFacing side = checkAir(world, pos, rand);
							if (side != EnumFacing.DOWN) {
								isClose = false;
								if (MistWorld.getHumi(world, pos.offset(side), 0) <= getSideProtectPercent(state, true)) {
									checkDry = true;
								}
							}
						}
						if (isClose && MistWorld.getHumi(world, pos, 0) <= getCloseProtectPercent(state, true)) {
							checkDry = true;
						}
					}
				}
				if (checkDry) return setDry(world, pos, state, rand);
				else if ((fog || waterType.getX() < 0) && state.getBlock() != getAcidBlock(state)) return setAcid(world, pos, state, waterDist, rand);
				else return doIfWet(world, pos, state, waterType, fog, rand);
			}
		}
		return false;
	}

	/** Returns a random chance associated with water permeability. */
	public default boolean isChance(IBlockState state, Random rand) {
		return rand.nextInt((5 - getWaterPerm(state)) * 2) == 0;
	}

	public default boolean isRainUp(World world, BlockPos pos, Random rand) {
		return world.isRainingAt(pos);
	}

	public default boolean isUpWet(IBlockState stateUp) {
		Material matUp = stateUp.getMaterial();
		return matUp == Material.SNOW || matUp == Material.CRAFTED_SNOW || matUp == Material.ICE || stateUp == Blocks.SPONGE.getDefaultState().withProperty(BlockSponge.WET, true);
	}

	public default EnumFacing checkAir(World world, BlockPos pos, Random rand) {
		EnumFacing face = EnumFacing.HORIZONTALS[rand.nextInt(4)];
		if (!world.isSideSolid(pos.offset(face), face.getOpposite())) return face;
		return EnumFacing.DOWN;
	}

	public default boolean doIfWet(World world, BlockPos pos, IBlockState state, Vector2f waterType, boolean fog, Random rand) {
		return false;
	}

	public default boolean doIfDry(World world, BlockPos pos, IBlockState state, boolean lava, Random rand) {
		return false;
	}

	public default boolean setWet(World world, BlockPos pos, IBlockState state, int waterDist, Random rand) {
		return world.setBlockState(pos, state.withProperty(WET, true), 2);
	}

	public default boolean setDry(World world, BlockPos pos, IBlockState state, Random rand) {
		return world.setBlockState(pos, state.withProperty(WET, false), 2);
	}

	public default boolean setAcid(World world, BlockPos pos, IBlockState state, int waterDist, Random rand) {
		if (getAcidBlock(state) == null) return false;
		if (getAcidBlock(state) instanceof IWettable) return world.setBlockState(pos, getAcidBlock(state).getDefaultState().withProperty(WET, true), 2);
		return world.setBlockState(pos, getAcidBlock(state).getDefaultState(), 2);
	}

	public static boolean checkWater(World world, BlockPos pos, EnumFacing direct) {
		BlockPos checkPos;
		for (EnumFacing face : FacingHelper.NOTDOWN) {
			if (face.getOpposite() != direct) {
				checkPos = pos.offset(face);
				if (world.isBlockLoaded(checkPos) && world.getBlockState(checkPos).getMaterial() == Material.WATER &&
						world.getBlockState(checkPos).getBlock() != MistBlocks.ACID_BLOCK)
					return true;
			}
		}
		return false;
	}

	public static boolean checkWater(World world, BlockPos pos) {
		return checkWater(world, pos, EnumFacing.UP);
	}

	public static boolean checkAcid(World world, BlockPos pos, EnumFacing direct) {
		BlockPos checkPos;
		for (EnumFacing face : FacingHelper.NOTDOWN) {
			if (face.getOpposite() != direct) {
				checkPos = pos.offset(face);
				if (world.isBlockLoaded(checkPos) && world.getBlockState(checkPos).getBlock() == MistBlocks.ACID_BLOCK)
					return true;
			}
		}
		return false;
	}

	public static boolean checkAcid(World world, BlockPos pos) {
		return checkAcid(world, pos, EnumFacing.UP);
	}

	public static boolean checkLava(World world, BlockPos pos) {
		BlockPos checkPos;
		for (EnumFacing face : EnumFacing.VALUES) {
			checkPos = pos.offset(face);
			if (world.isBlockLoaded(checkPos) && world.getBlockState(checkPos).getMaterial() == Material.LAVA)
				return true;
		}
		return false;
	}

	/**Returns: -1 - Acid; 0 - Nothing; 1 - Water. The priority of acid is greater than the priority of water.*/
	public static int checkFluid(World world, BlockPos pos, EnumFacing direct) {
		int i = 0;
		BlockPos checkPos;
		for (EnumFacing face : FacingHelper.NOTDOWN) {
			if (face.getOpposite() != direct) {
				checkPos = pos.offset(face);
				if (world.isBlockLoaded(checkPos)) {
					if (world.getBlockState(checkPos).getBlock() == MistBlocks.ACID_BLOCK) {
						return -1;
					} else if (world.getBlockState(checkPos).getMaterial() == Material.WATER) {
						i = 1;
					}
				}
			}
		}
		return i;
	}

	/**Returns: -1 - Acid; 0 - Nothing; 1 - Water. The priority of acid is greater than the priority of water.*/
	public static int checkFluid(World world, BlockPos pos) {
		return checkFluid(world, pos, EnumFacing.UP);
	}

	/**Returns Vector2f:
	 * Vector2f.getX() - Fluid type: -1 - acid; 0 - nothing; 1 - water;
	 * Vector2f.getY() - Fluid distance: -1 - far; 0 - 2 - distance in block.*/
	public static Vector2f checkFluidDist(World world, BlockPos pos, EnumFacing direct, @Nullable int waterPerm, int dist) {
		Vector2f fluidDist = new Vector2f(0, -1);
		IBlockState state = world.getBlockState(pos);
		if (waterPerm == 0 && state.getBlock() instanceof IWettable) waterPerm = ((IWettable)state.getBlock()).getWaterPerm(state);
		waterPerm = MathHelper.clamp(waterPerm, 1, 3);
		int i = canTransitFluid(state);
		if (dist == 0 || (dist < 3 && waterPerm > dist && i != 0)) {
			int fluidType = checkFluid(world, pos, direct);
			if (fluidType != 0 && (dist == 0 || i == 2 || i == fluidType)) fluidDist.set(fluidType, dist);
			else if (waterPerm > dist + 1) {
				float a = -1;
				float w = -1;
				fluidDist = checkFluidDist(world, pos.up(), EnumFacing.UP, 0, dist + 1);
				if (fluidDist.getX() < 0) a = fluidDist.getY();
				else if (fluidDist.getX() > 0) w = fluidDist.getY();
				BlockPos checkPos;
				for (EnumFacing face : EnumFacing.HORIZONTALS) {
					checkPos = pos.offset(face);
					if (direct != face.getOpposite() && world.isBlockLoaded(checkPos)) {
						fluidDist = checkFluidDist(world, checkPos, face, 0, dist + 1);
						if (fluidDist.getX() < 0) a = a < 0 ? fluidDist.getY() : Math.min(a, fluidDist.getY());
						else if (fluidDist.getX() > 0) w = w < 0 ? fluidDist.getY() : Math.min(w, fluidDist.getY());
					}
				}
				if (a > 0 && (dist == 0 || i < 0 || i == 2)) fluidDist.set(-1, a);
				else if (w > 0) fluidDist.set(1, w);
				else fluidDist.set(0, -1);
			}
		}
		return fluidDist;
	}

	public static int checkWaterDist(World world, BlockPos pos, EnumFacing direct, @Nullable int waterPerm, int dist) {
		int waterDist = -1;
		IBlockState state = world.getBlockState(pos);
		if (waterPerm == 0 && state.getBlock() instanceof MistBlockWettable) waterPerm = ((MistBlockWettable)state.getBlock()).getWaterPerm(state);
		waterPerm = MathHelper.clamp(waterPerm, 1, 3);
		if (dist == 0 || (dist < 3 && waterPerm > dist && canTransitWater(state))) {
			if (checkWater(world, pos, direct))
				waterDist = dist;
			else if (waterPerm > dist + 1) {
				waterDist = checkWaterDist(world, pos.up(), EnumFacing.UP, 0, dist + 1);
				BlockPos checkPos;
				for (EnumFacing face : EnumFacing.HORIZONTALS) {
					checkPos = pos.offset(face);
					if (waterDist < 0 && direct != face.getOpposite() && world.isBlockLoaded(checkPos))
						waterDist = checkWaterDist(world, checkPos, face, 0, dist + 1);
				}
			}
		}
		return waterDist;
	}

	public static int checkAcidDist(World world, BlockPos pos, EnumFacing direct, @Nullable int waterPerm, int dist) {
		int acidDist = -1;
		IBlockState state = world.getBlockState(pos);
		if (waterPerm == 0 && state.getBlock() instanceof MistBlockWettable) waterPerm = ((MistBlockWettable)state.getBlock()).getWaterPerm(state);
		waterPerm = MathHelper.clamp(waterPerm, 1, 3);
		if (dist == 0 || (dist < 3 && waterPerm > dist && canTransitAcid(state))) {
			if (checkAcid(world, pos, direct))
				acidDist = dist;
			else if (waterPerm > dist + 1) {
				acidDist = checkAcidDist(world, pos.up(), EnumFacing.UP, 0, dist + 1);
				BlockPos checkPos;
				for (EnumFacing face : EnumFacing.HORIZONTALS) {
					checkPos = pos.offset(face);
					if (acidDist < 0 && direct != face.getOpposite() && world.isBlockLoaded(checkPos))
						acidDist = checkAcidDist(world, checkPos, face, 0, dist + 1);
				}
			}
		}
		return acidDist;
	}

	public static boolean canTransitWater(IBlockState state) {
		if (state.getBlock() instanceof IWettable && ((IWettable)state.getBlock()).isAcid()) return false;
		if (state.getBlock() instanceof BlockSand) return true;
		if (state.getBlock() instanceof BlockGravel) return true;
		return state.getBlock() instanceof IWettable && ((IWettable)state.getBlock()).getWaterPerm(state) > 1 && state.getValue(WET);
	}

	public static boolean canTransitAcid(IBlockState state) {
		if (state.getBlock() instanceof BlockSand) return true;
		if (state.getBlock() instanceof BlockGravel) return true;
		return state.getBlock() instanceof IWettable && ((IWettable)state.getBlock()).isAcid() && ((IWettable)state.getBlock()).getWaterPerm(state) > 1 && state.getValue(WET);
	}

	/**Returns: -1 - Acid; 0 - Nothing; 1 - Water; 2 - Both.*/
	public static int canTransitFluid(IBlockState state) {
		if (state.getBlock() instanceof BlockSand) return 2;
		if (state.getBlock() instanceof BlockGravel) return 2;
		if (state.getBlock() instanceof IWettable && ((IWettable)state.getBlock()).getWaterPerm(state) > 1 && state.getValue(WET)) {
			if (state.getBlock() instanceof IWettable && ((IWettable)state.getBlock()).isAcid()) return -1;
			else return 1;
		} else return 0;
	}

	/** The quantity (1-3) of block water absorption. It affects:
	 *  the speed of wetting and drying of the block;
	 *  the maximum distance of water seepage.*/
	public int getWaterPerm(IBlockState state);

	/** Gets the min/max moisture percentage required for wetting/drying block from top.*/
	public default int getTopProtectPercent(IBlockState state, boolean isWet) {
		return isWet ? 50 : 100;
	}

	/** Gets the min/max moisture percentage required for wetting/drying block from side.*/
	public default int getSideProtectPercent(IBlockState state, boolean isWet) {
		return getTopProtectPercent(state, isWet);
	}

	/** Gets the min/max moisture percentage required for wetting/drying the closed block.*/
	public default int getCloseProtectPercent(IBlockState state, boolean isWet) {
		return isWet ? getTopProtectPercent(state, true) - 20 : getTopProtectPercent(state, false) + 20;
	}

	public boolean isAcid();

	public void setAcidBlock(Block acidBlock);

	public Block getAcidBlock(IBlockState state);
}