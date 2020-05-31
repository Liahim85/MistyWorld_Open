package ru.liahim.mist.init;

import java.util.HashMap;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.item.food.ItemMistSoup;
import ru.liahim.mist.util.ItemStackMapKey;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemColoring {

	private static int[][] data;
	private static int color, red, green, blue;
	private static final HashMap<ItemStackMapKey, Integer> foodColorList = Maps.<ItemStackMapKey, Integer>newHashMap();

	public static final IItemColor ITEM_ARMOR_COLORING = new IItemColor() {
		@Override
		public int colorMultiplier(ItemStack stack, int tintIndex) {
			return stack.getItem() instanceof ItemArmor && tintIndex == 0 ? ((ItemArmor)stack.getItem()).getColor(stack) : 0xFFFFFF;
		}
	};

	public static final IItemColor ITEM_SOUP_COLORING = new IItemColor() {
		@Override
		public int colorMultiplier(ItemStack stack, int tintIndex) {
			if (tintIndex > 0 && stack.getItem() instanceof ItemMistSoup) {
				ItemStack[] mainFood = ((ItemMistSoup) stack.getItem()).getFoodColors(stack);
				float[] percents = ((ItemMistSoup) stack.getItem()).getFoodPercent(stack);
				if (mainFood != null) {
					int[] colors = new int[5];
					int i = 0;
					for (ItemStack food : mainFood) {
						if (!food.isEmpty()) {
							colors[i] = ItemColoring.getFoodColor(food);
						} else break;
						++i;
					}
					if (i > 0) {
						if (tintIndex <= i && colors[tintIndex - 1] != 0) return colors[tintIndex - 1];
						float red = 0, green = 0, blue = 0;
						float count = 0;
						for (int f = 0; f < i; ++f) {
							if (colors[f] != 0 && percents[f] != 0) {
								red += ((float)((colors[f] >> 16) & 255) / 255) * percents[f];
								green += ((float)((colors[f] >> 8) & 255) / 255) * percents[f];
								blue += ((float)(colors[f] & 255) / 255) * percents[f];
								count += percents[f];
							}
						}
						if (count > 0) {
							red /= count;
							green /= count;
							blue /= count;
							return (int)((red + 0.2) * 212.5) << 16 | (int)((green + 0.2) * 212.5) << 8 | (int)((blue + 0.2) * 212.5);
						}
					}
				}
			}
			return 0xFFFFFF;
		}
	};
	
	public static final IItemColor ITEM_URN_COLORING = new IItemColor() {
		@Override
		public int colorMultiplier(ItemStack stack, int tintIndex) {
			if (tintIndex > 0 && stack.getItem() instanceof ItemMistSoup) {
				ItemStack[] mainFood = ((ItemMistSoup) stack.getItem()).getFoodColors(stack);
				if (mainFood != null) {
					int[] colors = new int[5];
					int i = 0;
					for (ItemStack food : mainFood) {
						if (!food.isEmpty()) {
							colors[i] = ItemColoring.getFoodColor(food);
						} else break;
						++i;
					}
					if (i > 0) {
						float red = 0, green = 0, blue = 0;
						if (tintIndex <= i && colors[tintIndex - 1] != 0) return colors[tintIndex - 1];
						int count = 0;
						for (int f = 0; f < i; ++f) {
							if (colors[f] != 0) {
								red += (float)((colors[f] >> 16) & 255) / 255;
								green += (float)((colors[f] >> 8) & 255) / 255;
								blue += (float)(colors[f] & 255) / 255;
								++count;
							}
						}
						if (count > 0) {
							red /= count;
							green /= count;
							blue /= count;
							return (int)((red + 0.2) * 212.5) << 16 | (int)((green + 0.2) * 212.5) << 8 | (int)((blue + 0.2) * 212.5);
						}
					}
				}
			}
			return 0xFFFFFF;
		}
	};

	@SideOnly(Side.CLIENT)
	public static void createFoodColorList() {
		TextureAtlasSprite atlas;
		ItemModelMesher imm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
			if (item instanceof ItemFood && item != MistItems.SOUP) {
				int damage = item.isDamageable() ? 0 : 1000; //65536
				for (int i = 0; i < damage; ++i) {
					atlas = imm.getParticleIcon(item, i);
					if (atlas != null && !atlas.getIconName().equals("missingno")) {
						int color = getSmoothColor(atlas);
						if (color != 0) foodColorList.put(new ItemStackMapKey(new ItemStack(item, 1, i)), color);
					}
				}
			}
		}
		Mist.logger.info(Mist.NAME + " saved the smoothed food colors");
	}

	private static int getSmoothColor(TextureAtlasSprite atlas) {
		int count = 0;
		red = green = blue = 0;
		for (int i = 0; i < atlas.getFrameCount(); ++i) {
			data = atlas.getFrameTextureData(i);
			for (int x = 0; x < data.length; ++x) {
				for (int y = 0; y < data[x].length; ++y) {
					color = data[x][y];
					if (((color >> 24) & 255) == 255) {
						red += (color >> 16) & 255;
						green += (color >> 8) & 255;
						blue += color & 255;
						++count;
					}
				}
			}
		}
		if (count == 0) return 0;
		red /= count;
		green /= count;
		blue /= count;
		return 255 << 24 | red << 16 | green << 8 | blue;
	}

	public static int getFoodColor(ItemStack stack) {
		ItemStackMapKey key = new ItemStackMapKey(stack);
		return foodColorList.containsKey(key) ? foodColorList.get(key) : 0;
	}
}