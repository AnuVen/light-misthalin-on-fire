package com.lmof;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LightMisthalinOnFirePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LightMisthalinOnFirePlugin.class);
		RuneLite.main(args);
	}
}
