package com.lmof;

import java.util.Random;
import java.util.Set;
import net.runelite.api.Actor;
import net.runelite.api.NPC;

public class NpcDialogueReplacer
{
	private static final Set<Integer> NPC_IDS = Set.of(2002, 1838, 1839);
	private static final String[] LINES = {
		"quaaaaaaaaaaaaaaaaaaack.........",
		"QUACK QUACK! QUACK QUACK!",
		"QUACK QUACK! QUACK QUACK quack QUACK!",
		"QUACK! quack QUACK! quack QUACK!"
	};

	private final Random random = new Random();

	public void onOverheadTextChanged(Actor actor)
	{
		if (actor instanceof NPC && NPC_IDS.contains(((NPC) actor).getId()))
		{
			actor.setOverheadText(LINES[random.nextInt(LINES.length)]);
		}
	}
}
