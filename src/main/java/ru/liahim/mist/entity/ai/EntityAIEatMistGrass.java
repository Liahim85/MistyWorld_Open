package ru.liahim.mist.entity.ai;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import ru.liahim.mist.block.MistGrass;
import ru.liahim.mist.util.FacingHelper;

public class EntityAIEatMistGrass extends EntityAIEatGrass {

	private static final Predicate<IBlockState> IS_TALL_GRASS = BlockStateMatcher.forBlock(Blocks.TALLGRASS).where(BlockTallGrass.TYPE, Predicates.equalTo(BlockTallGrass.EnumType.GRASS));
	private static final Predicate<IBlockState> IS_SNOW = BlockStateMatcher.forBlock(Blocks.SNOW_LAYER).where(BlockSnow.LAYERS, Predicates.equalTo(1));
	private final EntityLiving eater;
	private final World world;
	private final boolean offset;
	private final boolean destroyGrass;
	int sheepTimer;

	public EntityAIEatMistGrass(EntityLiving grassEaterEntity, boolean offset, boolean destroyGrass) {
		super(grassEaterEntity);
		this.eater = grassEaterEntity;
		this.world = grassEaterEntity.world;
		this.offset = offset;
		this.destroyGrass = destroyGrass;
		this.setMutexBits(7);
	}

	public EntityAIEatMistGrass(EntityLiving grassEaterEntity, boolean offset) {
		this(grassEaterEntity, offset, true);
	}

	@Override
	public boolean shouldExecute() {
		if (this.eater.getRNG().nextInt(this.eater.isChild() ? 100 : 1000) != 0 || isMoving()) return false;
		else {
			BlockPos pos = new BlockPos(this.eater.posX, this.eater.posY, this.eater.posZ);
			if (!this.eater.isChild() && this.offset) pos = FacingHelper.getAngelOffset(this.eater.rotationYawHead, pos);
			if (IS_TALL_GRASS.apply(this.world.getBlockState(pos))) return true;
			else {
				IBlockState state = this.world.getBlockState(pos.down());
				return state.getBlock() instanceof MistGrass && state.getValue(MistGrass.GROWTH);
			}
		}
	}

	private boolean isMoving() {
		if (this.eater.isBeingRidden()) {
			EntityLivingBase passenger = (EntityLivingBase) this.eater.getControllingPassenger();
			return passenger.moveForward != 0 || passenger.moveStrafing != 0;
		} return false;
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
			if (!this.eater.isChild() && this.offset) pos = FacingHelper.getAngelOffset(this.eater.rotationYawHead, pos);
			if (IS_TALL_GRASS.apply(this.world.getBlockState(pos))) {
				if (this.destroyGrass && ForgeEventFactory.getMobGriefingEvent(this.world, this.eater)) {
					this.world.destroyBlock(pos, false);
				}
				this.eater.eatGrassBonus();
			} else {
				pos = pos.down();
				IBlockState state = this.world.getBlockState(pos);
				if (state.getBlock() instanceof MistGrass && state.getValue(MistGrass.GROWTH)) {
					if (this.destroyGrass && ForgeEventFactory.getMobGriefingEvent(this.world, this.eater)) {
						if (IS_SNOW.apply(this.world.getBlockState(pos.up()))) this.world.destroyBlock(pos.up(), false);
						this.world.playEvent(2001, pos, Block.getIdFromBlock(state.getBlock()));
						this.world.setBlockState(pos, state.withProperty(MistGrass.GROWTH, false), 2);
					}
					this.eater.eatGrassBonus();
				}
			}
		}
	}
}