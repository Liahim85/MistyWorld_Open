package ru.liahim.mist.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.handlers.ClientEventHandler;

@SideOnly(Side.CLIENT)
public class MistMusic extends PositionedSound implements ITickableSound {

	protected boolean donePlaying;

	public MistMusic(ResourceLocation soundId, SoundCategory category) {
		super(soundId, category);
		this.attenuationType = AttenuationType.NONE;
	}

	@Override
	public void update() {
		if (ClientEventHandler.fadeOut > 0) {
			this.volume *= 0.99;
			if (this.volume <= 0.001F) {
				this.donePlaying = true;
				Minecraft.getMinecraft().getMusicTicker().timeUntilNextMusic = ClientEventHandler.fadeOut;
			}
		}
	}

	@Override
	public boolean isDonePlaying() {
		return this.donePlaying;
	}
}