package ru.liahim.mist.block;

import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class MistBlockTrapdoor extends BlockTrapDoor {

	private final int flammability;
	private final int fireSpeed;
	private Item door;

	public Item getDoor() {
		return door;
	}

	public void setDoor(Item door) {
		this.door = door;
	}

	public MistBlockTrapdoor(Material material, float hardness, int flammability, int fireSpeed) {
		super(material);
		this.setHardness(hardness);
		this.setSoundType(material == Material.WOOD ? SoundType.WOOD : SoundType.METAL);
        this.flammability = flammability;
		this.fireSpeed = fireSpeed;
	}

	public MistBlockTrapdoor(float hardness, int flammability, int fireSpeed) {
		this(Material.WOOD, hardness, flammability, fireSpeed);
	}

	public MistBlockTrapdoor(float hardness) {
		this(Material.WOOD, hardness, 20, 5);
	}

	public MistBlockTrapdoor(Material material, float hardness) {
		this(material, hardness, 0, 0);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.flammability;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.fireSpeed;
	}
}