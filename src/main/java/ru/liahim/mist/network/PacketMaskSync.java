package ru.liahim.mist.network;

import io.netty.buffer.ByteBuf;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketMaskSync implements IMessage {

	int playerID;
	ItemStack mask = ItemStack.EMPTY;

	public PacketMaskSync() {}

	public PacketMaskSync(EntityPlayer player, ItemStack mask) {
		this.playerID = player.getEntityId();
		this.mask = mask;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(playerID);
		ByteBufUtils.writeItemStack(buffer, mask);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		playerID = buffer.readInt();
		mask = ByteBufUtils.readItemStack(buffer);
	}

	public static class Handler implements IMessageHandler<PacketMaskSync, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final PacketMaskSync message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					World world = Minecraft.getMinecraft().world;
					if (world == null) return;
					Entity player = world.getEntityByID(message.playerID);
					if (player != null && player instanceof EntityPlayer) {
						IMistCapaHandler maskHandler = IMistCapaHandler.getHandler((EntityPlayer)player);
						maskHandler.setStackInSlot(0, message.mask);
					}			
			}});
			return null;
		}
	}
}