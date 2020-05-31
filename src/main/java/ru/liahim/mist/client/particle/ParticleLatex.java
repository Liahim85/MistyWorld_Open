package ru.liahim.mist.client.particle;

import net.minecraft.block.material.Material;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDrip;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ParticleLatex extends ParticleDrip {

	public ParticleLatex(World world, double xCoord, double yCoord, double zCoord) {
		super(world, xCoord, yCoord, zCoord, Material.WATER);
		this.particleRed = 1.0F;
		this.particleGreen = 1.0F;
		this.particleBlue = 1.0F;
	}

	@SideOnly(Side.CLIENT)
	public static class Factory implements IParticleFactory {
		@Override
		public Particle createParticle(int id, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... param) {
			return new ParticleLatex(world, x, y, z);
		}
	}
}