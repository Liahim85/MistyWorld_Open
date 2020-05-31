package ru.liahim.mist.common;

import java.util.Random;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import ru.liahim.mist.api.advancement.PortalType;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistPortalStone;
import ru.liahim.mist.handlers.ServerEventHandler;
import ru.liahim.mist.init.ModAdvancements;
import ru.liahim.mist.util.PlayerLocationData;
import ru.liahim.mist.util.PortalCoordData;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.biome.BiomeMistUp;

public class MistTeleporter extends Teleporter {

	protected WorldServer worldServerInstance;
	protected Random rand;
	PortalCoordData data;
	PlayerLocationData spawnPoses;
	int dimIn;
	int dimOut;
	BlockPos pPos;

	public MistTeleporter(WorldServer world, BlockPos pPos) {
		super(world);
		this.worldServerInstance = world;
		this.dimOut = this.worldServerInstance.provider.getDimension();
		this.rand = new Random(world.getSeed());
		this.data = PortalCoordData.get(world);
		this.spawnPoses = PlayerLocationData.get(world);
		this.pPos = pPos;
	}

	@Override
	public void placeInPortal(Entity entity, float rotationYaw) {
		this.dimIn = entity.world.provider.getDimension();
		boolean change = false;
		if (!this.placeInExistingPortal(entity, rotationYaw)) {
			if (!isSafeBiomeAt(pPos)) {
				BlockPos safeCoords = findSafeCoords(128, entity);
				if (safeCoords != null) {
					entity.setLocationAndAngles(safeCoords.getX(), entity.posY, safeCoords.getZ(), rotationYaw, 0.0F);
					change = true;
				} else {
					safeCoords = findSafeCoords(256, entity);
					if (safeCoords != null) {
						entity.setLocationAndAngles(safeCoords.getX(), entity.posY, safeCoords.getZ(), rotationYaw, 0.0F);
						change = true;
					} else {
						safeCoords = findSafeCoords(512, entity);
						if (safeCoords != null) {
							entity.setLocationAndAngles(safeCoords.getX(), entity.posY, safeCoords.getZ(), rotationYaw, 0.0F);
							change = true;
						} else {
							if (entity instanceof EntityPlayerMP) {
								EntityPlayerMP playerMP = (EntityPlayerMP)entity;
								ModAdvancements.OPEN_PORTAL.trigger(playerMP, this.worldServerInstance, this.pPos, PortalType.IN_VOID);
							}
							Mist.logger.info("I did everything possible, but could not find a safe biome.");
						}
					}
				}
			}
			if (change) {
				if (!this.placeInExistingPortal(entity, rotationYaw)) {
					this.makePortal(entity);
					this.placeInExistingPortal(entity, rotationYaw);
				}
			} else {
				this.makePortal(entity);
				this.placeInExistingPortal(entity, rotationYaw);
			}
		}
	}

	private boolean isSafeBiomeAt(BlockPos pos) {
		if (this.dimOut == Mist.getID()) {
			if (this.worldServerInstance.getBiome(pos) instanceof BiomeMistUp)
				return true;
			else return false;
		} else {
			return true;
		}
	}

	private BlockPos findSafeCoords(int range, Entity entityIn) {
		for (int i = 0; i < 25; i++) {
			int dx = rand.nextInt(range) - rand.nextInt(range);
			int dz = rand.nextInt(range) - rand.nextInt(range);
			if (isSafeBiomeAt(pPos.add(dx, 0, dz))) {
				return this.pPos.add(dx, 0, dz);
			}
		}
		return null;
	}

	@Override
	public boolean placeInExistingPortal(Entity entity, float rotationYaw) {
		int i = 128;
		double d0 = -1.0D;
		boolean flag = true;
		BlockPos pos = BlockPos.ORIGIN;
		if (this.data.getCoords(this.dimIn, this.pPos) != null) {
			BlockPos savePos = this.data.getCoords(this.dimIn, this.pPos);
			if (this.worldServerInstance.getBlockState(savePos).getBlock() == MistBlocks.PORTAL) {
				d0 = 0.0D;
				pos = savePos;
				flag = false;
			} else {
				this.data.removeCoords(this.dimIn, this.pPos);
			}
		}
		if (this.data.getCoords(this.dimIn, this.pPos) == null) {
			BlockPos pos3 = new BlockPos(MathHelper.floor(entity.posX) - (entity.posX < 0 ? 1 : 0), MathHelper.floor(entity.posY) + 1, MathHelper.floor(entity.posZ) - (entity.posZ < 0 ? 1 : 0));
			BlockPos pos2;
			for (int i1 = -i; i1 <= i; ++i1) {
				for (int j1 = -i; j1 <= i; ++j1) {
					for (BlockPos pos1 = pos3.add(i1, this.worldServerInstance.getActualHeight() - 1 - pos3.getY(), j1); pos1.getY() >= 0; pos1 = pos2) {
						pos2 = pos1.down();
						if (this.worldServerInstance.getBlockState(pos1).getBlock() == MistBlocks.PORTAL && this.data.getDim(this.dimOut, pos1) == this.dimIn) {
							double d1 = pos1.distanceSq(pos3);
							if (d0 < 0.0D || d1 < d0) {
								d0 = d1;
								pos = pos1;
							}
						}
					}
				}
			}
		}
		if (d0 >= 0.0D) {
			if (flag) {
				this.data.addCoords(this.dimIn, this.pPos, this.dimOut, pos);
			}
			double x = pos.getX() - (int)(Math.sin(Math.toRadians(rotationYaw)) * 2) + 0.5D;
			double y = pos.getY() - 0.5D;
			double z = pos.getZ() + (int)(Math.cos(Math.toRadians(rotationYaw)) * 2) + 0.5D;
			entity.motionX = 0;
			entity.motionY = 0;
			entity.motionZ = 0;
			entity.rotationYaw = rotationYaw;
			if (entity instanceof EntityPlayerMP) {
				((EntityPlayerMP)entity).invulnerableDimensionChange = true;
				((EntityPlayerMP)entity).connection.setPlayerLocation(x, y, z, entity.rotationYaw, entity.rotationPitch);
				if (this.dimOut == Mist.getID()) {
					this.spawnPoses.addSpawnPos((EntityPlayerMP)entity, MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
					ServerEventHandler.setSpawnPos((EntityPlayerMP)entity);
				}
			} else entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
			return true;
		} else return false;
	}

	@Override
	public boolean makePortal(Entity entity) {
		int i = 16;
		double d0 = -1.0D;
		int h = 0;
		int d = 65;
		if (this.dimOut == Mist.getID()) {
			h = MistWorld.getFogMaxHight() + 4;
			d = 129;
		}
		BlockPos pos = BlockPos.ORIGIN;
		BlockPos pos3 = new BlockPos(MathHelper.floor(entity.posX) - (entity.posX < 0 ? 1 : 0), MathHelper.floor(entity.posY) + 1, MathHelper.floor(entity.posZ) - (entity.posZ < 0 ? 1 : 0));
		BlockPos pos2;
		for (int x1 = -i; x1 <= i; ++x1) {
			for (int z1 = -i; z1 <= i; ++z1) {
				if (isSafeBiomeAt(pos3.add(x1, 0, z1))) {
					for (BlockPos blockpos1 = pos3.add(x1, this.worldServerInstance.getActualHeight() - 3 - pos3.getY(), z1); blockpos1.getY() >= h; blockpos1 = pos2) {
						pos2 = blockpos1.down();
						while (pos2.getY() > 1 && this.worldServerInstance.getBlockState(pos2).getMaterial().isReplaceable() &&
								!(this.worldServerInstance.getBlockState(pos2).getBlock() instanceof BlockLiquid)) {
							blockpos1 = pos2;
							pos2 = blockpos1.down();
						}
						if (this.worldServerInstance.getBlockState(blockpos1).getMaterial().isReplaceable() &&
								!(this.worldServerInstance.getBlockState(blockpos1).getBlock() instanceof BlockLiquid) &&
								(this.worldServerInstance.getBlockState(pos2).getMaterial().isSolid() ||
								this.worldServerInstance.getBlockState(pos2).getBlock() instanceof BlockLiquid))
						{
							boolean check = true;
							for (int x2 = -1; x2 <= 1; ++x2) {
								for (int z2 = -1; z2 <= 1; ++z2) {
									BlockPos blockpos4 = pos2.add(x2, 0, z2);
									if (check &&
											(!this.worldServerInstance.getBlockState(blockpos4.up()).getMaterial().isReplaceable() ||
											this.worldServerInstance.getBlockState(blockpos4.up()).getBlock() instanceof BlockLiquid ||
											!this.worldServerInstance.getBlockState(blockpos4.up(2)).getMaterial().isReplaceable() ||
											this.worldServerInstance.getBlockState(blockpos4.up(2)).getBlock() instanceof BlockLiquid ||
											(!this.worldServerInstance.getBlockState(blockpos4).getMaterial().isSolid() &&
											this.worldServerInstance.getBlockState(blockpos4).getMaterial() != Material.WATER)))
									{
										check = false;
									}
								}
							}
							if (check) {
								double d1 = blockpos1.distanceSq(pos3);
								if (d0 < 0.0D || d1 < d0) {
									d0 = d1;
									pos = blockpos1.up();
								}
							}
						}
					}
				}
			}
		}
		if (d0 < 0.0D) {
			pos = pos3.add(0, d - pos3.getY(), 0);
			for (int x3 = -1; x3 <= 1; ++x3) {
				for (int y3 = -2; y3 <= 1; ++y3) {
					for (int z3 = -1; z3 <= 1; ++z3) {
						BlockPos blockpos5 = pos.add(x3, y3, z3);
						if (y3 == -2)
							// TODO  replace slab!!!
							this.worldServerInstance.setBlockState(blockpos5, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockSlab.HALF, BlockStoneSlab.EnumBlockHalf.TOP));
						else this.worldServerInstance.setBlockToAir(blockpos5);
					}
				}
			}
		}
		this.worldServerInstance.setBlockState(pos.down(), MistBlocks.PORTAL_WORK.getDefaultState().withProperty(MistPortalStone.ISUP, Boolean.valueOf(false)));
		this.worldServerInstance.setBlockState(pos, MistBlocks.PORTAL.getDefaultState());
		this.worldServerInstance.setBlockState(pos.up(), MistBlocks.PORTAL_WORK.getDefaultState().withProperty(MistPortalStone.ISUP, Boolean.valueOf(true)));
		this.data.addCoords(this.dimIn, this.pPos, this.dimOut, pos);
		this.data.addCoords(this.dimOut, pos, this.dimIn, this.pPos);
		return true;
	}
}