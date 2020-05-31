package ru.liahim.mist.network;

import ru.liahim.mist.common.MistTime;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketTimeSync implements IMessage {

	int day, month, year;
	long offset;

	public PacketTimeSync() {}

	public PacketTimeSync(int day, int month, int year, long offset) {
		this.day = day;
		this.month = month;
		this.year = year;
		this.offset = offset;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(day);
		buffer.writeInt(month);
		buffer.writeInt(year);
		buffer.writeLong(offset);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		day = buffer.readInt();
		month = buffer.readInt();
		year = buffer.readInt();
		offset = buffer.readLong();
	}	

	public static class Handler implements IMessageHandler<PacketTimeSync, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final PacketTimeSync message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					MistTime.setTime(message.day, message.month, message.year, message.offset);
			}});
			return null;
		}
	}
}
