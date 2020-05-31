package ru.liahim.mist.network;

import io.netty.buffer.ByteBuf;
import ru.liahim.mist.capability.handler.IFoodHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketMushroomSync implements IMessage {

	int[] list;
	boolean isCook;

	public PacketMushroomSync() {}

	public PacketMushroomSync(int[] list, boolean isCook) {
		this.list = list;
		this.isCook = isCook;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeBoolean(this.isCook);
		buffer.writeInt(this.list.length);
		for (int i = 0; i < this.list.length; i++) {
			buffer.writeInt(this.list[i]);
		}
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.isCook = buffer.readBoolean();
		this.list = new int[buffer.readInt()];
		for (int i = 0; i < this.list.length; i++) {
			this.list[i] = buffer.readInt();
		}
	}

	public static class Handler implements IMessageHandler<PacketMushroomSync, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final PacketMushroomSync message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					IFoodHandler mCapa = IFoodHandler.getHandler(Minecraft.getMinecraft().player);
					mCapa.setMushroomList(message.list, message.isCook);
			}});
			return null;
		}
	}
}