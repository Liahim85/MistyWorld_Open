package ru.liahim.mist.network;

import io.netty.buffer.ByteBuf;
import ru.liahim.mist.init.ModParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSpawnParticle implements IMessage {

	double x, y, z;
	ParticleType type;

	public PacketSpawnParticle() {}

	public PacketSpawnParticle(ParticleType type, double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
		this.type = ParticleType.values()[buf.readInt()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
		buf.writeInt(type.ordinal());
	}

	public static class Handler implements IMessageHandler<PacketSpawnParticle, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final PacketSpawnParticle message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					World world = Minecraft.getMinecraft().player.world;
					if (message.type == ParticleType.SOAP) {
						float size = 0.5F;
						double x = message.x + world.rand.nextDouble() * size - size/2;
						double y = message.y + world.rand.nextDouble() * 0.2 + 0.1;
						double z = message.z + world.rand.nextDouble() * size - size/2;
						world.spawnParticle(ModParticle.MIST_BUBBLE, false, x, y, z, 0, -world.rand.nextFloat() * 0.2F, 0);
					} else if (message.type == ParticleType.CLOUD) {
						for (int i = 0; i < 8; ++i) {
							double d0 = message.x + world.rand.nextDouble() * 0.8D + 0.1D;
							double d1 = message.y + world.rand.nextDouble() * 0.8D + 0.1D;
							double d2 = message.z + world.rand.nextDouble() * 0.8D + 0.1D;
							world.spawnParticle(EnumParticleTypes.CLOUD, false, d0, d1, d2, 0.0D, 0.0D, 0.0D);
						}
					}
			}});
			return null;
		}
	}

	public static enum ParticleType {

		SOAP(),
		CLOUD();
	}
}