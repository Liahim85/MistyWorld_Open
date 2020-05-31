package ru.liahim.mist.network;

import ru.liahim.mist.tileentity.TileEntityCampfire;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketFirePitFillPot implements IMessage {

	int x, y, z, volum, milk;

	public PacketFirePitFillPot() {}

	public PacketFirePitFillPot(BlockPos pos, int volum, int milk) {
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
		this.volum = volum;
		this.milk = milk;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
		buffer.writeInt(volum);
		buffer.writeInt(milk);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		x = buffer.readInt();
		y = buffer.readInt();
		z = buffer.readInt();
		volum = buffer.readInt();
		milk = buffer.readInt();
	}

	public static class Handler implements IMessageHandler<PacketFirePitFillPot, IMessage> {
		@Override
		public IMessage onMessage(final PacketFirePitFillPot message, final MessageContext ctx) {
			EntityPlayer player = ctx.getServerHandler().player;
			IThreadListener mainThread = (WorldServer)player.world; 
	        mainThread.addScheduledTask(new Runnable() {
	        @Override
			public void run() {
	        	TileEntity te = ctx.getServerHandler().player.world.getTileEntity(new BlockPos(message.x, message.y, message.z));
				if (te instanceof TileEntityCampfire) {
					((TileEntityCampfire)te).setVolum(message.volum, false);
					((TileEntityCampfire)te).addMilk(message.milk);
					((TileEntityCampfire)te).updateStatus();
					if (message.volum > 0) {
						IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(player.getHeldItemMainhand());
						if (fluidHandler != null && !player.capabilities.isCreativeMode) {
							if (fluidHandler instanceof FluidBucketWrapper) fluidHandler.drain(Fluid.BUCKET_VOLUME, true);
							else fluidHandler.drain(message.volum * 250, true);
							player.setHeldItem(EnumHand.MAIN_HAND, fluidHandler.getContainer());
						}
					}
				}
			}});
			return null;
		}
	}
}