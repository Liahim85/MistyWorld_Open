package ru.liahim.mist.block;

import ru.liahim.mist.api.block.IMistStoneUpper;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class MistOreUpper extends MistOre implements IMistStoneUpper {
	
	public MistOreUpper(float hardness, float resistance, int harvestLevel, MapColor color) {
		super(hardness, resistance, harvestLevel, color);
	}

	public MistOreUpper(float hardness, float resistance, int harvestLevel) {
		this(hardness, resistance, harvestLevel, Material.ROCK.getMaterialMapColor());
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}
}