package ru.liahim.mist.network;

import io.netty.buffer.ByteBuf;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.capability.handler.MistCapaHandler.HurtType;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketToxicSync implements IMessage {

	int param;
	int typeId;

	public PacketToxicSync() {}

	public PacketToxicSync(int param, int typeId) {
		this.param = param;
		this.typeId = typeId;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(param);
		buffer.writeInt(typeId);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		param = buffer.readInt();
		typeId = buffer.readInt();
	}

	public static class Handler implements IMessageHandler<PacketToxicSync, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final PacketToxicSync message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					IMistCapaHandler mistCapa = IMistCapaHandler.getHandler(Minecraft.getMinecraft().player);
					switch (HurtType.values()[message.typeId]) {
					case POLLUTION:
						mistCapa.setPollution(message.param);
						break;
					case TOXIC:
						mistCapa.setToxic(message.param);
						break;
					}
			}});
			return null;
		}
	}
}