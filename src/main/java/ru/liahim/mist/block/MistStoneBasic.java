package ru.liahim.mist.block;

import ru.liahim.mist.api.block.IMistStoneBasic;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class MistStoneBasic extends MistBlock implements IMistStoneBasic {

	public MistStoneBasic() {
		super(Material.ROCK, MapColor.GRAY);
		this.setHardness(800.0F);
		this.setResistance(8000);
		this.setHarvestLevel("pickaxe", 3);
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}
}