package ru.liahim.mist.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.IRubberBallCollideble;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.entity.EntityRubberBall;

/**@author Liahim*/
public class MistBlockWettableFalling extends BlockFalling implements IWettable, IRubberBallCollideble {

	private final int waterPerm;
	private Block acidBlock;

	public MistBlockWettableFalling(Material material, int waterPerm) {
		super(material);
		this.waterPerm = MathHelper.clamp(waterPerm, 1, 3);
		this.setDefaultState(this.blockState.getBaseState().withProperty(WET, true));
		this.setTickRandomly(true);
		this.acidBlock = this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
		IBlockState state = getStateFromMeta(stack.getItemDamage());
		String gender = I18n.format(stack.getUnlocalizedName() + ".name");
		if (gender.length() > 2 && gender.substring(gender.length() - 2, gender.length() - 1).equals("_")) {
			gender = gender.substring(gender.length() - 2, gender.length());
		} else gender = "";
		tooltip.add(I18n.format(state.getValue(WET) ? "tile.mist.block_wet" + gender + ".tooltip" : "tile.mist.block_dry" + gender + ".tooltip"));
		if (showPorosityTooltip()) tooltip.add(I18n.format("tile.mist.soil_porosity_" + this.waterPerm + gender + ".tooltip"));
	}

	@SideOnly(Side.CLIENT)
	protected boolean showPorosityTooltip() { return true; }

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			int i = IWettable.checkFluid(world, pos);
			if (i < 0) this.setAcid(world, pos, state, 0, world.rand);
			else if (i > 0 && !state.getValue(WET)) this.setWet(world, pos, state, 0, world.rand);
		}
		state = world.getBlockState(pos);
		if (state.getBlock() instanceof IWettable) {
			checkFallable(world, pos, state, world.rand);
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (world.isRemote) return;
		IBlockState checkState = world.getBlockState(fromPos);
		if (checkState.getBlock() == MistBlocks.ACID_BLOCK) this.setAcid(world, pos, state, 0, world.rand);
		else if (checkState.getMaterial() == Material.WATER && !state.getValue(WET)) this.setWet(world, pos, state, 0, world.rand);
		state = world.getBlockState(pos);
		if (state.getBlock() instanceof IWettable) {
			checkFallable(world, pos, state, world.rand);
		}
    }

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (update(world, pos, state, rand)) {
			state = world.getBlockState(pos);
			if (state.getBlock() instanceof IWettable) {
				checkFallable(world, pos, state, rand);
			}
		}
	}

	protected void checkFallable(World world, BlockPos pos, IBlockState state, Random rand) {
		if (canFall(world, pos, state, rand)) {
			int i = 32;
			if (!fallInstantly && world.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
				if (!world.isRemote) {
					EntityFallingBlock entityfallingblock = new EntityFallingBlock(world, pos.getX() + 0.5D,
							pos.getY(), pos.getZ() + 0.5D, state);
					this.onStartFalling(entityfallingblock);
					world.spawnEntity(entityfallingblock);
				}
			} else {
				world.setBlockToAir(pos);
				BlockPos blockpos;

				for (blockpos = pos.down(); (world.isAirBlock(blockpos) || canFallThrough(world
						.getBlockState(blockpos))) && blockpos.getY() > 0; blockpos = blockpos.down()) {
					;
				}
				if (blockpos.getY() > 0) {
					world.setBlockState(blockpos.up(), state); // Forge: Fix loss of state information during world gen.
				}
			}
		}
	}

	protected boolean canFall(World world, BlockPos pos, IBlockState state, Random rand) {
		return (world.isAirBlock(pos.down()) || canFallThrough(world.getBlockState(pos.down()))) && pos.getY() >= 0 &&
				(!state.getValue(WET) || rand.nextInt(8) == 0 || checkNeighbor(world, pos));
	}

	protected boolean checkNeighbor(World world, BlockPos pos) {
		BlockPos checkPos;
		IBlockState state;
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			checkPos = pos.offset(side);
			state = world.getBlockState(checkPos);
			if (state.isSideSolid(world, pos, side.getOpposite())) {
				if (state.getBlock() instanceof BlockFalling) {
					if (state.getBlock() instanceof IWettable ? state.getValue(WET) : false) {
						if (!world.isAirBlock(checkPos.down()) && !canFallThrough(world.getBlockState(checkPos.down()))) {
							return false;
						}
					}
				} else {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean setDry(World world, BlockPos pos, IBlockState state, Random rand) {
		return world.setBlockState(pos, state.withProperty(WET, false));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		if (!state.getValue(WET) && rand.nextInt(16) == 0) {
			BlockPos blockpos = pos.down();
			if (canFallThrough(world.getBlockState(blockpos))) {
				double d0 = pos.getX() + rand.nextFloat();
				double d1 = pos.getY() - 0.05D;
				double d2 = pos.getZ() + rand.nextFloat();
				world.spawnParticle(EnumParticleTypes.FALLING_DUST, d0, d1, d2, 0.0D, 0.0D, 0.0D, Block.getStateId(state));
			}
		}
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(WET) ? 0 : 1;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(WET, meta == 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { WET });
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	/** The quantity (1-3) of block water absorption. It affects:
	 *  the speed of wetting and drying of the block;
	 *  the maximum distance of water seepage;
	 *  leaching from the soil humus.*/
	@Override
	public int getWaterPerm(IBlockState state) {
		return waterPerm;
	}

	@Override
	public void setAcidBlock(Block acidBlock) {
		this.acidBlock = acidBlock;
	}

	@Override
	public Block getAcidBlock(IBlockState state) {
		return this.acidBlock;
	}

	@Override
	public boolean isAcid() {
		return false;
	}

	@Override
	public boolean isCollide(World world, IBlockState state, EntityRubberBall ball, RayTraceResult result, Random rand) {
		this.checkFallable(world, result.getBlockPos(), state, rand);
		return true;
	}
}