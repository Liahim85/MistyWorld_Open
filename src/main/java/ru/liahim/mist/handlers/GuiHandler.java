package ru.liahim.mist.handlers;

import ru.liahim.mist.entity.AbstractMistMount;
import ru.liahim.mist.inventory.container.ContainerFirePitPot;
import ru.liahim.mist.inventory.container.ContainerMask;
import ru.liahim.mist.inventory.container.ContainerMistFurnace;
import ru.liahim.mist.inventory.container.ContainerMountInventory;
import ru.liahim.mist.inventory.container.ContainerRespirator;
import ru.liahim.mist.inventory.container.ContainerUrn;
import ru.liahim.mist.inventory.gui.GuiCampfirePot;
import ru.liahim.mist.inventory.gui.GuiCampfirePotInfo;
import ru.liahim.mist.inventory.gui.GuiMask;
import ru.liahim.mist.inventory.gui.GuiMistFurnace;
import ru.liahim.mist.inventory.gui.GuiMistMount;
import ru.liahim.mist.inventory.gui.GuiRespirator;
import ru.liahim.mist.inventory.gui.GuiSkills;
import ru.liahim.mist.inventory.gui.GuiUrn;
import ru.liahim.mist.tileentity.TileEntityCampfire;
import ru.liahim.mist.tileentity.TileEntityMistFurnace;
import ru.liahim.mist.tileentity.TileEntityUrn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch(ID) {
			case 0: return new ContainerMask(player.inventory, !world.isRemote, player);
			case 1: return new ContainerRespirator(player.inventory);
			case 2: return null;
			case 3: return new ContainerFirePitPot((TileEntityCampfire)world.getTileEntity(new BlockPos(x, y, z)));
			case 4: return null;
			case 5: return ((TileEntityUrn)world.getTileEntity(new BlockPos(x, y, z))).createContainer(player.inventory, player);
			case 6: return new ContainerUrn(null, player);
			case 7: return new ContainerMistFurnace(player.inventory, (TileEntityMistFurnace)world.getTileEntity(new BlockPos(x, y, z)));
			case 8: {
				Entity entity = player.world.getEntityByID(x);
				if (entity instanceof AbstractMistMount) {
					AbstractMistMount mount = (AbstractMistMount)entity;
					return new ContainerMountInventory(player.inventory, mount.horseChest, mount, player);
				}
			}
			default: return null;
		}
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch(ID) {
			case 0: return new GuiMask(player);
			case 1: return new GuiRespirator(player.inventory);
			case 2: return new GuiCampfirePot(player, (TileEntityCampfire)world.getTileEntity(new BlockPos(x, y, z)));
			case 3: return new GuiCampfirePotInfo((TileEntityCampfire)world.getTileEntity(new BlockPos(x, y, z)));
			case 4: return new GuiSkills(player);
			case 5: return new GuiUrn((TileEntityUrn) world.getTileEntity(new BlockPos(x, y, z)), player);
			case 6: return new GuiUrn(null, player);
			case 7: return new GuiMistFurnace(player.inventory, (TileEntityMistFurnace)world.getTileEntity(new BlockPos(x, y, z)));
			case 8: {
				Entity entity = player.world.getEntityByID(x);
				if (entity instanceof AbstractMistMount) {
					AbstractMistMount mount = (AbstractMistMount)entity;
					return new GuiMistMount(player.inventory, new ContainerHorseChest(mount.getDisplayName(), mount.getInventorySize()), mount);
				}
			}
			default: return null;
		}
	}
}