package com.lmof;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Animation;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

@Slf4j
public class FireSpawner
{
	private static final int FIRE_1X1_MODEL_ID = 26584;
	private static final int FIRE_1X1_ANIM_ID = 6646;
	private static final int FIRE_2X2_MODEL_ID = 26582;
	private static final int FIRE_2X2_ANIM_ID = 6645;

	private static final int REGION_SIZE = 64;
	private static final int SPACING = 8;

	private final List<RuneLiteObject> spawnedFires = new ArrayList<>();

	public void spawnFires(Client client)
	{
		clearFires();

		Model fire1x1Model = client.loadModel(FIRE_1X1_MODEL_ID);
		Model fire2x2Model = client.loadModel(FIRE_2X2_MODEL_ID);
		Animation fire1x1Anim = client.loadAnimation(FIRE_1X1_ANIM_ID);
		Animation fire2x2Anim = client.loadAnimation(FIRE_2X2_ANIM_ID);

		if (fire1x1Model == null || fire2x2Model == null)
		{
			log.warn("[LMOF] Failed to load fire models");
			return;
		}

		for (int regionId : MisthalinRegion.getRegionIds())
		{
			spawnFiresInRegion(client, regionId, fire1x1Model, fire2x2Model, fire1x1Anim, fire2x2Anim);
		}

		log.info("[LMOF] Spawned {} fires", spawnedFires.size());
	}

	private void spawnFiresInRegion(Client client, int regionId,
		Model fire1x1Model, Model fire2x2Model,
		Animation fire1x1Anim, Animation fire2x2Anim)
	{
		int regionBaseX = (regionId >> 8) << 6;
		int regionBaseY = (regionId & 0xFF) << 6;
		Random rand = new Random(regionId);

		for (int rx = 0; rx < REGION_SIZE; rx += SPACING)
		{
			for (int ry = 0; ry < REGION_SIZE; ry += SPACING)
			{
				int worldX = regionBaseX + rx + rand.nextInt(SPACING);
				int worldY = regionBaseY + ry + rand.nextInt(SPACING);
				boolean use2x2 = rand.nextBoolean();

				if (!MisthalinRegion.isInRegion(regionId, worldX, worldY))
				{
					continue;
				}

				LocalPoint lp = LocalPoint.fromWorld(client.getTopLevelWorldView(),
					new WorldPoint(worldX, worldY, 0));
				if (lp == null)
				{
					continue;
				}

				RuneLiteObject fire = client.createRuneLiteObject();
				fire.setModel(use2x2 ? fire2x2Model : fire1x1Model);
				Animation anim = use2x2 ? fire2x2Anim : fire1x1Anim;
				if (anim != null)
				{
					fire.setAnimation(anim);
					fire.setShouldLoop(true);
				}
				fire.setLocation(lp, 0);
				fire.setActive(true);
				spawnedFires.add(fire);
			}
		}
	}

	public void clearFires()
	{
		for (RuneLiteObject fire : spawnedFires)
		{
			fire.setActive(false);
		}
		spawnedFires.clear();
	}
}
