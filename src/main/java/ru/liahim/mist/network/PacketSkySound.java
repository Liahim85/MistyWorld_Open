package ru.liahim.mist.network;

import io.netty.buffer.ByteBuf;
import ru.liahim.mist.api.sound.MistSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSkySound implements IMessage {

	SkySoundType type;
	float volume;

	public PacketSkySound() {}

	public PacketSkySound(SkySoundType type, float volume) {
		this.type = type;
		this.volume = volume;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.type = SkySoundType.values()[buf.readInt()];
		this.volume = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(type.ordinal());
		buf.writeFloat(volume);
	}

	public static class Handler implements IMessageHandler<PacketSkySound, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final PacketSkySound message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					Minecraft.getMinecraft().player.playSound(message.type.event, message.volume, 1);
			}});
			return null;
		}
	}

	public static enum SkySoundType {

		AMBIENT(MistSounds.SKY_SOUND),
		BOOM(MistSounds.SKY_BOOM);

		private final SoundEvent event;

		private SkySoundType(SoundEvent event) {
			this.event = event;
		}
	}
}