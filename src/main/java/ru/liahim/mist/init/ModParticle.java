package ru.liahim.mist.init;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.client.particle.ParticleAcidRain;
import ru.liahim.mist.client.particle.ParticleBubbleMist;
import ru.liahim.mist.client.particle.ParticleLatex;

public class ModParticle {

	public static final EnumParticleTypes MIST_RAIN = EnumHelper.addEnum(EnumParticleTypes.class, "mist_rain", new Class[] { String.class, int.class, boolean.class }, "mist_rain", EnumParticleTypes.values().length, false);
	public static final EnumParticleTypes MIST_LATEX = EnumHelper.addEnum(EnumParticleTypes.class, "mist_latex", new Class[] { String.class, int.class, boolean.class }, "mist_latex", EnumParticleTypes.values().length, false);
	public static final EnumParticleTypes MIST_BUBBLE = EnumHelper.addEnum(EnumParticleTypes.class, "mist_bubble", new Class[] { String.class, int.class, boolean.class }, "mist_bubble", EnumParticleTypes.values().length, false);

	@SideOnly(Side.CLIENT)
	public static void registerParticles() {
		Minecraft.getMinecraft().effectRenderer.registerParticle(MIST_RAIN.getParticleID(), new ParticleAcidRain.Factory());
		Minecraft.getMinecraft().effectRenderer.registerParticle(MIST_LATEX.getParticleID(), new ParticleLatex.Factory());
		Minecraft.getMinecraft().effectRenderer.registerParticle(MIST_BUBBLE.getParticleID(), new ParticleBubbleMist.Factory());
	}
}