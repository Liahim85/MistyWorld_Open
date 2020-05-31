package ru.liahim.mist.api.registry;

import ru.liahim.mist.api.block.IWettable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;

public interface IMistHarvest {

	public HarvestType getHarvestType();

	public static HarvestType getHarvestType(Block block) {
		if (block instanceof BlockCrops) {
			if (block == Blocks.POTATOES) return HarvestType.WP2_3;
			if (block == Blocks.CARROTS) return HarvestType.WP3_3;
			if (block == Blocks.BEETROOTS) return HarvestType.WP2_3;
			if (block == Blocks.WHEAT) return HarvestType.WP1_2;
			if (block == Blocks.MELON_STEM) return HarvestType.WP3_3;
			if (block == Blocks.PUMPKIN_STEM) return HarvestType.WP3_3;
		} else if (block instanceof IMistHarvest) {
			return ((IMistHarvest)block).getHarvestType();
		}
		return MistRegistry.getHarvestType(block);
	}

	public static boolean isSoilSuitable(Block crop, IBlockState soil) {
		if (soil.getBlock() instanceof IWettable) {
			int i = ((IWettable)soil.getBlock()).getWaterPerm(soil);
			HarvestType type = IMistHarvest.getHarvestType(crop);
			return i >= type.getMinWaterPerm() && i <= type.getMaxWaterPerm();
		}
		return false;
	}

	public static enum HarvestType {

		WP1_1(1, 1),	//Marsh plants
		WP1_2(1, 2),	//Cereals
		WP1_3(1, 3),	//Common
		WP2_2(2, 2),	//Vegetables
		WP2_3(2, 3),	//Tubers
		WP3_3(3, 3);	//Gourds, Carrots
		
		private final int minWaterPerm;
		private final int maxWaterPerm;

		private HarvestType(int minWaterPerm, int maxWaterPerm) {
			this.minWaterPerm = MathHelper.clamp(minWaterPerm, 1, 3);
			this.maxWaterPerm = MathHelper.clamp(maxWaterPerm, 1, 3);
		}

		public int getMinWaterPerm() {
			return this.minWaterPerm;
		}

		public int getMaxWaterPerm() {
			return this.maxWaterPerm;
		}
	}
}