package com.lmof;

public class FireColorMap
{
	private static final int COLOR_SPACE_SIZE = 65536;

	private final int[] colorMap = new int[COLOR_SPACE_SIZE];

	public void update(float intensity)
	{
		for (int color = 0; color < COLOR_SPACE_SIZE; color++)
		{
			colorMap[color] = transformColor(color, intensity);
		}
	}

	public int getFireColor(int color)
	{
		if (color < 0 || color >= COLOR_SPACE_SIZE)
		{
			return color;
		}
		return colorMap[color];
	}

	private static int transformColor(int color, float intensity)
	{
		int hue = JagexHSL.hue(color);
		int sat = JagexHSL.saturation(color);
		int lum = JagexHSL.luminance(color);

		int fireHue;
		if (lum > 80)
		{
			fireHue = 8; // yellow
		}
		else if (lum > 40)
		{
			fireHue = 5; // orange
		}
		else
		{
			fireHue = 0; // red
		}

		hue = Math.min(63, Math.max(0, Math.round(hue * (1 - intensity) + fireHue * intensity)));
		sat = Math.min(7, Math.round(sat + 3 * intensity));
		lum = Math.min(127, Math.max(0, Math.round(lum * (1.0f + 0.3f * intensity))));

		return JagexHSL.pack(hue, sat, lum);
	}
}
