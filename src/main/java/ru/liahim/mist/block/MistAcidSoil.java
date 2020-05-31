package ru.liahim.mist.block;

import ru.liahim.mist.api.block.IMistSoil;
import ru.liahim.mist.common.Mist;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IPlantable;

/**@author Liahim*/
public abstract class MistAcidSoil extends MistBlockWettable implements IMistSoil {

	private Block soilBlock;
	private Block grassBlock;

	public MistAcidSoil(Material material, float hardness, int waterPerm) {
		super(material, waterPerm);
		this.setHardness(hardness);
		this.setSoundType(material == Material.GRASS ? SoundType.PLANT : SoundType.GROUND);
		this.setHarvestLevel("shovel", 0);
		this.soilBlock = this;
		this.grassBlock = this;
	}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction,
		IPlantable plantable) {
		return plantable.getPlantType(world, pos) == Mist.MIST_DOWN_PLANT;
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

	@Override
	public boolean isAcid() {
		return true;
	}
}