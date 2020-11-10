package ru.liahim.mist.block;

import java.util.Random;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MistBlockDoor extends BlockDoor {

	protected static final AxisAlignedBB SOUTH_AABB_UP = new AxisAlignedBB(0.0D, -1.0D, 0.0D, 1.0D, 1.0D, 0.1875D);
	protected static final AxisAlignedBB NORTH_AABB_UP = new AxisAlignedBB(0.0D, -1.0D, 0.8125D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB WEST_AABB_UP = new AxisAlignedBB(0.8125D, -1.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB EAST_AABB_UP = new AxisAlignedBB(0.0D, -1.0D, 0.0D, 0.1875D, 1.0D, 1.0D);
	protected static final AxisAlignedBB SOUTH_AABB_DOWN = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 2.0D, 0.1875D);
	protected static final AxisAlignedBB NORTH_AABB_DOWN = new AxisAlignedBB(0.0D, 0.0D, 0.8125D, 1.0D, 2.0D, 1.0D);
	protected static final AxisAlignedBB WEST_AABB_DOWN = new AxisAlignedBB(0.8125D, 0.0D, 0.0D, 1.0D, 2.0D, 1.0D);
	protected static final AxisAlignedBB EAST_AABB_DOWN = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.1875D, 2.0D, 1.0D);
	private final int flammability;
	private final int fireSpeed;
	private Item door;

	public Item getDoor() {
		return door;
	}

	public void setDoor(Item door) {
		this.door = door;
	}

	public MistBlockDoor(Material material, float hardness, int flammability, int fireSpeed) {
		super(material);
		this.setHardness(hardness);
		this.setSoundType(material == Material.WOOD ? SoundType.WOOD : SoundType.METAL);
        this.flammability = flammability;
		this.fireSpeed = fireSpeed;
	}

	public MistBlockDoor(float hardness, int flammability, int fireSpeed) {
		this(Material.WOOD, hardness, flammability, fireSpeed);
	}

	public MistBlockDoor(float hardness) {
		this(Material.WOOD, hardness, 20, 5);
	}

	public MistBlockDoor(Material material, float hardness) {
		this(material, hardness, 0, 0);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		state = state.getActualState(source, pos);
		EnumFacing enumfacing = state.getValue(FACING);
		boolean open = !state.getValue(OPEN);
		boolean right = state.getValue(HINGE) == BlockDoor.EnumHingePosition.RIGHT;
		if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
			switch (enumfacing) {
			case EAST:
			default:
				return open ? EAST_AABB_UP : (right ? NORTH_AABB_UP : SOUTH_AABB_UP);
			case SOUTH:
				return open ? SOUTH_AABB_UP : (right ? EAST_AABB_UP : WEST_AABB_UP);
			case WEST:
				return open ? WEST_AABB_UP : (right ? SOUTH_AABB_UP : NORTH_AABB_UP);
			case NORTH:
				return open ? NORTH_AABB_UP : (right ? WEST_AABB_UP : EAST_AABB_UP);
			}
		} else {
			switch (enumfacing) {
			case EAST:
			default:
				return open ? EAST_AABB_DOWN : (right ? NORTH_AABB_DOWN : SOUTH_AABB_DOWN);
			case SOUTH:
				return open ? SOUTH_AABB_DOWN : (right ? EAST_AABB_DOWN : WEST_AABB_DOWN);
			case WEST:
				return open ? WEST_AABB_DOWN : (right ? SOUTH_AABB_DOWN : NORTH_AABB_DOWN);
			case NORTH:
				return open ? NORTH_AABB_DOWN : (right ? WEST_AABB_DOWN : EAST_AABB_DOWN);
			}
		}
	}

	private static boolean check = false;

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IBlockState actualState = state.getActualState(world, pos);
		boolean ret = super.onBlockActivated(world, pos, actualState, player, hand, facing, hitX, hitY, hitZ);
		if (!check && !player.isSneaking() && this.blockMaterial != Material.IRON) {
			BlockPos chechPos = pos.offset(actualState.getValue(HINGE) == EnumHingePosition.LEFT ? actualState.getValue(FACING).rotateY() : actualState.getValue(FACING).rotateYCCW());
			IBlockState door = world.getBlockState(chechPos).getActualState(world, chechPos);
			if (door.getBlock() instanceof BlockDoor && door.getValue(HALF) == actualState.getValue(HALF) && door.getValue(HINGE) != actualState.getValue(HINGE) && door.getValue(OPEN) == actualState.getValue(OPEN)) {
				check = true;
				((BlockDoor)door.getBlock()).onBlockActivated(world, chechPos, door, player, hand, facing, hitX, hitY, hitZ);
				check = false;
			}
		}
		return ret;
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.flammability;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.fireSpeed;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER ? Items.AIR : this.door;
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(this.door);
	}
}