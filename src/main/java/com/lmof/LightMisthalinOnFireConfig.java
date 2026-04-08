package com.lmof;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("lightMisthalinOnFire")
public interface LightMisthalinOnFireConfig extends Config
{
	@ConfigItem(
		keyName = "addFire",
		name = "Add Fire",
		description = "Spawn fire objects across Misthalin (turn plugin off and on)"
	)
	default boolean addFire()
	{
		return true;
	}

	@ConfigItem(
		keyName = "replaceTreesBushes",
		name = "Replace Trees / Bushes",
		description = "Replace trees and bushes with dead variants (turn plugin off and on)"
	)
	default boolean replaceTreesBushes()
	{
		return true;
	}
}
