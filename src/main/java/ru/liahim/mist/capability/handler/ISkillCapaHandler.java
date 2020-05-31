package ru.liahim.mist.capability.handler;

import ru.liahim.mist.capability.SkillCapability;
import ru.liahim.mist.init.ModConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface ISkillCapaHandler extends INBTSerializable<NBTTagCompound> {
	
	public int getSkill(Skill skill);
	public void setSkill(Skill skill, int amount);
	public void addSkill(Skill skill, int amount);

	public int[] getSkillsArray();
	public void setSkillsArray(int[] skills);

	public void setPlayer(EntityPlayer player);

	public static ISkillCapaHandler getHandler(EntityPlayer player) {
		ISkillCapaHandler handler = player.getCapability(SkillCapability.CAPABILITY_SKILL, null);
		handler.setPlayer(player);
		return handler;
	}

	public static enum Skill {

		TAMING("taming", new int[] { 50, 150, 300, 500, 750, 1000 }),
		CUTTING("cutting", new int[] { 100, 300, 600, 1000 });

		private final String name;
		private final int[] levelSizesIn;
		private int[] levelSizes;	// 1, 2, 3, -1
		private int[] order;		// 0, 1, (1 + 2), (1 + 2 + 3)

		private Skill(String name, int[] levelSizes) {
			this.name = name;
			this.levelSizesIn = levelSizes;
			updateSizes();
		}

		public void updateSizes() {
			this.levelSizes = new int[this.levelSizesIn.length + 1];
			this.order = new int[this.levelSizesIn.length + 1];
			int size = 0;
			double m = ModConfig.player.skillFactor[this.ordinal()];
			for (int i = 0; i < this.levelSizesIn.length; ++i) {
				size += this.levelSizesIn[i] * m;
				this.levelSizes[i] = (int) (this.levelSizesIn[i] * m);
				this.order[i + 1] = size;
			}
			this.levelSizes[this.levelSizesIn.length] = -1;
			this.order[0] = 0;
		}

		public String getName() {
			return this.name;
		}

		public int getLevel(int count) {
			for (int i = 1; i < this.order.length; ++i) {
				if (count < this.order[i]) return i;
			}
			return this.order.length;
		}

		public int getLevelSize(int count) {
			return this.levelSizes[getLevel(count) - 1];
		}

		public float getPosition(int count) {
			int level = getLevel(count) - 1;
			return (float)(count - this.order[level]) / this.levelSizes[level];
		}

		public int getTotalSize() {
			return this.order[this.order.length - 1];
		}

		public static int getLevel(EntityPlayer player, Skill skill) {
			return skill.getLevel(ISkillCapaHandler.getHandler(player).getSkill(skill));
		}
	}
}