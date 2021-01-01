package ru.liahim.mist.block;

import java.util.Random;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.api.block.IMistStoneUpper;
import ru.liahim.mist.api.block.IMossable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.block.gizmos.MistCampfire.CookingTool;
import ru.liahim.mist.capability.handler.ISkillCapaHandler;
import ru.liahim.mist.capability.handler.ISkillCapaHandler.Skill;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.handlers.ServerEventHandler;
import ru.liahim.mist.init.BlockColoring;
import ru.liahim.mist.init.ModAdvancements;
import ru.liahim.mist.network.PacketHandler;
import ru.liahim.mist.network.PacketSpawnParticle;
import ru.liahim.mist.network.PacketSpawnParticle.ParticleType;
import ru.liahim.mist.tileentity.TileEntityCampfire;
import ru.liahim.mist.util.FacingHelper;
import ru.liahim.mist.world.MistWorld;

public class MistStoneMined extends MistBlock implements IMistStoneUpper, IColoredBlock, IMossable {

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColor() {
		return BlockColoring.GRASS_COLORING_1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return BlockColoring.BLOCK_ITEM_COLORING;
	}

	public static final PropertyEnum<EnumStoneStage> STAGE = PropertyEnum.<EnumStoneStage>create("stage", EnumStoneStage.class);
	public static final PropertyEnum<EnumStoneType> TYPE = PropertyEnum.<EnumStoneType>create("type", EnumStoneType.class);

	public MistStoneMined() {
		super(Material.ROCK);
		this.setHarvestLevel("pickaxe", 0);
		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, EnumStoneType.MINED).withProperty(STAGE, EnumStoneStage.NORMAL));
		this.setTickRandomly(true);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote && rand.nextBoolean()) {
			IBlockState downState = world.getBlockState(pos.down());
			boolean campfire = downState.getBlock() == MistBlocks.CAMPFIRE;
			if (campfire) {
				TileEntity tile = world.getTileEntity(pos.down());
				if (tile instanceof TileEntityCampfire) {
					TileEntityCampfire fire = (TileEntityCampfire)tile;
					if (fire.getCookingTool() == CookingTool.NONE && fire.getTemperature() > 20) return;
				}
			}
			Material downMat = downState.getMaterial();
			EnumStoneType type = state.getValue(TYPE);
			EnumStoneStage stage = state.getValue(STAGE);
			EnumStoneStage newStage = null;
			if (downMat == Material.FIRE || downMat == Material.LAVA) {
				newStage = updateHotStatus(world, pos, state);
			} else {
				if (stage == EnumStoneStage.HOT_3) newStage = EnumStoneStage.HOT_2;
				else if (stage == EnumStoneStage.HOT_2) newStage = EnumStoneStage.HOT_1;
				else if (stage == EnumStoneStage.HOT_1) {
					if (type.isNature() && !campfire) {
						world.setBlockState(pos, MistBlocks.STONE.getDefaultState());
						return;
					} else newStage = EnumStoneStage.NORMAL;
				} else if (stage == EnumStoneStage.MOSS) {
					if (rand.nextBoolean() && MistWorld.isPosInFog(world, pos.getY())) {
						newStage = EnumStoneStage.NORMAL;
					}
				} else if (stage == EnumStoneStage.NORMAL) {
					if (type.isNature()) {
						if (!campfire) world.setBlockState(pos, MistBlocks.STONE.getDefaultState());
						return;
					} else {
						if (rand.nextInt(250) == 0 && !MistWorld.isPosInFog(world, pos.getY()) && world.getBiome(pos).getRainfall() >= 0.3F) {
							for (EnumFacing face : FacingHelper.NOTDOWN) {
								if (world.getBlockState(pos.offset(face)).getBlock() == MistBlocks.ACID_BLOCK) {
									return;
								}
							}
							boolean check = false;
							if (!world.isSideSolid(pos.up(), EnumFacing.DOWN)) {
								for (EnumFacing side : EnumFacing.HORIZONTALS) {
									if (world.getBlockState(pos.offset(side)).getBlock() instanceof MistGrass) {
										check = true;
										break;
									}
								}
							}
							if (!check) {
								for (EnumFacing side : EnumFacing.HORIZONTALS) {
									if (!world.isSideSolid(pos.offset(side), side.getOpposite()) && world.getBlockState(pos.offset(side).down()).getBlock() instanceof MistGrass) {
										check = true;
										break;
									}
								}
							}
							if (check) newStage = EnumStoneStage.MOSS;
						}
					}
				}
			}
			if (newStage != null) world.setBlockState(pos, state.withProperty(STAGE, newStage));
		}
	}

	public static EnumStoneStage updateHotStatus(World world, BlockPos pos, IBlockState state) {
		EnumStoneStage stage = state.getValue(STAGE);
		EnumStoneStage newStage = null;
		if (stage == EnumStoneStage.MOSS) newStage = EnumStoneStage.NORMAL;
		else if (stage == EnumStoneStage.NORMAL) newStage = EnumStoneStage.HOT_1;
		else if (stage == EnumStoneStage.HOT_1) newStage = EnumStoneStage.HOT_2;
		else if (stage == EnumStoneStage.HOT_2) {
			if (checkWater(state, world, pos.up(), pos, EnumStoneStage.HOT_3)) return null;
			else if (!world.isRainingAt(pos.up())) newStage = EnumStoneStage.HOT_3;
			else return null;
		}
		return newStage;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote && state.getValue(STAGE) == EnumStoneStage.MOSS) {
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
		if (!world.isRemote) {
			EnumStoneStage stage = state.getValue(STAGE);
			if (stage == EnumStoneStage.MOSS) {
				if (fromPos.getY() >= pos.getY() && world.getBlockState(fromPos).getBlock() == MistBlocks.ACID_BLOCK) {
					world.setBlockState(pos, this.getDefaultState());
				}
			} else {
				int hot = stage.getHot();
				if (hot > 0) {
					for (EnumFacing face : hot == 3 ? FacingHelper.NOTDOWN : EnumFacing.HORIZONTALS) {
						if (checkWater(state, world, pos.offset(face), pos, stage)) return;
					}
				}
				if (state.getValue(TYPE).isNature()) {
					if (!MistStoneUpper.checkConnection(world, pos)) {
						world.destroyBlock(pos, false);
						Block.spawnAsEntity(world, pos, new ItemStack(this));
						Pair<BlockPos, EntityPlayer> pair = ServerEventHandler.breakPlayer;
						if (pair != null && pair.getKey().equals(fromPos) && pair.getValue() instanceof EntityPlayerMP) {
							ModAdvancements.STONE_MINED.trigger((EntityPlayerMP)pair.getValue(), new ItemStack(MistBlocks.STONE_MINED));
						}
					} else if (stage == EnumStoneStage.NORMAL) {
						IBlockState checkState = world.getBlockState(pos.down());
						if (checkState.getBlock() != MistBlocks.CAMPFIRE && checkState.getMaterial() != Material.FIRE && checkState.getMaterial() != Material.LAVA) {
							world.setBlockState(pos, MistBlocks.STONE.getDefaultState());
						}
					}
				}
			}
		}
	}

	protected static boolean checkWater(IBlockState state, World world, BlockPos pos, BlockPos center, EnumStoneStage stage) {
		IBlockState checkState = world.getBlockState(pos);
		boolean water = checkState.getMaterial() == Material.WATER;
		boolean check = false;
		if (water || checkState.getBlock() == Blocks.ICE || checkState.getBlock() == Blocks.SNOW) {
			if (water) world.setBlockToAir(pos);
			else world.setBlockState(pos, Blocks.WATER.getDefaultState());
			if (stage.getHot() == 3) ((MistStoneMined)state.getBlock()).spawnBricks(world, pos, center);
			else world.setBlockState(center, state.withProperty(STAGE, EnumStoneStage.NORMAL));
			check = true;
		} else if (checkState.getBlock() == Blocks.SNOW_LAYER) {
			world.setBlockToAir(pos);
			check = true;
		}
		if (check) {
			world.playSound((EntityPlayer)null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 1.0F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
			PacketHandler.INSTANCE.sendToAllAround(new PacketSpawnParticle(ParticleType.CLOUD, pos.getX(), pos.getY(), pos.getZ()), new TargetPoint(Mist.getID(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 32));
		}
		return check;
	}

	protected void spawnBricks(World world, BlockPos waterPos, BlockPos pos) {
		Pair<BlockPos, EntityPlayer> pair = ServerEventHandler.waterPlayer;
		int skill = 1;
		if (pair != null && pair.getKey().equals(waterPos)) {
			skill = Skill.getLevel(pair.getValue(), Skill.MASON);
			ISkillCapaHandler.getHandler(pair.getValue()).addSkill(Skill.MASON, 10);
			if (pair.getValue() instanceof EntityPlayerMP) ModAdvancements.BRICK.trigger((EntityPlayerMP)pair.getValue(), new ItemStack(MistItems.BRICK));
		}
		world.playSound(null, pos, MistSounds.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.2F + 0.9F);
		world.destroyBlock(pos, false);
		int count = skill + world.rand.nextInt(world.rand.nextInt(2) + 1);
		if (count > 4) count = 4;
		for (int i = 0; i < count; ++i) Block.spawnAsEntity(world, pos, new ItemStack(MistItems.BRICK));
		for (int i = 0; i < 4 - count; ++i) Block.spawnAsEntity(world, pos, new ItemStack(MistItems.ROCKS));
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		if (state.getValue(TYPE).isNature()) ISkillCapaHandler.getHandler(player).addSkill(Skill.MASON, 2);
		super.harvestBlock(world, player, pos, state, te, stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		if (state.getValue(STAGE).getHot() == 3 && world.isRainingAt(pos.up())) {
			double x = pos.getX() + world.rand.nextDouble();
			double y = pos.getY() + world.rand.nextDouble() * 0.2 + 1.1;
			double z = pos.getZ() + world.rand.nextDouble();
			world.spawnParticle(EnumParticleTypes.CLOUD, x, y, z, 0, 0, 0);
			world.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F, true);
		}
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {
		EnumStoneStage type = world.getBlockState(pos).getValue(STAGE);
		if (type == EnumStoneStage.HOT_3 && !entity.isImmuneToFire() && entity instanceof EntityLivingBase && !EnchantmentHelper.hasFrostWalkerEnchantment((EntityLivingBase) entity)) {
			entity.attackEntityFrom(DamageSource.HOT_FLOOR, 1.0F);
		}
		super.onEntityWalk(world, pos, entity);
	}

	@Override
	public void getDrops(NonNullList drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		if (state.getValue(TYPE).isNature()) {
			MistBlocks.STONE.getDrops(drops, world, pos, state, fortune);
		} else super.getDrops(drops, world, pos, state, fortune);
	}

	@Override
	public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
		return state.getValue(TYPE).isNature() ? 100 : 3;
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
		return world.getBlockState(pos).getValue(TYPE).isNature() ? 1000 : 10;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(STAGE).getHot() * 4;
    }

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return state.getValue(TYPE).isNature() ? EnumPushReaction.BLOCK : EnumPushReaction.NORMAL;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).getMetadata() * 5 + state.getValue(STAGE).getMetadata();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TYPE, EnumStoneType.byMetadata(meta / 5)).withProperty(STAGE, EnumStoneStage.byMetadata(meta % 5));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, STAGE, TYPE);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		if (state.getValue(TYPE).isNature()) return new ItemStack(Item.getItemFromBlock(MistBlocks.STONE));
		else if (state.getValue(STAGE) != EnumStoneStage.MOSS) state = state.withProperty(STAGE, EnumStoneStage.NORMAL);
		return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(state));
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TYPE, EnumStoneType.MINED).withProperty(STAGE, EnumStoneStage.NORMAL))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TYPE, EnumStoneType.MINED).withProperty(STAGE, EnumStoneStage.MOSS))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TYPE, EnumStoneType.CHISELED).withProperty(STAGE, EnumStoneStage.NORMAL))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TYPE, EnumStoneType.CHISELED).withProperty(STAGE, EnumStoneStage.MOSS))));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return state.getValue(STAGE) == EnumStoneStage.MOSS ? layer == BlockRenderLayer.CUTOUT_MIPPED : layer == BlockRenderLayer.SOLID;
    }

	@Override
	public boolean isUpperStone(IBlockState state) {
		return state.getValue(TYPE).isNature();
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return false;
	}

	@Override
	public boolean setMossy(IBlockState state, World world, BlockPos pos) {
		if (!(state.getBlock() instanceof IMossable) || state.getValue(TYPE).isNature || state.getValue(STAGE) != EnumStoneStage.NORMAL) return false;
		return world.setBlockState(pos, state.withProperty(STAGE, EnumStoneStage.MOSS));
	}

	public static enum EnumStoneType implements IStringSerializable {

		MINED(0, "mined", false),
		CHISELED(1, "chiseled", false),
		NATURE(2, "nature", true);

		private static final EnumStoneType[] META_LOOKUP = new EnumStoneType[values().length];
		private final int meta;
		private final String name;
		private final boolean isNature;

		private EnumStoneType(int meta, String name, boolean isNature) {
			this.meta = meta;
			this.name = name;
			this.isNature = isNature;
		}

		public int getMetadata() {
			return this.meta;
		}

		public boolean isNature() {
			return this.isNature;
		}

		public static EnumStoneType byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		@Override
		public String getName() {
			return this.name;
		}

		static {
			for (EnumStoneType type : values()) {
				META_LOOKUP[type.getMetadata()] = type;
			}
		}
	}

	public static enum EnumStoneStage implements IStringSerializable {

		NORMAL(0, "normal", 0),
		HOT_1(1, "hot_1", 1),
		HOT_2(2, "hot_2", 2),
		HOT_3(3, "hot_3", 3),
		MOSS(4, "moss", 0);

		private static final EnumStoneStage[] META_LOOKUP = new EnumStoneStage[values().length];
		private final int meta;
		private final String name;
		private final int hot;

		private EnumStoneStage(int meta, String name, int hot) {
			this.meta = meta;
			this.name = name;
			this.hot = hot;
		}

		public int getMetadata() {
			return this.meta;
		}

		public int getHot() {
			return this.hot;
		}

		public static EnumStoneStage byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		@Override
		public String getName() {
			return this.name;
		}

		static {
			for (EnumStoneStage type : values()) {
				META_LOOKUP[type.getMetadata()] = type;
			}
		}
	}
}