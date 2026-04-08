package com.lmof;

public final class JagexHSL
{
	private JagexHSL()
	{
	}

	public static int hue(int color)
	{
		return (color >> 10) & 0x3F;
	}

	public static int saturation(int color)
	{
		return (color >> 7) & 0x7;
	}

	public static int luminance(int color)
	{
		return color & 0x7F;
	}

	public static int pack(int hue, int saturation, int luminance)
	{
		return (hue << 10) | (saturation << 7) | luminance;
	}
}
