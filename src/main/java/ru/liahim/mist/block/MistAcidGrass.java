package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.init.BlockColoring;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MistAcidGrass extends MistAcidDirt implements IColoredBlock {

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColor() {
		return BlockColoring.DOWN_GRASS_COLORING;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return BlockColoring.BLOCK_ITEM_COLORING;
	}

	public MistAcidGrass(Material material, float hardness, int waterPerm) {
		super(material, hardness, waterPerm);
	}

	public MistAcidGrass(float hardness, int waterPerm) {
		super(Material.GRASS, hardness, waterPerm);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			boolean change = false;
			IBlockState stateUp = world.getBlockState(pos.up());
			if (stateUp.getBlock().isNormalCube(stateUp, world, pos.up()) || (world.getLightFromNeighbors(pos.up()) < 4 && stateUp.getLightOpacity(world, pos.up()) > 2)) {
				change = world.setBlockState(pos, this.getSoilBlock().getDefaultState().withProperty(WET, state.getValue(WET)), 2);
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
		if (fog || waterType.getX() < 0) {
			if (rand.nextInt(4) == 0 && world.getLightFromNeighbors(pos.up()) >= 9) {
				BlockPos pos1;
				IBlockState state1;
				IBlockState state1Up;
				for (int i = 0; i < 4; ++i) {
					pos1 = pos.add(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1);
					if (pos1.getY() >= 0 && pos1.getY() < 256 && !world.isBlockLoaded(pos1)) continue;
					state1 = world.getBlockState(pos1);
					state1Up = world.getBlockState(pos1.up());
					if (!state1Up.getBlock().isNormalCube(state1Up, world, pos1.up()) && world.getLightFromNeighbors(pos1.up()) >= 4 && state1Up.getLightOpacity(world, pos1.up()) <= 2) {
						if (!(state1.getBlock() instanceof MistAcidGrass) && state1.getBlock() instanceof MistAcidSoil && state1.getValue(WET)) {
							world.setBlockState(pos1, ((MistAcidSoil)state1.getBlock()).getGrassBlock().getDefaultState(), 2);
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean doIfDry(World world, BlockPos pos, IBlockState state, boolean lava, Random rand) {
		if (rand.nextInt(4) == 0 && (lava || MistWorld.getHumi(world, pos.up(), 0) < 30)) world.setBlockState(pos, this.getSoilBlock().getDefaultState().withProperty(WET, false), 2);
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);
		if (state.getValue(WET) && rand.nextInt(10) == 0) {
			world.spawnParticle(EnumParticleTypes.TOWN_AURA, pos.getX() + rand.nextFloat(), pos.getY() + 1.1F, pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D, new int[0]);
		}
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(this.getSoilBlock());
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

	@Override
	public int getTopProtectPercent(IBlockState state, boolean isWet) {
		return isWet ? 30 : 120;
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
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return MapColor.PURPLE_STAINED_HARDENED_CLAY;
	}
}