package ru.liahim.mist.capability.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import ru.liahim.mist.network.PacketHandler;
import ru.liahim.mist.network.PacketSkillSync;

public class SkillCapaHandler implements ISkillCapaHandler {
	
	private int[] skills = new int[Skill.values().length];
	private EntityPlayerMP playerMP;

	@Override
	public int getSkill(Skill skill) { return this.skills[skill.ordinal()]; }

	@Override
	public void setSkill(Skill skill, int amount) {
		this.skills[skill.ordinal()] = MathHelper.clamp(amount, 0, skill.getTotalSize());
		if (playerMP != null) PacketHandler.INSTANCE.sendTo(new PacketSkillSync(ISkillCapaHandler.getHandler(this.playerMP).getSkillsArray()), this.playerMP);
	}

	@Override
	public void addSkill(Skill skill, int amount) {
		setSkill(skill, this.skills[skill.ordinal()] + amount);
	}

	@Override
	public int[] getSkillsArray() {
		return this.skills;
	}

	@Override
	public void setSkillsArray(int[] skills) {
		this.skills = skills;
	}

	@Override
	public void setPlayer(EntityPlayer player) {
		if (player instanceof EntityPlayerMP) this.playerMP = (EntityPlayerMP)player;
		else this.playerMP = null;
	}

	////////////////////////////////// NBT //////////////////////////////////

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		for(Skill skill : Skill.values()) {
			nbt.setInteger(skill.getName(), this.skills[skill.ordinal()]);
		}
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		for(Skill skill : Skill.values()) {
			this.setSkill(skill, nbt.getInteger(skill.getName()));
		}
	}
}