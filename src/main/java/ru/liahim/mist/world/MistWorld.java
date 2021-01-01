package ru.liahim.mist.world;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.ISeasonalChanges;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.handlers.FogRenderer;
import ru.liahim.mist.handlers.ServerEventHandler;

public class MistWorld {

	public static final float baseHeightUp = 3.6F;
	public static final float baseHeightDown = -1.7F;
	public static final byte seaLevelUp = 125;
	public static final byte seaLevelDown = 36;
	public static final byte lowerStoneHight = 26;
	public static final int fogMinHight_S = 100;
	public static final int fogMaxHight_S = 108;
	private static float fogDelta = 0;
	public static IBlockState stoneBlockUpper;
	public static IBlockState stoneBlockMedium;
	public static IBlockState stoneBlockLower;
	public static IBlockState gravelBlock;
	public static IBlockState worldStone;
	public static IBlockState waterBlockUpper;
	public static IBlockState waterBlockLower;
	public static final DamageSource IN_FOG = (new DamageSource("mist.in_fog")).setDamageBypassesArmor();
	public static final DamageSource DISSOLUTION = (new DamageSource("mist.dissolution")).setDamageBypassesArmor();
	private final static int fogHumi = 160;
	private static long customSeed;
	public static void setCustomSeed(long seed) { customSeed = seed; }
	public static long getCustomSeed() { return customSeed; }
	@SideOnly(Side.CLIENT)
	private static long clientSeed;
	@SideOnly(Side.CLIENT)
	public static void setClientSeed(long seed) { clientSeed = seed; }
	@SideOnly(Side.CLIENT)
	public static long getClientSeed() { return clientSeed; }

	public static void setWorldBlocks() {
		stoneBlockUpper = MistBlocks.STONE.getDefaultState();
		stoneBlockMedium = MistBlocks.STONE_POROUS.getDefaultState();
		stoneBlockLower = MistBlocks.STONE_BASIC.getDefaultState();
		gravelBlock = MistBlocks.GRAVEL.getDefaultState();
		worldStone = stoneBlockMedium;
		waterBlockUpper = Blocks.WATER.getDefaultState();
		waterBlockLower = MistBlocks.ACID_BLOCK.getDefaultState();
	}

	public static float getFogHight(World world, float tick) {
		//int h = fogMaxHight - fogMinHight;
		float f = MathHelper.cos(world.getCelestialAngle(tick) * ((float)Math.PI * 2F) - 0.35F) * 16 + 4; //h * 2 + h / 2;
		f = MathHelper.clamp(f, 0.0F, 8); //h);
		float rain = world.getRainStrength(tick);
		return getFogMinHight() + f - rain;
	}

	public static float getHumi(World world, BlockPos pos, float tick) {
		Material mat = world.getBlockState(pos).getMaterial();
		if (mat == Material.WATER) return 200.0F;
		int dim = world.provider.getDimension();
		if (dim == -1 || dim == 1 || mat == Material.LAVA || mat == Material.FIRE) return 0.0F;
		float H = 0.0F;
		if (dim == Mist.getID()) {
			int Y = pos.getY();
			if (Y < getFogMinHight()) return fogHumi;
			else if (Y < getFogMaxHight() + 4.0F) {
				float fogHight;
				if (world.isRemote) fogHight = FogRenderer.fogHeight;
				else fogHight = getFogHight(world, tick) + 4.0F;
				H = Math.max(fogHight - Y, 0);
			}
		}
		Biome biome = world.getBiome(pos);
		float cel = world.getCelestialAngle(tick) * ((float)Math.PI * 2F);
		float light = Math.max(world.getLightFor(EnumSkyBlock.BLOCK, pos) * 0.625F, world.getLightFor(EnumSkyBlock.SKY, pos) * getSunBrightnessBody(world, cel, 0));
		float temp = MathHelper.clamp(biome.getTemperature(pos) * 0.4F + 0.2F, 0.0F, 1.0F);
		float humi = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
		float delta = (temp * 0.4F + 0.4F) * MathHelper.cos(cel - 0.35F);
		float percent = 100 - (temp - humi + delta)/1.8F * 100 - light + world.getRainStrength(tick) * 15;
		return H == 0.0F || percent > fogHumi ? percent : H >= 4.0F ? fogHumi : percent + (fogHumi - percent)/4.0F * H;
	}

	private static float getSunBrightnessBody(World world, float cel, float tick) {
        float f1 = 1.0F - (MathHelper.cos(cel) * 2.0F + 0.2F);
        f1 = 1.0F - MathHelper.clamp(f1, 0.0F, 1.0F);
        return f1 * 0.8F + 0.2F;
    }

	/**Only for server!*/
	public static long getPosRandom(World world, BlockPos pos, int range) {
		long l = pos.getX() * 3129871L ^ pos.getZ() * 116129781L ^ pos.getY() * world.getSeed();
		l = l * l * 42317861L + l * 11L;
		return Math.abs(range == 0 ? l : (l % (range + 3)) % range);
	}

	private static BlockPos getCenterPos(long seed, BlockPos pos, boolean main) {
		int deltaX = (int)(seed >> 8 & 15L) * 1024;
		int deltaZ = (int)(seed >> 16 & 15L) * 1024;
		if (main) {
			deltaX = deltaX - (pos.getX() & 16383);
			deltaZ = deltaZ - (pos.getZ() & 16383);
			if (Math.abs(deltaX) >= 8192) deltaX = deltaX > 0 ? (deltaX = deltaX - 16384) : (deltaX = deltaX + 16384);
			if (Math.abs(deltaZ) >= 8192) deltaZ = deltaZ > 0 ? (deltaZ = deltaZ - 16384) : (deltaZ = deltaZ + 16384);
		} else {
			deltaX = (deltaX + 2048) & 4095;
			deltaZ = (deltaZ + 2048) & 4095;
			deltaX = deltaX - (pos.getX() & 4095);
			deltaZ = deltaZ - (pos.getZ() & 4095);
			if (Math.abs(deltaX) >= 2048) deltaX = deltaX > 0 ? (deltaX = deltaX - 4096) : (deltaX = deltaX + 4096);
			if (Math.abs(deltaZ) >= 2048) deltaZ = deltaZ > 0 ? (deltaZ = deltaZ - 4096) : (deltaZ = deltaZ + 4096);
		}
		return new BlockPos(pos.getX() + deltaX, seaLevelDown, pos.getZ() + deltaZ);
	}

	@SideOnly(Side.CLIENT)
	/**Center radius ~384; Main Center radius ~768.*/
	public static BlockPos getCenterPos(BlockPos pos, boolean main) {
		return getCenterPos(Mist.proxy.getClientSeed(), pos, main);
	}

	/**Center radius ~384; Main Center radius ~768.*/
	public static BlockPos getCenterPos(World world, BlockPos pos, boolean main) {
		return getCenterPos(world.getSeed(), pos, main);
	}

	public static boolean isPosInFog(World world, BlockPos pos) {
		return isPosInFog(world, pos.getY());
	}

	public static boolean isPosInFog(World world, float y) {
		if (world.provider.getDimension() == Mist.getID()) {
			if (y >= MistWorld.getFogMaxHight() + 4.0F) return false;
			else if (y >= MistWorld.getFogMinHight() + 4.0F) {
				if (world.isRemote) return y < FogRenderer.fogHeight;
				else return y < MistWorld.getFogHight(world, 0) + 4.0F;
			}
			else return true;
		} else return false;
	}

	public static int getFogMinHight() {
		return fogMinHight_S + (int)MistWorld.fogDelta;
	}

	public static int getFogMaxHight() {
		return fogMaxHight_S + (int)MistWorld.fogDelta;
	}

	//���������� �������� (�� ����/�� ����) !!!
	public static void setFogDelta(float fogDelta) {
		MistWorld.fogDelta = fogDelta;
	}

	public static void seasonalTest(Chunk chunk) {
		int x = chunk.x * 16;
		int z = chunk.z * 16;
		int h = 0;
		IBlockState state;
		IBlockState newState;
		long tick = MistTime.getTickOfMonth(chunk.getWorld());
		for (int ii = 0; ii < 16; ++ii) {
			ExtendedBlockStorage ebs = chunk.getBlockStorageArray()[ii];
			if (ebs != Chunk.NULL_BLOCK_STORAGE && !ebs.isEmpty()) {
				for (int i = 0; i < 16; ++i) {
	            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                	state = ebs.get(i, j, k);
                	if (state.getBlock() instanceof ISeasonalChanges) {
                		newState = ((ISeasonalChanges)state.getBlock()).getSeasonState(chunk.getWorld(), new BlockPos(x + i, h + j, z + k), state, tick);
                		if (newState != null) {
                			ebs.set(i, j, k, newState);
                		}
                	}
                }
	            }
				}
			}
			h += 16;
		}
	}

	public static boolean canPlayAmbiendSounds(World world, BlockPos pos) {
		return ServerEventHandler.ambientSoundsTimer <= 0 && !world.isRaining() && pos.getY() > MistWorld.getFogMaxHight();
	}
}