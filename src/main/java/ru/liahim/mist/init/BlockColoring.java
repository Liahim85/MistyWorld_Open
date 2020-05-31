package ru.liahim.mist.init;

import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.world.biome.BiomeMist;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockColoring {

	public static final IBlockColor FOLIAGE_COLORING = new IBlockColor() {
		@Override
		@SideOnly(Side.CLIENT)
		public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
			return world != null && pos != null ? BiomeColorHelper.getFoliageColorAtPos(world, pos)
				: ColorizerFoliage.getFoliageColorBasic();
		}
	};

	public static final IBlockColor GRASS_COLORING_0 = new IBlockColor() {
		@Override
		@SideOnly(Side.CLIENT)
		public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
			return world != null && pos != null ? tintIndex == 0 ? BiomeColorHelper.getGrassColorAtPos(world, pos)
				: 0xFFFFFFFF : ColorizerGrass.getGrassColor(0.5D, 1.0D);
		}
	};

	public static final IBlockColor GRASS_COLORING_1 = new IBlockColor() {
		@Override
		@SideOnly(Side.CLIENT)
		public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
			return world != null && pos != null ? tintIndex == 1 ? BiomeColorHelper.getGrassColorAtPos(world, pos)
				: 0xFFFFFFFF : ColorizerGrass.getGrassColor(0.5D, 1.0D);
		}
	};

	public static final IBlockColor DOWN_GRASS_COLORING = new IBlockColor() {
		@Override
		@SideOnly(Side.CLIENT)
		public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
			return world != null && pos != null && tintIndex == 1 ? getDownGrassColor(world, pos) : 0xC8C8C8;
		}
	};

	public static final IItemColor BLOCK_ITEM_COLORING = new IItemColor() {
		@Override
		@SideOnly(Side.CLIENT)
		public int colorMultiplier(ItemStack stack, int tintIndex) {
			if (stack.getItem() instanceof ItemBlock) {
				IBlockState state = ((ItemBlock)stack.getItem()).getBlock().getStateFromMeta(stack.getMetadata());
				IBlockColor blockColor = ((IColoredBlock)state.getBlock()).getBlockColor();
				return blockColor == null ? 0xFFFFFF : blockColor.colorMultiplier(state, null, null, tintIndex);
			}
			return 0xFFFFFF;
		}
	};

	public static int getDownGrassColor(IBlockAccess blockAccess, BlockPos pos) {
		int r = 0, g = 0, b = 0;
		Biome biome;
		int r0 = 175, g0 = 205, b0 = 200;
		int rt = 205, gt = 200, bt = 175;
		int rh = 200, gh = 200, bh = 200;
        for (BlockPos.MutableBlockPos checkPos : BlockPos.getAllInBoxMutable(pos.add(-1, 0, -1), pos.add(1, 0, 1))) {
        	biome = blockAccess.getBiome(checkPos);
        	if (biome instanceof BiomeMist && ((BiomeMist)biome).getDownGrassColor() >= 0) {
        		int i = ((BiomeMist)biome).getDownGrassColor();
        		r += i >> 16 & 255;
        		g += i >> 8 & 255;
        		b += i & 255;
        	} else {
	    		float temp = MathHelper.clamp(biome.getTemperature(pos) * 0.4F + 0.2F, 0.0F, 1.0F);
	    		float humi = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
	    		float w = temp - humi;
	            r += ((r0 + (rt - r0) * temp) * (w + 1) - (r0 + (rh - r0) * humi) * (w - 1)) / 2;
	            g += ((g0 + (gt - g0) * temp) * (w + 1) - (g0 + (gh - g0) * humi) * (w - 1)) / 2;
	            b += ((b0 + (bt - b0) * temp) * (w + 1) - (b0 + (bh - b0) * humi) * (w - 1)) / 2;
        	}
        }
        return (r / 9) << 16 | (g / 9) << 8 | b / 9;
    }
}