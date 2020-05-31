package ru.liahim.mist.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import ru.liahim.mist.api.block.IMistSoil;

/**@author Liahim*/
public abstract class MistSoil extends MistBlockWettable implements IMistSoil {

	private Block soilBlock;
	private Block grassBlock;
	private Block farmBlock;

	public MistSoil(Material material, float hardness, int waterPerm) {
		super(material, waterPerm);
		this.setHardness(hardness);
		this.setSoundType(material == Material.GRASS ? SoundType.PLANT : SoundType.GROUND);
		this.setHarvestLevel("shovel", 0);
		this.soilBlock = this;
		this.grassBlock = this;
		this.farmBlock = this;
	}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction,
		IPlantable plantable) {
		IBlockState plant = plantable.getPlant(world, pos.offset(direction));
		if (plant.getBlock() == Blocks.DEADBUSH) return true;
		EnumPlantType plantType = plantable.getPlantType(world, pos.offset(direction));
		switch (plantType) {
		case Plains:
		case Cave:
			return true;
		case Beach:
			return (world.getBlockState(pos.east()).getMaterial() == Material.WATER ||
					world.getBlockState(pos.west()).getMaterial() == Material.WATER ||
					world.getBlockState(pos.north()).getMaterial() == Material.WATER || world.getBlockState(pos.south()).getMaterial() == Material.WATER);
		default:
			return false;
		}
	}

	@Override
	public void setSoilBlock(Block soilBlock) {
		this.soilBlock = soilBlock;
	}

	@Override
	public Block getSoilBlock() {
		return this.soilBlock;
	}

	@Override
	public void setGrassBlock(Block grassBlock) {
		this.grassBlock = grassBlock;
	}

	@Override
	public Block getGrassBlock() {
		return this.grassBlock;
	}

	public void setFarmBlock(Block farmBlock) {
		this.farmBlock = farmBlock;
	}

	public Block getFarmBlock() {
		return this.farmBlock;
	}

	public IBlockState getFarmState(IBlockState state) {
		return this.getFarmBlock().getDefaultState().withProperty(WET, state.getValue(WET));
	}
}