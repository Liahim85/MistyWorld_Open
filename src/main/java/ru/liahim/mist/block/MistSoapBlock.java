package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.init.ModParticle;
import ru.liahim.mist.util.FacingHelper;
import ru.liahim.mist.world.MistWorld;

public class MistSoapBlock extends MistBlock {

	public MistSoapBlock() {
		super(Material.CLAY, MapColor.SILVER);
		this.setHardness(0.6F);
	}

	@Override
	public float getSlipperiness(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity entity) {
		return world instanceof World && MistWorld.isPosInFog((World)world, pos.getY() + 2) ? 0.7F : 0.99F;
	}

	/*@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {
		if (!world.isRemote && entity.isSprinting() && world.rand.nextBoolean() && !MistWorld.isPosInFog(world, pos.getY() + 2)) {
			float size = entity.width;
			double x = world.rand.nextDouble() * size - size/2;
			double y = world.rand.nextDouble() * 0.2 + 0.1;
			double z = world.rand.nextDouble() * size - size/2;
			((WorldServer)world).spawnParticle(ModParticle.MIST_BUBBLE, entity.posX, entity.posY, entity.posZ, 1, x, y, z, 0.1, new int[0]);
		}
	}*/

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		if (rand.nextBoolean() && world.isRainingAt(pos.up()) && !MistWorld.isPosInFog(world, pos.getY() + 2)) {
			double x = pos.getX() + world.rand.nextDouble();
			double y = pos.getY() + world.rand.nextDouble() * 0.2 + 1.05;
			double z = pos.getZ() + world.rand.nextDouble();
			world.spawnParticle(ModParticle.MIST_BUBBLE, x, y, z, 0, -world.rand.nextFloat() * 0.2F, 0);
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			for (EnumFacing face : FacingHelper.NOTDOWN) {
				if (world.getBlockState(pos.offset(face)).getBlock() == MistBlocks.ACID_BLOCK) {
					world.setBlockState(pos, this.getDefaultState());
					break;
				}
			}
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isRemote && fromPos.getY() >= pos.getY() && world.getBlockState(fromPos).getBlock() == MistBlocks.ACID_BLOCK) {
			world.setBlockState(pos, this.getDefaultState());
		}
	}
}