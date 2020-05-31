package ru.liahim.mist.network;

import io.netty.buffer.ByteBuf;
import ru.liahim.mist.common.Mist;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSeedSync implements IMessage {

	long seed;

	public PacketSeedSync() {}

	public PacketSeedSync(long seed) {
		this.seed = seed;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeLong(seed);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		seed = buffer.readLong();
	}

	public static class Handler implements IMessageHandler<PacketSeedSync, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final PacketSeedSync message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					Mist.proxy.setClientSeed(message.seed);
			}});
			return null;
		}
	}
}