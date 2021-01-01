package ru.liahim.mist.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.entity.IMatWalkable;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.block.gizmos.MistCompostHeap;
import ru.liahim.mist.init.BlockColoring;
import ru.liahim.mist.util.FacingHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**@author Liahim*/
public class MistFloatingMat extends MistBlockWettable implements IColoredBlock {

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColor() {
		return BlockColoring.GRASS_COLORING_0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return BlockColoring.BLOCK_ITEM_COLORING;
	}

	public static final PropertyBool GROWTH = PropertyBool.create("growth");
	protected static final AxisAlignedBB MAT_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D);
	protected static final AxisAlignedBB MAT_AABB_BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.9375D, 1.0D);

	public MistFloatingMat() {
		super(Material.GRASS, 2);
		this.setSoundType(SoundType.SLIME);
		this.setDefaultState(this.blockState.getBaseState().withProperty(WET, true).withProperty(GROWTH, true));
		this.setHardness(0.7F);
		this.setHarvestLevel("shovel", 0);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			long tick = world.getWorldTime() % 24000;
			if (tick > 12000 && rand.nextInt(2 + (int) Math.abs(tick - 18000)/250) == 0) {
				world.playSound(null, pos, MistSounds.BLOCK_SWAMP_FROG, SoundCategory.AMBIENT, 0.5F, world.rand.nextFloat() * 0.3F + 0.7F);
			}
		}
		super.updateTick(world, pos, state, rand);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean par_7) {
		if (!(entity instanceof IMatWalkable) && entity instanceof EntityLivingBase && entity.width * entity.width * entity.height > 0.512F) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, Block.NULL_AABB);
		} else addCollisionBoxToList(pos, entityBox, collidingBoxes, MAT_AABB);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return state.getValue(GROWTH) ? FULL_BLOCK_AABB : MAT_AABB_BOX;
	}

	/*@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		super.updateTick(world, pos, state, rand);
		if (!world.isRemote && rand.nextInt(1000) == 0 && world.getBiome(pos) == MistBiomes.upSwamp &&
				world.getBlockState(pos.down()).getBlock() instanceof BlockLiquid && world.getBlockState(pos.up()).getBlock().isReplaceable(world, pos.up())) {
				world.setBlockState(pos.up(), MistBlocks.MOONBERRY.getDefaultState());
		}
	}*/

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.withProperty(WET, !world.isSideSolid(pos.down(), EnumFacing.UP, false));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(GROWTH) ? 0 : 1;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(GROWTH, meta == 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { WET, GROWTH });
	}

	@Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		if (entity instanceof IMatWalkable) return;
		if (entity.posY < pos.getY() + 0.875D && entity instanceof EntityLivingBase && entity.width * entity.width * entity.height > 0.512F) {
			entity.setInWeb();
			//TODO motion Y with water collide
			if (entity.prevPosY < entity.posY) entity.posY = entity.prevPosY;
		} else {
			entity.motionX *= 0.4D;
			entity.motionZ *= 0.4D;
		}
    }

	@Override
	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
		entity.fall(fallDistance, 0.5F);
    }

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	/*@Override
	public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos) {
        return PathNodeType.WATER;
    }*/

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		switch (side) {
		case UP:
			return true;
		case NORTH:
		case SOUTH:
		case WEST:
		case EAST:
			IBlockState checkState = world.getBlockState(pos.offset(side));
			Block block = checkState.getBlock();
			return !checkState.isOpaqueCube() && block != MistBlocks.FLOATING_MAT;
		default:
			return super.shouldSideBeRendered(state, world, pos, side);
		}
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		switch (face) {
		case UP:
			return false;
		case NORTH:
		case SOUTH:
		case WEST:
		case EAST:
			IBlockState faceState = world.getBlockState(pos.offset(face));
			Block block = faceState.getBlock();
			return faceState.getMaterial().isLiquid();
		default:
			return false;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		player.addStat(StatList.getBlockStats(this));
		player.addExhaustion(0.005F);
		if (this.canSilkHarvest(world, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
			List<ItemStack> items = new ArrayList<ItemStack>();
			items.add(this.getSilkTouchDrop(state));
			ForgeEventFactory.fireBlockHarvesting(items, world, pos, state, 0, 1.0f, true, player);
			for (ItemStack is : items) spawnAsEntity(world, pos, is);
		} else {
			if (world.provider.doesWaterVaporize()) {
				world.setBlockToAir(pos);
				return;
			}
			int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
			harvesters.set(player);
			this.dropBlockAsItem(world, pos, state, i);
			harvesters.set(null);
			if (!canFlow(world, pos)) {
				world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState());
			}
		}
	}

	private boolean canFlow(World world, BlockPos pos) {
		boolean check = false;
		for (EnumFacing face : FacingHelper.NOTUP) {
			Material material = world.getBlockState(pos.offset(face)).getMaterial();
			if (!material.blocksMovement() && !material.isLiquid()) {
				check = true;
				break;
			}
		}
		return check;
	}

	@Override
	public boolean setDry(World world, BlockPos pos, IBlockState state, Random rand) {
		if (world.isSideSolid(pos.down(), EnumFacing.UP)) {
			return world.setBlockState(pos, MistBlocks.COMPOST_HEAP.getDefaultState().withProperty(MistCompostHeap.WORK, false).withProperty(MistCompostHeap.STAGE, rand.nextInt(3) + 2), 3);
		}
		return world.destroyBlock(pos, true);
	}

	@Override
	public boolean setAcid(World world, BlockPos pos, IBlockState state, int waterDist, Random rand) {
		return world.destroyBlock(pos, true);
	}

	@Override
	public boolean doIfWet(World world, BlockPos pos, IBlockState state, Vector2f waterType, boolean fog, Random rand) {
		if (fog) world.destroyBlock(pos, true);
		else if (!state.getValue(GROWTH) && world.rand.nextInt(4) == 0 && world.getLightFromNeighbors(pos.up()) >= 9) {
			return world.setBlockState(pos, state.withProperty(GROWTH, true));
		}
		return false;
	}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction,
		IPlantable plantable) {
		EnumPlantType plantType = plantable.getPlantType(world, pos.offset(direction));
		switch (plantType) {
		case Plains:
		case Cave:
			return true;
		case Beach:
			return (world.getBlockState(pos.east()).getMaterial() == Material.WATER || world.getBlockState(pos.west()).getMaterial() == Material.WATER ||
					world.getBlockState(pos.north()).getMaterial() == Material.WATER || world.getBlockState(pos.south()).getMaterial() == Material.WATER);
		default:
			return false;
		}
	}

	@Override
	public void getDrops(NonNullList ret, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		Random rand = world instanceof World ? ((World)world).rand : RANDOM;
		int count = quantityDropped(state, fortune, rand);
		for (int i = 0; i < count; i++) {
			Item item = this.getItemDropped(state, rand, fortune);
			if (item != null) {
				ret.add(new ItemStack(item, 1, this.damageDropped(state)));
			}
		}
		if (count < 3 && rand.nextInt(3 + count * 2) == 0) {
			ret.add(new ItemStack(Items.STICK));
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(WET) ? MistItems.COMPOST : Item.getItemFromBlock(this);
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random rand) {
		return state.getValue(WET) ? rand.nextInt(3) + 1 : 1;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.DESTROY;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT;
    }

	@Override
	public int getTopProtectPercent(IBlockState state, boolean isWet) {
		return isWet ? 30 : 120;
	}

	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(GROWTH) ? 0 : 1);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(state));
	}
}