package ru.liahim.mist.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistFloatingMat;
import ru.liahim.mist.util.FacingHelper;

public class EntityAIEatFloatingMat extends EntityAIEatGrass {

	private final EntityLiving eater;
	private final World world;
	private final boolean destroyGrass;
	int sheepTimer;

	public EntityAIEatFloatingMat(EntityLiving grassEaterEntity, boolean destroyGrass) {
		super(grassEaterEntity);
		this.eater = grassEaterEntity;
		this.world = grassEaterEntity.world;
		this.destroyGrass = destroyGrass;
		this.setMutexBits(7);
	}

	public EntityAIEatFloatingMat(EntityLiving grassEaterEntity) {
		this(grassEaterEntity, true);
	}

	@Override
	public boolean shouldExecute() {
		if (this.eater.getRNG().nextInt(this.eater.isChild() ? 50 : 500) != 0) return false;
		else {
			BlockPos pos = new BlockPos(this.eater.posX, this.eater.posY, this.eater.posZ);
			IBlockState state = this.world.getBlockState(pos);
			if (this.eater.isChild()) return state.getBlock() == MistBlocks.FLOATING_MAT && state.getValue(MistFloatingMat.GROWTH);
			else {
				if (state.getBlock() != MistBlocks.FLOATING_MAT && this.world.isSideSolid(pos, EnumFacing.UP)) pos = pos.down();
				pos = FacingHelper.getAngelOffset(this.eater.rotationYawHead, pos);
				state = this.world.getBlockState(pos);
				return state.getBlock() == MistBlocks.FLOATING_MAT && state.getValue(MistFloatingMat.GROWTH);
			}
		}
	}

	@Override
	public void startExecuting() {
		this.sheepTimer = 40;
		this.world.setEntityState(this.eater, (byte) 10);
		this.eater.getNavigator().clearPath();
	}

	@Override
	public void resetTask() {
		this.sheepTimer = 0;
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.sheepTimer > 0;
	}

	@Override
	public int getEatingGrassTimer() {
		return this.sheepTimer;
	}

	@Override
	public void updateTask() {
		this.sheepTimer = Math.max(0, this.sheepTimer - 1);
		if (this.sheepTimer == 4) {
			BlockPos pos = new BlockPos(this.eater.posX, this.eater.posY, this.eater.posZ);
			IBlockState state = this.world.getBlockState(pos);
			if (!this.eater.isChild()) {
				if (state.getBlock() != MistBlocks.FLOATING_MAT && this.world.isSideSolid(pos, EnumFacing.UP)) pos = pos.down();
				pos = FacingHelper.getAngelOffset(this.eater.rotationYawHead, pos);
				state = this.world.getBlockState(pos);
			}
			if (state.getBlock() == MistBlocks.FLOATING_MAT && state.getValue(MistFloatingMat.GROWTH)) {
				if (this.destroyGrass && ForgeEventFactory.getMobGriefingEvent(this.world, this.eater)) {
					this.world.playEvent(2001, pos, Block.getIdFromBlock(state.getBlock()));
					this.world.setBlockState(pos, state.withProperty(MistFloatingMat.GROWTH, false), 2);
				}
				this.eater.eatGrassBonus();
			}
		}
	}
}