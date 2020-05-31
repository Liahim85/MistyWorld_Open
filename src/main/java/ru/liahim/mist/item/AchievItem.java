package ru.liahim.mist.item;

public class AchievItem extends ItemMist {

	public static final int count = 9;

	public AchievItem() {
		super();
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}
}