package ru.liahim.mist.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TextureUtilForHook {
	
	public static final float[] COLOR_GAMMAS;
	public static final int[] MIPMAP_BUFFER;

	static {
        COLOR_GAMMAS = new float[256];
        for (int i1 = 0; i1 < COLOR_GAMMAS.length; ++i1) {
            COLOR_GAMMAS[i1] = (float)Math.pow(i1 / 255.0F, 2.2D);
        }
        MIPMAP_BUFFER = new int[4];
    }
	
	public static float getColorGamma(int p_188543_0_) {
        return COLOR_GAMMAS[p_188543_0_ & 255];
    }

	@SideOnly(Side.CLIENT)
	public static int blendColors(int color_0, int color_1, int color_2, int color_3, boolean alpha) {
		if (alpha) {
			TextureUtilForHook.MIPMAP_BUFFER[0] = color_0;
			TextureUtilForHook.MIPMAP_BUFFER[1] = color_1;
			TextureUtilForHook.MIPMAP_BUFFER[2] = color_2;
			TextureUtilForHook.MIPMAP_BUFFER[3] = color_3;
            float a = 0.0F;
            float r = 0.0F;
            float g = 0.0F;
            float b = 0.0F;
            int i = 0;
            for (int j = 0; j < 4; ++j) {
                if (TextureUtilForHook.MIPMAP_BUFFER[j] >> 24 != 0) {
                    a += TextureUtilForHook.getColorGamma(TextureUtilForHook.MIPMAP_BUFFER[j] >> 24);
                    r += TextureUtilForHook.getColorGamma(TextureUtilForHook.MIPMAP_BUFFER[j] >> 16);
                    g += TextureUtilForHook.getColorGamma(TextureUtilForHook.MIPMAP_BUFFER[j] >> 8);
                    b += TextureUtilForHook.getColorGamma(TextureUtilForHook.MIPMAP_BUFFER[j] >> 0);
                    ++i;
                }
            }
            a = a / 4.0F;
            r = r / i;
            g = g / i;
            b = b / i;
            int aInt = (int)(Math.pow(a, 0.45454545454545453D) * 255.0D);
            int rInt = (int)(Math.pow(r, 0.45454545454545453D) * 255.0D);
            int gInt = (int)(Math.pow(g, 0.45454545454545453D) * 255.0D);
            int bInt = (int)(Math.pow(b, 0.45454545454545453D) * 255.0D);
            if (aInt < 96) aInt = 0;
            return aInt << 24 | rInt << 16 | gInt << 8 | bInt;
        } else {
            int a = TextureUtilForHook.blendColorComponent(color_0, color_1, color_2, color_3, 24);
            int r = TextureUtilForHook.blendColorComponent(color_0, color_1, color_2, color_3, 16);
            int g = TextureUtilForHook.blendColorComponent(color_0, color_1, color_2, color_3, 8);
            int b = TextureUtilForHook.blendColorComponent(color_0, color_1, color_2, color_3, 0);
            return a << 24 | r << 16 | g << 8 | b;
        }
	}

	public static int blendColorComponent(int color_0, int color_1, int color_2, int color_3, int shift) {
        float a = TextureUtilForHook.getColorGamma(color_0 >> shift);
        float r = TextureUtilForHook.getColorGamma(color_1 >> shift);
        float g = TextureUtilForHook.getColorGamma(color_2 >> shift);
        float b = TextureUtilForHook.getColorGamma(color_3 >> shift);
        float color = (((float)Math.pow((a + r + g + b) * 0.25D, 0.45454545454545453D)));
        return (int)(color * 255.0D);
    }
}