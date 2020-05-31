package ru.liahim.mist.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import ru.liahim.mist.api.block.MistBlocks;

public class WalkNodeProcessorMistUpperSwamp extends WalkNodeProcessorMistUpper {

	@Override
	protected PathNodeType getPathNodeTypeRaw(IBlockAccess world, int x, int y, int z) {
		BlockPos blockpos = new BlockPos(x, y, z);
        IBlockState iblockstate = world.getBlockState(blockpos);
        Block block = iblockstate.getBlock();
        Material material = iblockstate.getMaterial();

        PathNodeType type = block.getAiPathNodeType(iblockstate, world, blockpos);
        if (type != null) return type;

        if (material == Material.AIR) {
            return PathNodeType.OPEN;
        } else if (block != Blocks.TRAPDOOR && block != Blocks.IRON_TRAPDOOR && block != Blocks.WATERLILY) {
            if (block == Blocks.FIRE) {
                return PathNodeType.DAMAGE_FIRE;
            } else if (block == Blocks.CACTUS) {
                return PathNodeType.DAMAGE_CACTUS;
            } else if (block instanceof BlockDoor && material == Material.WOOD && !iblockstate.getValue(BlockDoor.OPEN).booleanValue()) {
                return PathNodeType.DOOR_WOOD_CLOSED;
            } else if (block instanceof BlockDoor && material == Material.IRON && !iblockstate.getValue(BlockDoor.OPEN).booleanValue()) {
                return PathNodeType.DOOR_IRON_CLOSED;
            } else if (block instanceof BlockDoor && iblockstate.getValue(BlockDoor.OPEN).booleanValue()) {
                return PathNodeType.DOOR_OPEN;
            } else if (block instanceof BlockRailBase) {
                return PathNodeType.RAIL;
            } else if (!(block instanceof BlockFence) && !(block instanceof BlockWall) && (!(block instanceof BlockFenceGate) || iblockstate.getValue(BlockFenceGate.OPEN).booleanValue())) {
                if (material == Material.WATER || block == MistBlocks.FLOATING_MAT) {
                    return PathNodeType.WATER;
                } else if (material == Material.LAVA) {
                    return PathNodeType.LAVA;
                } else {
                    return block.isPassable(world, blockpos) ? PathNodeType.OPEN : PathNodeType.BLOCKED;
                }
            } else {
                return PathNodeType.FENCE;
            }
        } else {
            return PathNodeType.TRAPDOOR;
        }
    }
}