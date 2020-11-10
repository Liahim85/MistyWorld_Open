package ru.liahim.mist.world;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.common.ClientProxy;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.handlers.ClientEventHandler;
import ru.liahim.mist.handlers.FogRenderer;
import ru.liahim.mist.init.ModConfig;

public class WorldProviderMist extends WorldProvider {

	@Override
	public DimensionType getDimensionType() {
		return Mist.dimensionType;
	}

	@Override
	protected void init() {
		this.hasSkyLight = true;
		this.biomeProvider = new BiomeProviderMist(ModConfig.getCustomSeed(this.world.getSeed()), this.world.getWorldInfo());
	}

	@Override
	public IChunkGenerator createChunkGenerator() {
		long seed = ModConfig.getCustomSeed(this.world.getSeed());
		return new ChunkProviderMist(this.world, seed, true);
	}

	@Override
	public int getRespawnDimension(EntityPlayerMP player) {
		return Mist.getID();
	}

	@Override
	public long getSeed() {
		return MistWorld.getCustomSeed() == 0L ? super.getSeed() : MistWorld.getCustomSeed();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getCloudHeight() {
		return 192.0F;
	}

	@Override
	public double getHorizon() {
		return MistWorld.seaLevelDown;
	}

	@Override
	public boolean isBlockHighHumidity(BlockPos pos) {
		return MistWorld.isPosInFog(world, pos) ? true : world.getBiome(pos).isHighHumidity();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
		Vec3d vec3d;
		if (cameraEntity.posY > FogRenderer.fogHeight - 8) {
			vec3d = getSkyColorBody(this.world, cameraEntity, partialTicks);
		} else
			vec3d = new Vec3d(FogRenderer.red, FogRenderer.green, FogRenderer.blue);
		return vec3d;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IRenderHandler getCloudRenderer() {
		return ClientProxy.CloudRendererMist;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IRenderHandler getSkyRenderer() {
		return ClientProxy.SkyRendererMist;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public IRenderHandler getWeatherRenderer() {
        return ClientProxy.WeatherRendererMist;
    }

	private static MusicType currentMusic;
	private static MistMusicType mistMusic;
	private static float volume = 1;

	@Nullable
	@SideOnly(Side.CLIENT)
	@Override
	public MusicTicker.MusicType getMusicType() {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (!player.capabilities.isCreativeMode || !player.capabilities.allowFlying) {
			boolean play = ClientEventHandler.currentSound != null && Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(ClientEventHandler.currentSound);
			if (!play) {
				ClientEventHandler.currentSound = null;
				/*if (volume < 0) {
					volume = 0;
					Minecraft.getMinecraft().getSoundHandler().setSoundLevel(SoundCategory.MUSIC, Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MUSIC));
				}*/
				if (player.posY < MistWorld.getFogMinHight()) {
					if (mistMusic != MistMusicType.DOWN) {
						mistMusic = MistMusicType.DOWN;
						if (player.getRNG().nextInt(3) == 0)
							currentMusic = ClientProxy.MIST_DOWN_MUSIC;
						else currentMusic = null;
					}
				} else {
					if (mistMusic != MistMusicType.UP) {
						mistMusic = MistMusicType.UP;
						if (player.getRNG().nextInt(5) == 0)
							currentMusic = ClientProxy.MIST_UP_MUSIC;
						else currentMusic = null;
					}
				}
			}/* else if (volume == 0 && getMistMusicType(player) != mistMusic) {
				volume = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MUSIC);
			}
			if (volume > 0) {
				volume *= 0.99F;
				if (volume < 0.001F) volume = -1;
				if (ClientEventHandler.currentSound != null) Minecraft.getMinecraft().getSoundHandler().sndManager.sndSystem.setVolume(ClientEventHandler.currentSound.getSoundLocation().toString(), volume);//.setSoundLevel(SoundCategory.MUSIC, volume);
			}*/
			//System.out.println(currentMusic + "_" + volume);
			return currentMusic;
		} else {
			/*if (volume != 0) {
				volume = 0;
				Minecraft.getMinecraft().getSoundHandler().setSoundLevel(SoundCategory.MUSIC, Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MUSIC));
			}*/
			return null;
		}
	}

	/*private MistMusicType getMistMusicType(EntityPlayerSP player) {
		if (player.posY < MistWorld.getFogMinHight()) return MistMusicType.DOWN;
		else return MistMusicType.UP;
	}*/

	private enum MistMusicType { UP, DOWN }

	@SideOnly(Side.CLIENT)
	public Vec3d getSkyColorBody(World world, Entity entity, float partialTicks) {
		float f = world.getCelestialAngle(partialTicks);
		float fs = MathHelper.cos(f * ((float) Math.PI * 2F)) * 2.0F + 0.5F;
		float fc = MathHelper.cos(f * ((float) Math.PI * 2F)) * 2.0F + 0.2F;
		fs = MathHelper.clamp(fs, 0.0F, 1.0F);
		fc = MathHelper.clamp(fc, 0.0F, 1.0F);
		int i = MathHelper.floor(entity.posX);
		int j = MathHelper.floor(entity.posY);
		int k = MathHelper.floor(entity.posZ);
		BlockPos blockpos = new BlockPos(i, j, k);
		int l = net.minecraftforge.client.ForgeHooksClient.getSkyBlendColour(world, blockpos);		
		float r = MathHelper.clamp((int)(120 + fc * ((l >> 16 & 255) - 120)), 0, 255) / 255.0F;
		float g = MathHelper.clamp((int)(210 - fc * (210 - (l >> 8 & 255))), 0, 255) / 255.0F;
		float b = MathHelper.clamp((int)(185 + fc * ((l & 255) - 185)), 0, 255) / 255.0F;
		r = r * fs;
		g = g * fs;
		b = b * fs;
		float rain = world.getRainStrength(partialTicks);
		if (rain > 0.0F) {
			float f1 = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.55F;
			float f2 = 1.0F - rain * 0.75F;
			r = MathHelper.clamp(r * f2 + (f1 - 0.02F) * (1.0F - f2), 0.0F, 1.0F);
			g = MathHelper.clamp(g * f2 + (f1 + 0.02F) * (1.0F - f2), 0.0F, 1.0F);
			b = MathHelper.clamp(b * f2 + (f1 + 0.01F) * (1.0F - f2), 0.0F, 1.0F);
		}
		float Thunder = world.getThunderStrength(partialTicks);
		if (Thunder > 0.0F) {
			float f3 = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.3F;
			float f4 = 1.0F - Thunder * 0.75F;
			r = MathHelper.clamp(r * f4 + (f3 - 0.02F) * (1.0F - f4), 0.0F, 1.0F);
			g = MathHelper.clamp(g * f4 + (f3 + 0.01F) * (1.0F - f4), 0.0F, 1.0F);
			b = MathHelper.clamp(b * f4 + (f3 + 0.02F) * (1.0F - f4), 0.0F, 1.0F);
		}
		if (world.getLastLightningBolt() > 0) {
			float f5 = world.getLastLightningBolt() - partialTicks;
			if (f5 > 1.0F) f5 = 1.0F;
			f5 = f5 * 0.45F;
			r = r * (1.0F - f5) + 0.8F * f5;
			g = g * (1.0F - f5) + 0.8F * f5;
			b = b * (1.0F - f5) + 1.0F * f5;
		}
		return new Vec3d(r, g, b);
	}
}