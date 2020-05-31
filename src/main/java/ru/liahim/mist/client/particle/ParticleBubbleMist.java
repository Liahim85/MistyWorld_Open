package ru.liahim.mist.client.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleBubble;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ParticleBubbleMist extends ParticleBubble {

	public ParticleBubbleMist(World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		super(world, x, y, z, xSpeed, ySpeed, zSpeed);
	}

	@Override
	public void setExpired() {
		if (this.particleMaxAge-- <= 0) this.isExpired = true;
	}

	@SideOnly(Side.CLIENT)
	public static class Factory implements IParticleFactory {
		@Override
		public Particle createParticle(int id, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... params) {
			return new ParticleBubbleMist(world, x, y, z, xSpeed, ySpeed, zSpeed);
		}
	}
}