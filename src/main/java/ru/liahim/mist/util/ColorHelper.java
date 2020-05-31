package ru.liahim.mist.util;

public class ColorHelper {

	public static int mixColor(int... colors) {
		int r = 0, g = 0, b = 0;
		for (int color : colors) {
			r += color >> 16 & 255;
			g += color >> 8 & 255;
			b += color & 255;
		}
		r /= colors.length;
		g /= colors.length;
		b /= colors.length;
		return r << 16 | g << 8 | b;
	}
}