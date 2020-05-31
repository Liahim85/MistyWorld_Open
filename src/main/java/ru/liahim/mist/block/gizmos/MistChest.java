package ru.liahim.mist.block.gizmos;

import net.minecraft.block.BlockChest;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.tileentity.TileEntityMistChest;

public class MistChest extends BlockChest {

	public static final BlockChest.Type MIST_CHEST = EnumHelper.addEnum(BlockChest.Type.class, "mist_chest", new Class[0], new Object[0]);

	public final ChestType type;

	public MistChest(ChestType chestType) {
		super(MIST_CHEST);
		this.type = chestType;
		this.setCreativeTab(Mist.mistTab);
	}

	public MistChest(ChestType chestType, Material material) {
		super(MIST_CHEST);
		this.type = chestType;
		this.blockMaterial = material;
		this.blockMapColor = material.getMaterialMapColor();
		this.translucent = !material.blocksLight();
		this.setCreativeTab(Mist.mistTab);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMistChest();
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return isType(this.type, world, pos.north()) ? NORTH_CHEST_AABB : isType(this.type, world, pos.south()) ? SOUTH_CHEST_AABB : isType(this.type, world, pos.west()) ? WEST_CHEST_AABB : isType(this.type, world, pos.east()) ? EAST_CHEST_AABB : NOT_CONNECTED_AABB;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) return true;
		else {
			ILockableContainer container = this.getLockableContainer(world, pos);
			if (container != null) {
				player.displayGUIChest(container);
				if (!this.type.isTrapped()) player.addStat(StatList.CHEST_OPENED);
				else player.addStat(StatList.TRAPPED_CHEST_TRIGGERED);
			}
			return true;
		}
	}

	@Override
	public boolean canProvidePower(IBlockState state) {
		return this.type.isTrapped();
	}

	public boolean isType(ChestType type, IBlockAccess world, BlockPos pos) {
		ChestType other = getType(world, pos);
		return other != null && other == type;
	}

	public ChestType getType(IBlockAccess world, BlockPos pos) {
		if (world.getBlockState(pos).getBlock() == this) {
			return ((MistChest) world.getBlockState(pos).getBlock()).type;
		}
		return null;
	}

	public static enum ChestType {

		NIOBIUM_BASIC(false),
		NIOBIUM_TRAP(true);

		private final boolean isTrapped;

		ChestType(boolean isTrapped) {
			this.isTrapped = isTrapped;
		}

		public boolean isTrapped() {
			return this.isTrapped;
		}
	}
}