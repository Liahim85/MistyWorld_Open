package ru.liahim.mist.util;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.handlers.FogRenderer;

@SideOnly(Side.CLIENT)
public class FogTexture {

	private static int textureId;
	/** Border offset by chunks */
	public static int offset = Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
	/** Size of the texture by blocks */
	private static int size = (offset * 2 + 1) << 4;
	private static IntBuffer buf = BufferUtils.createIntBuffer(size * size);

	public static int chunkX = Integer.MAX_VALUE, chunkZ = Integer.MAX_VALUE, fogHeight = 0;

	public static void initFogTexture() {
		textureId = TextureUtil.glGenTextures();
		allocateTexture();
	}

	public static int getTextureSize() {
		return size;
	}

	public static void allocateTexture() {
		size = (offset * 2 + 1) << 4;
		buf = BufferUtils.createIntBuffer(size * size);
		TextureUtil.allocateTexture(textureId, size, size);
	}

	public static void bindTexture() {
		GlStateManager.bindTexture(textureId);
	}

	public static void createFogTexture(World world, int chunkX, int chunkZ) {
		chunkX -= offset;
		chunkZ -= offset;
		for (int x = 0; x < size >> 4; ++x) {
			for (int z = 0; z < size >> 4; ++z) {
				createTextureForChunk(world, chunkX + x, chunkZ + z, x << 4, z << 4);
			}
		}
		upload(0, 0, size, size);
	}

	public static void createBlockTexture(World world, BlockPos pos) {
		if (pos.getY() > fogHeight - 16) {
			int chunkX = (pos.getX() >> 4) - 1;
			int chunkZ = (pos.getZ() >> 4) - 1;
			int minX = Integer.MAX_VALUE;
			int minZ = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int maxZ = Integer.MIN_VALUE;
			for (int x = 0; x < 3; ++x) {
				for (int z = 0; z < 3; ++z) {
					if (MathHelper.abs(chunkX + x - FogTexture.chunkX) <= offset) {
						if (MathHelper.abs(chunkZ + z - FogTexture.chunkZ) <= offset) {
							int xOffset = (chunkX + x - FogTexture.chunkX + offset) << 4;
							int zOffset = (chunkZ + z - FogTexture.chunkZ + offset) << 4;
							if (xOffset < minX) minX = xOffset;
							if (zOffset < minZ) minZ = zOffset;
							if (xOffset > maxX) maxX = xOffset;
							if (zOffset > maxZ) maxZ = zOffset;
							createTextureForChunk(world, chunkX + x, chunkZ + z, xOffset, zOffset);
						}
					}
				}
			}
			if (minX < maxX && minZ < maxZ) upload(minX, minZ, maxX - minX + 16, maxZ - minZ + 16);
		}
	}

	public static void createChunkTexture(World world, int chunkX, int chunkZ) {
		if (MathHelper.abs(chunkX - FogTexture.chunkX) <= offset && MathHelper.abs(chunkZ - FogTexture.chunkZ) <= offset) {
			int xOffset = (chunkX - FogTexture.chunkX + offset) << 4;
			int zOffset = (chunkZ - FogTexture.chunkZ + offset) << 4;
			createTextureForChunk(world, chunkX, chunkZ, xOffset, zOffset);
			upload(xOffset, zOffset, 16, 16);
		}
	}

	private static boolean empty = true;

	private static void createTextureForChunk(World world, int chunkX, int chunkZ, int offsetX, int offsetZ) {
		empty = true;
		if (!world.getChunkProvider().provideChunk(chunkX, chunkZ).isEmpty()) {
			int i = FogRenderer.depth < 4 ? -1 : 1;
			ExtendedBlockStorage ebs = world.getChunkFromChunkCoords(chunkX, chunkZ).getBlockStorageArray()[fogHeight >> 4];
			ExtendedBlockStorage ebs1;
			if (fogHeight >> 4 != (fogHeight + i) >> 4) ebs1 = world.getChunkFromChunkCoords(chunkX, chunkZ).getBlockStorageArray()[(fogHeight + i) >> 4];
			else ebs1 = ebs;
			boolean e = ebs1 == Chunk.NULL_BLOCK_STORAGE;
			if (ebs != Chunk.NULL_BLOCK_STORAGE) {
				for (int x = 0; x < 16; ++x) {
					for (int z = 0; z < 16; ++z) {
						int s = ebs.getSkyLight(x, fogHeight & 15, z) * 17;
						int b = ebs.getBlockLight(x, fogHeight & 15, z) * 17;
						int s1 = e ? 255 : ebs1.getSkyLight(x, (fogHeight + i) & 15, z) * 17;
						int b1 = e ? 0 : ebs1.getBlockLight(x, (fogHeight + i) & 15, z) * 17;
						b = (b * b) / 255;
						b1 = (b1 * b1) / 255;
						putToBuffer(x + offsetX, z + offsetZ, b1 << 24 | s << 16 | b << 8 | s1);
					}
				}
				empty = false;
			}
		}
		if (empty) {
			int l = 255 << 16 | 255;
			for (int x = 0; x < 16; ++x) {
				for (int z = 0; z < 16; ++z) {
					putToBuffer(x + offsetX, z + offsetZ, l);
				}
			}
		}
	}

	private static void putToBuffer(int x, int y, int color) {
		buf.put(y * size + x, color);
	}

	private static void setPosition(int x, int y) {
		buf.position(y * size + x);
	}

	private static void upload(int xOffset, int yOffset, int width, int height) {
		GlStateManager.bindTexture(textureId);
		boolean offset = xOffset > 0 || yOffset > 0;
		if (offset) {
			setPosition(xOffset, yOffset);
			GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, size);
		}
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, xOffset, yOffset, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buf);
        if (offset) {
        	setPosition(0, 0);
			GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        }
	}
}