package ru.liahim.mist.tileentity;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.block.gizmos.MistChest;
import ru.liahim.mist.util.InventoryUtil;

public class TileEntityMistChest extends TileEntityChest {

	private MistChest.ChestType cachedChestType;

	public TileEntityMistChest() {}

	public TileEntityMistChest(MistChest.ChestType type) {
		this.cachedChestType = type;
	}

	@Override
	public String getGuiID() {
		return "mist:chest";
	}

	@Override
	@Nullable
	protected TileEntityChest getAdjacentChest(EnumFacing side) {
		BlockPos blockpos = this.pos.offset(side);
		if (this.isChestAt(blockpos)) {
			TileEntity te = this.world.getTileEntity(blockpos);
			if (te instanceof TileEntityMistChest) {
				TileEntityMistChest chest = (TileEntityMistChest) te;
				chest.setNeighbor(this, side.getOpposite());
				return chest;
			}
		}
		return null;
	}

	private boolean isChestAt(BlockPos pos) {
		if (this.world == null) return false;
		else {
			Block block = this.world.getBlockState(pos).getBlock();
			return block instanceof MistChest && ((MistChest)block).type == this.getType();
		}
	}

	@Override
	public void openInventory(EntityPlayer player) {
		if (!player.isSpectator()) {
			if (this.numPlayersUsing < 0) this.numPlayersUsing = 0;
			++this.numPlayersUsing;
			this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
			this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
			if (this.getType().isTrapped()) {
				this.world.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType(), false);
			}
		}
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		if (!player.isSpectator() && this.getBlockType() instanceof BlockChest) {
			--this.numPlayersUsing;
			this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
			this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
			if (this.getType().isTrapped()) {
				this.world.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType(), false);
			}
		}
	}

	@Override
	public void fillWithLoot(@Nullable EntityPlayer player) {
		if (this.lootTable != null && this.world instanceof WorldServer) {
			LootTable loottable = this.world.getLootTableManager().getLootTableFromLocation(this.lootTable);
			this.lootTable = null;
			Random random;
			if (this.lootTableSeed == 0L) random = new Random();
			else random = new Random(this.lootTableSeed);
			LootContext.Builder builder = new LootContext.Builder((WorldServer) this.world);
			if (player != null) builder.withLuck(player.getLuck()).withPlayer(player);
			InventoryUtil.optimizeAndFillInventory(this, loottable.generateLootForPools(random, builder.build()), random);
		}
	}

	public MistChest.ChestType getType() {
		if (this.cachedChestType == null) {
			if (this.world == null || !(this.getBlockType() instanceof MistChest)) {
				return MistChest.ChestType.NIOBIUM_BASIC;
			}
			this.cachedChestType = ((MistChest)this.getBlockType()).type;
		}
		return this.cachedChestType;
	}

	@SuppressWarnings("incomplete-switch")
	private void setNeighbor(TileEntityChest chest, EnumFacing side) {
		if (chest.isInvalid()) this.adjacentChestChecked = false;
		else if (this.adjacentChestChecked) {
			switch (side) {
			case NORTH:
				if (this.adjacentChestZNeg != chest) this.adjacentChestChecked = false;
				break;
			case SOUTH:
				if (this.adjacentChestZPos != chest) this.adjacentChestChecked = false;
				break;
			case EAST:
				if (this.adjacentChestXPos != chest) this.adjacentChestChecked = false;
				break;
			case WEST:
				if (this.adjacentChestXNeg != chest) this.adjacentChestChecked = false;
			}
		}
	}

	@Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 2, 2));
    }

	public static void initializeType(TileEntity tile, EnumBiomeType biomeType, Random rand) {
		if (tile instanceof TileEntityMistChest) {
			TileEntityMistChest chest = (TileEntityMistChest)tile;
			switch (biomeType) {
				case Forest:
					chest.setLootTable(LootTables.CHEST_BASEMENTS_FOREST_LOOT, rand.nextLong());
					break;
				case Desert:
					chest.setLootTable(LootTables.CHEST_BASEMENTS_DESERT_LOOT, rand.nextLong());
					break;
				case Cold:
					chest.setLootTable(LootTables.CHEST_BASEMENTS_COLD_LOOT, rand.nextLong());
					break;
				case Jungle:
					chest.setLootTable(LootTables.CHEST_BASEMENTS_JUNGLE_LOOT, rand.nextLong());
					break;
				case Swamp:
					chest.setLootTable(LootTables.CHEST_BASEMENTS_SWAMP_LOOT, rand.nextLong());
					break;
				default: break;
			}
		}
	}
}