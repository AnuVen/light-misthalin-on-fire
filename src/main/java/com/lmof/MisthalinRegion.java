package com.lmof;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class MisthalinRegion
{
	private static final Set<Integer> REGION_IDS;

	private static final Set<Integer> WILDERNESS_BORDER_IDS = Set.of(
		12599, 12343, 12855, 13111, 13367
	);

	static
	{
		Set<Integer> ids = new HashSet<>();

		// Lumbridge and surroundings
		ids.addAll(Arrays.asList(12849, 12850, 12851, 12852, 12853, 12854));
		// Mid-Misthalin
		ids.addAll(Arrays.asList(12593, 12594, 12595, 12596, 12597, 12598));
		// Varrock and surroundings
		ids.addAll(Arrays.asList(12337, 12338, 12339, 12340, 12341, 12342));
		// Edgeville / Barbarian Village (partial)
		ids.addAll(Arrays.asList(12082, 12083));
		// Draynor
		ids.addAll(Arrays.asList(12336, 12592));
		// Eastern Misthalin
		ids.addAll(Arrays.asList(13106, 13107, 13108, 13109, 13110, 13364, 13365, 13366));
		// Wilderness border (partial — y=0 only)
		ids.addAll(WILDERNESS_BORDER_IDS);

		REGION_IDS = Collections.unmodifiableSet(ids);
	}

	private MisthalinRegion()
	{
	}

	public static Set<Integer> getRegionIds()
	{
		return REGION_IDS;
	}

	public static boolean isInRegion(int regionId, int worldX, int worldY)
	{
		if (!REGION_IDS.contains(regionId))
		{
			return false;
		}

		if (WILDERNESS_BORDER_IDS.contains(regionId))
		{
			return (worldY & 63) == 0;
		}

		switch (regionId)
		{
			case 12082: return checkRegion12082(worldX & 63, worldY & 63);
			case 12083: return checkRegion12083(worldX & 63, worldY & 63);
			case 12849: return checkRegion12849(worldX & 63, worldY & 63);
			case 13106: return checkRegion13106(worldX & 63, worldY & 63);
			case 13107: return checkRegion13107(worldX & 63, worldY & 63);
			default: return true;
		}
	}

	private static boolean checkRegion12082(int lx, int ly)
	{
		if (lx < 57) return false;
		if (lx == 57) return ly <= 54;
		if (lx == 58) return ly <= 55;
		if (lx == 59) return ly <= 56;
		return true;
	}

	private static boolean checkRegion12083(int lx, int ly)
	{
		if (lx == 60) return (ly <= 3) || (ly >= 33 && ly <= 45);
		if (lx == 61) return (ly <= 8) || (ly >= 21 && ly <= 46) || (ly >= 50 && ly <= 60);
		return lx >= 62;
	}

	private static boolean checkRegion12849(int lx, int ly)
	{
		if (lx == 63) return false;
		if (lx == 62) return ly >= 53;
		if (lx == 61) return ly >= 50;
		if (lx == 60) return ly >= 49;
		if (lx == 59) return ly >= 47;
		if (lx == 58) return ly >= 46;
		if (lx == 57 || lx == 56) return ly >= 41;
		if (lx == 55) return ly >= 40;
		if (lx == 54) return ly >= 39;
		return true;
	}

	private static boolean checkRegion13106(int lx, int ly)
	{
		if (lx == 0) return ly > 0;
		if (lx == 1) return ly > 1 && (ly < 48 || ly > 54);
		if (lx == 2) return ly > 2 && ly < 40;
		if (lx == 3) return ly >= 23 && ly <= 32;
		return false;
	}

	private static boolean checkRegion13107(int lx, int ly)
	{
		if (lx <= 1) return true;
		if (lx == 2) return ly >= 31;
		if (lx == 3) return (ly >= 32 && ly <= 36) || ly >= 58;
		if (lx == 4) return ly >= 59;
		if (lx == 5) return ly >= 60;
		if (lx == 6) return ly >= 61;
		return false;
	}
}
