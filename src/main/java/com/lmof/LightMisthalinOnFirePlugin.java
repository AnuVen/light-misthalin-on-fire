package com.lmof;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Scene;
import net.runelite.api.SceneTileModel;
import net.runelite.api.SceneTilePaint;
import net.runelite.api.Tile;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.PreMapLoad;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Light Misthalin On Fire"
)
public class LightMisthalinOnFirePlugin extends Plugin
{
	private static final int WATER_TEXTURE = 1;
	private static final int LAVA_TEXTURE = 31;
	private static final float FIRE_INTENSITY = 0.75f;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private LightMisthalinOnFireConfig config;

	private final FireColorMap fireColorMap;
	private final FireSpawner fireSpawner = new FireSpawner();
	private final TreeReplacer treeReplacer = new TreeReplacer();
	private final NpcDialogueReplacer npcDialogueReplacer = new NpcDialogueReplacer();

	{
		fireColorMap = new FireColorMap();
		fireColorMap.update(FIRE_INTENSITY);
	}

	@Override
	protected void startUp()
	{
		log.info("[LMOF] Plugin started!");
		clientThread.invokeLater(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.setGameState(GameState.LOADING);
			}
		});
	}

	@Override
	protected void shutDown()
	{
		log.info("[LMOF] Plugin stopped!");
		clientThread.invokeLater(() ->
		{
			fireSpawner.clearFires();
			treeReplacer.clearDeadTrees();
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.setGameState(GameState.LOADING);
			}
		});
	}

	@Subscribe
	public void onPreMapLoad(PreMapLoad event)
	{
		Scene scene = event.getScene();
		if (scene == null)
		{
			return;
		}

		recolorScene(scene);
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged event)
	{
		npcDialogueReplacer.onOverheadTextChanged(event.getActor());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(() ->
			{
				if (config.addFire())
				{
					fireSpawner.spawnFires(client);
				}
				if (config.replaceTreesBushes())
				{
					treeReplacer.replaceTrees(client);
				}
			});
		}
	}

	private void recolorScene(Scene scene)
	{
		Tile[][][] extTiles = scene.getExtendedTiles();
		Tile[][][] stdTiles = scene.getTiles();

		if (extTiles == null || stdTiles == null)
		{
			return;
		}

		int baseX = scene.getBaseX();
		int baseY = scene.getBaseY();
		int offset = computeExtendedOffset(stdTiles, extTiles);

		int recolored = 0;

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

					recolored += recolorTile(tile);
				}
			}
		}

		log.info("[LMOF] Recolored {} tile surfaces", recolored);
	}

	private int recolorTile(Tile tile)
	{
		int count = 0;

		SceneTilePaint paint = tile.getSceneTilePaint();
		if (paint != null)
		{
			if (paint.getTexture() == WATER_TEXTURE)
			{
				paint.setTexture(LAVA_TEXTURE);
				tile.setSceneTilePaint(paint);
				count++;
			}
			else if (paint.getTexture() == -1)
			{
				paint.setNwColor(fireColorMap.getFireColor(paint.getNwColor()));
				paint.setNeColor(fireColorMap.getFireColor(paint.getNeColor()));
				paint.setSwColor(fireColorMap.getFireColor(paint.getSwColor()));
				paint.setSeColor(fireColorMap.getFireColor(paint.getSeColor()));
				tile.setSceneTilePaint(paint);
				count++;
			}
		}

		SceneTileModel model = tile.getSceneTileModel();
		if (model != null)
		{
			recolorTileModel(model);
			tile.setSceneTileModel(model);
			count++;
		}

		return count;
	}

	private void recolorTileModel(SceneTileModel model)
	{
		int[] colorsA = model.getTriangleColorA();
		int[] colorsB = model.getTriangleColorB();
		int[] colorsC = model.getTriangleColorC();
		int[] textureIds = model.getTriangleTextureId();

		if (colorsA == null || colorsB == null || colorsC == null)
		{
			return;
		}

		for (int i = 0; i < colorsA.length; i++)
		{
			if (textureIds != null && i < textureIds.length)
			{
				if (textureIds[i] == WATER_TEXTURE)
				{
					textureIds[i] = LAVA_TEXTURE;
					continue;
				}
				else if (textureIds[i] != -1)
				{
					continue;
				}
			}

			colorsA[i] = fireColorMap.getFireColor(colorsA[i]);
			colorsB[i] = fireColorMap.getFireColor(colorsB[i]);
			colorsC[i] = fireColorMap.getFireColor(colorsC[i]);
		}
	}

	private static int computeExtendedOffset(Tile[][][] stdTiles, Tile[][][] extTiles)
	{
		int stdSize = stdTiles.length > 0 ? stdTiles[0].length : 0;
		int extSize = extTiles.length > 0 ? extTiles[0].length : 0;
		return (extSize - stdSize) / 2;
	}

	@Provides
	LightMisthalinOnFireConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LightMisthalinOnFireConfig.class);
	}
}
