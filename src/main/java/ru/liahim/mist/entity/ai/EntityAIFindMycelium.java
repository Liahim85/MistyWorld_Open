package ru.liahim.mist.entity.ai;

import java.util.Comparator;
import java.util.Iterator;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.pathfinding.Path;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import ru.liahim.mist.api.entity.IMyceliumFinder;
import ru.liahim.mist.block.upperplant.MistMushroom;
import ru.liahim.mist.init.ModAdvancements;

public class EntityAIFindMycelium extends EntityAIBase {

	EntityCreature entity;
	EntityPlayer player;
    double speed;
    int range;
    private IBlockState lastMushroom;
    private IBlockState mushroom;
    protected BlockPos pos;
    private int delayCounter;

    public EntityAIFindMycelium(EntityCreature entity, int range, double speed) {
        this.entity = entity;
        this.range = range;
        this.speed = speed;
	}

	public void setMushroom(IBlockState state, EntityPlayer player) {
		this.mushroom = state;
		this.player = player;
	}

	@Override
	public boolean shouldExecute() {
		if (this.mushroom != null) {
			this.pos = getTargetPos(this.mushroom);
			if (this.pos == null) {
				if (this.entity instanceof IMyceliumFinder) ((IMyceliumFinder)this.entity).playNotFindSound();
				this.reset();
			}
		}
		return this.pos != null;
	}

	private BlockPos getTargetPos(IBlockState mushroom) {
		Iterator<TileEntity> itr = this.entity.world.loadedTileEntityList.stream()
		.filter(te -> MistMushroom.isPair(mushroom, te))
		.sorted(Comparator.comparingDouble(te -> this.entity.getPosition().distanceSq(te.getPos())))
		.iterator();
		return itr.hasNext() ? itr.next().getPos() : null;
	}

	@Override
	public void startExecuting() {
		boolean finder = this.entity instanceof IMyceliumFinder;
		int i = finder ? ((IMyceliumFinder)this.entity).getFindingRange() : this.range;
		ChunkCache chunkcache = new ChunkCache(this.entity.world, this.entity.getPosition().add(-i, -i, -i), this.entity.getPosition().add(i, i, i), 0);
		Path path = this.entity.getNavigator().pathFinder.findPath(chunkcache, this.entity, this.pos, this.range);
		if (path == null) {
			if (finder) ((IMyceliumFinder)this.entity).playNotFindSound();
			this.reset();
		}
		this.entity.getNavigator().setPath(path, this.speed);
		if (this.delayCounter <= 0 || this.lastMushroom == null || this.mushroom != this.lastMushroom) {
			this.delayCounter = 50;
			this.lastMushroom = this.mushroom;
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.pos != null && this.pos.getX() != Math.floor(this.entity.posX) && this.pos.getZ() != Math.floor(this.entity.posZ) &&
				MistMushroom.isPair(this.mushroom, this.entity.world.getTileEntity(this.pos)) && !this.entity.getNavigator().noPath();
	}

	@Override
	public void resetTask() {
		if (--this.delayCounter <= 0) this.reset();
	}

	private void reset() {
		if (this.pos != null && this.entity.getPosition().distanceSq(this.pos) < 25 &&
				this.player instanceof EntityPlayerMP) ModAdvancements.MYCELIUM.trigger((EntityPlayerMP) this.player, this.player.world, this.pos, this.mushroom);
		this.mushroom = null;
		this.lastMushroom = null;
		this.pos = null;
		this.delayCounter = 0;
	}
}