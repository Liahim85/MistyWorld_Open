package ru.liahim.mist.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.capability.handler.ISkillCapaHandler;

public class PacketSkillSync implements IMessage {

	int[] list;

	public PacketSkillSync() {}

	public PacketSkillSync(int[] list) {
		this.list = list;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(this.list.length);
		for (int i = 0; i < this.list.length; i++) {
			buffer.writeInt(this.list[i]);
		}
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.list = new int[buffer.readInt()];
		for (int i = 0; i < this.list.length; i++) {
			this.list[i] = buffer.readInt();
		}
	}

	public static class Handler implements IMessageHandler<PacketSkillSync, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final PacketSkillSync message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					ISkillCapaHandler mCapa = ISkillCapaHandler.getHandler(Minecraft.getMinecraft().player);
					mCapa.setSkillsArray(message.list);
			}});
			return null;
		}
	}
}