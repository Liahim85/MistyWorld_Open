package ru.liahim.mist.entity.item;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.item.MistItems;

public class EntityMistPainting extends EntityHanging implements IEntityAdditionalSpawnData {
	
	public EnumArt art;

	public EntityMistPainting(World world) {
		super(world);
	}

	public EntityMistPainting(World world, BlockPos pos, EnumFacing facing, int meta) {
		super(world, pos);
		this.art = EnumArt.values()[meta];
		this.updateFacingWithBoundingBox(facing);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		compound.setString("Motive", this.art.title);
		super.writeEntityToNBT(compound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		String s = compound.getString("Motive");
		for (EnumArt enumart : EnumArt.values()) {
			if (enumart.title.equals(s)) {
				this.art = enumart;
			}
		}
		if (this.art == null) {
			this.art = EnumArt.CLIFF;
		}
		super.readEntityFromNBT(compound);
	}

	@Override
	public int getWidthPixels() {
		return this.art.sizeX;
	}

	@Override
	public int getHeightPixels() {
		return this.art.sizeY;
	}

	@Override
	public void onBroken(@Nullable Entity brokenEntity) {
		if (this.world.getGameRules().getBoolean("doEntityDrops")) {
			this.playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0F, 1.0F);
			if (brokenEntity instanceof EntityPlayer) {
				EntityPlayer entityplayer = (EntityPlayer)brokenEntity;
				if (entityplayer.capabilities.isCreativeMode) {
					return;
				}
			}
			this.entityDropItem(new ItemStack(MistItems.PAINTING, 1, this.art.ordinal()), 0.0F);
		}
	}

	@Override
	public void playPlaceSound() {
		this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0F, 1.0F);
	}

	@Override
	public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
		this.setPosition(x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
		BlockPos blockpos = this.hangingPosition.add(x - this.posX, y - this.posY, z - this.posZ);
		this.setPosition(blockpos.getX(), blockpos.getY(), blockpos.getZ());
	}

	public static enum EnumArt {

		CLIFF("cliff", 16, 16, 0, 0),						// 0 Forest					Savanna	Tropics
		BUSHES("bushes", 16, 16, 16, 0),					// 1 Forest	Cold			Savanna
		FOG_OCEAN("fog_ocean", 16, 16, 32, 0),				// 2 		Cold	Swamp			Tropics
		MORNING("morning", 16, 16, 48, 0),					// 3 Forest			Swamp			Tropics
		WATERING_PLACE("watering_place", 16, 16, 64, 0),	// 4 						Savanna	Tropics
		EARLY_SPRING("early_spring", 16, 16, 80, 0),		// 5 		Cold	Swamp
		HOT_DAY("hot_day", 16, 16, 96, 0),					// 6 						Savanna
		ETUDE("etude", 16, 16, 112, 0),						// 7 		Cold			Savanna
		OAKS("oaks", 32, 16, 0, 32),						// 8 Forest Cold			Savanna
		SOMEWHERE("somewhere", 32, 16, 32, 32),				// 9 		Cold	Swamp			Tropics
		EDGE("edge", 32, 16, 64, 32),						// 10 Forest		Swamp
		SWAMP("swamp", 16, 32, 0, 64),						// 11 Forest		Swamp			Tropics
		MEADOW("meadow", 16, 16, 128, 0),					// 12 Forest				Savanna	Tropics
		FALL("fall", 16, 32, 16, 64),						// 13 Forest Cold	Swamp
		POPLARS("poplars", 16, 16, 144, 0),					// 14 Forest		Swamp
		WANDERER("wanderer", 16, 32, 32, 64);				// 15						Savanna	Tropics

		public static final int MAX_NAME_LENGTH = "SkullAndRoses".length();
		/** Painting Title. */
		public final String title;
		public final int sizeX;
		public final int sizeY;
		public final int offsetX;
		public final int offsetY;

		private EnumArt(String titleIn, int width, int height, int textureU, int textureV) {
			this.title = titleIn;
			this.sizeX = width;
			this.sizeY = height;
			this.offsetX = textureU;
			this.offsetY = textureV;
		}
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeInt(this.art.ordinal());
        buffer.writeInt(this.hangingPosition.getX());
        buffer.writeInt(this.hangingPosition.getY());
        buffer.writeInt(this.hangingPosition.getZ());
        buffer.writeByte(this.getHorizontalFacing().getIndex());
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		this.art = EnumArt.values()[buffer.readInt()];
        this.hangingPosition = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        this.updateFacingWithBoundingBox(EnumFacing.getFront((buffer.readByte())));
	}
}