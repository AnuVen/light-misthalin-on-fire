package com.lmof;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;

@Slf4j
public class TreeReplacer
{
	// Dead tree model IDs
	private static final int DEAD_TREE_A = 1716;
	private static final int DEAD_TREE_B = 1715;
	private static final int DEAD_STUMP = 33665;

	// Tree object IDs -> dead tree A (model 1716)
	private static final Set<Integer> TREES_TO_DEAD_A = Set.of(1276, 10041);

	// Tree object IDs -> dead tree B (model 1715)
	private static final Set<Integer> TREES_TO_DEAD_B = Set.of(
		1278,
		// Yew trees
		10822, 10823, 10828,
		// Willow trees
		10819, 10829, 10831, 10833
	);

	// Oak tree object IDs -> dead stump
	private static final Set<Integer> OAKS_TO_STUMP = Set.of(10820);

	// Bush object IDs -> random darkened dead bush
	private static final Set<Integer> BUSH_IDS = Set.of(
		1118, 1124, 1173, 1187, 1188, 1189, 1192, 1196,
		1298, 1299, 1300, 1301, 1390, 1391, 1392, 1393, 1394,
		2357, 2409, 5791,
		10586, 10778, 17039,
		29660, 29661, 29715,
		42835, 44782, 50667, 50668, 50669
	);

	// Grass ground object IDs to hide
	private static final Set<Integer> GRASS_IDS = Set.of(
		4735, 4736, 4737, 4738, 4739, 4740, 4741, 4742,
		5533, 5534, 5536, 5545
	);

	// Dead bush model IDs (randomly selected per bush)
	private static final int[] DEAD_BUSH_MODELS = {7916, 7915, 8045, 8164, 8165};

	private final List<RuneLiteObject> replacements = new ArrayList<>();

	public void replaceTrees(Client client)
	{
		clearDeadTrees();

		WorldView wv = client.getTopLevelWorldView();
		if (wv == null)
		{
			return;
		}

		Scene scene = wv.getScene();
		if (scene == null)
		{
			return;
		}

		Model deadModelA = client.loadModel(DEAD_TREE_A);
		Model deadModelB = client.loadModel(DEAD_TREE_B);
		Model stumpModel = client.loadModel(DEAD_STUMP);

		Model[] deadBushModels = loadDarkenedBushModels(client);

		if (deadModelA == null || deadModelB == null || stumpModel == null)
		{
			log.warn("[LMOF] Failed to load dead tree models");
			return;
		}

		Random rand = new Random(42);

		int baseX = scene.getBaseX();
		int baseY = scene.getBaseY();

		Tile[][][] extTiles = scene.getExtendedTiles();
		Tile[][][] stdTiles = scene.getTiles();
		if (extTiles == null || stdTiles == null)
		{
			return;
		}

		int stdSizeX = stdTiles.length > 0 ? stdTiles[0].length : 0;
		int extSizeX = extTiles.length > 0 ? extTiles[0].length : 0;
		int offset = (extSizeX - stdSizeX) / 2;

		int replaced = 0;

		for (int z = 0; z < extTiles.length; z++)
		{
			for (int x = 0; x < extTiles[z].length; x++)
			{
				for (int y = 0; y < extTiles[z][x].length; y++)
				{
					Tile tile = extTiles[z][x][y];
					if (tile == null)
					{
						continue;
					}

					int worldX = baseX + x - offset;
					int worldY = baseY + y - offset;
					int regionId = ((worldX >> 6) << 8) | (worldY >> 6);
					if (!MisthalinRegion.isInRegion(regionId, worldX, worldY))
					{
						continue;
					}

					// Hide grass
					GroundObject groundObj = tile.getGroundObject();
					if (groundObj != null && GRASS_IDS.contains(groundObj.getId()))
					{
						tile.setGroundObject(null);
					}

					// Replace trees/bushes
					GameObject[] gameObjects = tile.getGameObjects();
					if (gameObjects == null)
					{
						continue;
					}

					for (GameObject obj : gameObjects)
					{
						if (obj == null)
						{
							continue;
						}

						int id = obj.getId();
						Model deadModel = resolveDeadModel(id, deadModelA, deadModelB, stumpModel, deadBushModels, rand);
						if (deadModel == null)
						{
							continue;
						}

						LocalPoint lp = obj.getLocalLocation();
						if (lp == null)
						{
							continue;
						}

						scene.removeGameObject(obj);

						RuneLiteObject replacement = client.createRuneLiteObject();
						replacement.setModel(deadModel);
						replacement.setLocation(lp, z);
						replacement.setActive(true);
						replacements.add(replacement);
						replaced++;
					}
				}
			}
		}

		log.info("[LMOF] Replaced {} trees/bushes", replaced);
	}

	private static Model resolveDeadModel(int objectId, Model deadA, Model deadB, Model stump,
		Model[] bushModels, Random rand)
	{
		if (TREES_TO_DEAD_A.contains(objectId))
		{
			return deadA;
		}
		if (TREES_TO_DEAD_B.contains(objectId))
		{
			return deadB;
		}
		if (OAKS_TO_STUMP.contains(objectId))
		{
			return stump;
		}
		if (BUSH_IDS.contains(objectId))
		{
			return bushModels[rand.nextInt(bushModels.length)];
		}
		return null;
	}

	private static Model[] loadDarkenedBushModels(Client client)
	{
		Model[] models = new Model[DEAD_BUSH_MODELS.length];
		for (int i = 0; i < DEAD_BUSH_MODELS.length; i++)
		{
			ModelData md = client.loadModelData(DEAD_BUSH_MODELS[i]);
			if (md != null)
			{
				md = md.shallowCopy().cloneColors();
				darkenModel(md);
				models[i] = md.light();
			}
		}
		return models;
	}

	public void clearDeadTrees()
	{
		for (RuneLiteObject obj : replacements)
		{
			obj.setActive(false);
		}
		replacements.clear();
	}

	private static void darkenModel(ModelData md)
	{
		short[] faceColors = md.getFaceColors();
		if (faceColors == null)
		{
			return;
		}

		for (int i = 0; i < faceColors.length; i++)
		{
			int color = faceColors[i] & 0xFFFF;
			int hue = JagexHSL.hue(color);
			int sat = Math.max(0, JagexHSL.saturation(color) - 2);
			int lum = JagexHSL.luminance(color) / 6;

			faceColors[i] = (short) JagexHSL.pack(hue, sat, lum);
		}
	}
}
