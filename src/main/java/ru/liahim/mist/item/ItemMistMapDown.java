package ru.liahim.mist.item;

import java.util.List;

import javax.annotation.Nullable;

import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.world.MistWorld;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMistMapDown extends ItemMistMap {

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.format("item.map_down.tooltip"));
	}

	public static ItemStack setupNewMap(World worldIn, double worldX, double worldZ, byte scale, boolean trackingPosition, boolean unlimitedTracking) {
		ItemStack itemstack = new ItemStack(MistItems.MAP_DOWN, 1, worldIn.getUniqueDataId("map"));
		String s = "map_" + itemstack.getMetadata();
		MapData mapdata = new MapData(s);
		worldIn.setData(s, mapdata);
		mapdata.scale = scale;
		mapdata.calculateMapCenter(worldX, worldZ, mapdata.scale);
		mapdata.dimension = worldIn.provider.getDimension();
		mapdata.trackingPosition = trackingPosition;
		mapdata.unlimitedTracking = unlimitedTracking;
		mapdata.markDirty();
		return itemstack;
	}

	@Override
	public void updateMapData(World world, Entity viewer, MapData data) {
		if (world.provider.getDimension() == data.dimension && viewer instanceof EntityPlayer) {
			int i = 1 << data.scale;
			int j = data.xCenter;
			int k = data.zCenter;
			int l = MathHelper.floor(viewer.posX - j) / i + 64;
			int i1 = MathHelper.floor(viewer.posZ - k) / i + 64;
// -------> Mist hook
			int j1 = 48 / i;
			//int j1 = 128 / i;
			
// -------> Mist hook
			boolean mist = world.provider.getDimension() == Mist.getID();
			float fogHight = MistWorld.getFogHight(world, 0);

			if (world.provider.isNether()) {
				j1 /= 2;
			}

			MapData.MapInfo mapdata$mapinfo = data.getMapInfo((EntityPlayer) viewer);
			++mapdata$mapinfo.step;
			boolean flag = false;

			for (int k1 = l - j1 + 1; k1 < l + j1; ++k1) {
				if ((k1 & 15) == (mapdata$mapinfo.step & 15) || flag) {
					flag = false;
					double d0 = 0.0D;

					for (int l1 = i1 - j1 - 1; l1 < i1 + j1; ++l1) {
						if (k1 >= 0 && l1 >= -1 && k1 < 128 && l1 < 128) {
							int i2 = k1 - l;
							int j2 = l1 - i1;
							boolean flag1 = i2 * i2 + j2 * j2 > (j1 - 2) * (j1 - 2);
							int k2 = (j / i + k1 - 64) * i;
							int l2 = (k / i + l1 - 64) * i;
							Multiset<MapColor> multiset = HashMultiset.<MapColor> create();
							Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(k2, 0, l2));

							if (!chunk.isEmpty()) {
								int i3 = k2 & 15;
								int j3 = l2 & 15;
								int k3 = 0;
								double d1 = 0.0D;

								if (world.provider.isNether()) {
									int l3 = k2 + l2 * 231871;
									l3 = l3 * l3 * 31287121 + l3 * 11;

									if ((l3 >> 20 & 1) == 0) {
										multiset.add(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT).getMapColor(world, BlockPos.ORIGIN), 10);
									} else {
										multiset.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE).getMapColor(world, BlockPos.ORIGIN), 100);
									}

									d1 = 100.0D;
								} else {
									BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

									for (int i4 = 0; i4 < i; ++i4) {
										for (int j4 = 0; j4 < i; ++j4) {
											int k4 = chunk.getHeightValue(i4 + i3, j4 + j3) + 1;
											IBlockState iblockstate = Blocks.AIR.getDefaultState();

											if (k4 <= 1) {
												iblockstate = Blocks.BEDROCK.getDefaultState();
											} else {
												label175: {
													while (true) {
														--k4;
														iblockstate = chunk.getBlockState(i4 + i3, k4, j4 + j3);
														blockpos$mutableblockpos.setPos((chunk.x << 4) + i4 + i3, k4, (chunk.z << 4) + j4 + j3);

														if (iblockstate.getMapColor(world, blockpos$mutableblockpos) != MapColor.AIR || k4 <= 0) {
															break;
														}
													}

													if (k4 > 0 && iblockstate.getMaterial().isLiquid()) {
														int l4 = k4 - 1;

														while (true) {
															IBlockState iblockstate1 = chunk.getBlockState(i4 + i3, l4--, j4 + j3);
															++k3;

															if (l4 <= 0 || !iblockstate1.getMaterial().isLiquid()) {
																break label175;
															}
														}
													}
												}
											}

											d1 += (double) k4 / (double) (i * i);

// ---------------------------------------> Mist hook
											if (mist) {
												if (k4 < fogHight - 4) multiset.add(iblockstate.getMapColor(world, blockpos$mutableblockpos));
												else if (k4 < fogHight) multiset.add(MapColor.GRAY);
												else multiset.add(MapColor.GRAY);
											} else multiset.add(iblockstate.getMapColor(world, blockpos$mutableblockpos));
// ---------------------------------------> End of hook

											// multiset.add(iblockstate.getMapColor(worldIn, blockpos$mutableblockpos));
										}
									}
								}

								k3 = k3 / (i * i);
								double d2 = (d1 - d0) * 4.0D / (i + 4) + ((k1 + l1 & 1) - 0.5D) * 0.4D;
								int i5 = 1;

								if (d2 > 0.6D) {
									i5 = 2;
								}

								if (d2 < -0.6D) {
									i5 = 0;
								}

								MapColor mapcolor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MapColor.AIR);

// ---------------------------> Mist hook ------------------------|---------------------------------------------|
								if (mapcolor == MapColor.WATER || mapcolor == MapColor.LIME_STAINED_HARDENED_CLAY) {
									d2 = k3 * 0.2D + (k1 + l1 & 1) * 0.2D;
									i5 = 1;

									if (d2 < 0.5D) {
										i5 = 2;
									}

									if (d2 > 0.9D) {
										i5 = 0;
									}
								}

								d0 = d1;

// ---------------------------> Mist hook
								if (mist) {
									if (d1 > fogHight + 4) {
										i5 = ((k1 + l1) % 3) == 0 ? 0 : 2;
									} else if (d1 >= fogHight - 4) i5 = 0;
								}
// ---------------------------> End of hook

								if (l1 >= 0 && i2 * i2 + j2 * j2 < j1 * j1 && (!flag1 || (k1 + l1 & 1) != 0)) {
									byte b0 = data.colors[k1 + l1 * 128];
									byte b1 = (byte) (mapcolor.colorIndex * 4 + i5);

									if (b0 != b1) {
										data.colors[k1 + l1 * 128] = b1;
										data.updateMapData(k1, l1);
										flag = true;
									}
								}
							}
						}
					}
				}
			}
		}
	}
}