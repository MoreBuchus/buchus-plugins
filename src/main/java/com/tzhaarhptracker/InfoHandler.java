package com.tzhaarhptracker;

import com.google.inject.Singleton;
import javax.inject.Inject;

@Singleton
public abstract class InfoHandler
{
	protected final TzhaarHPTrackerPlugin plugin;
	protected final TzhaarHPTrackerConfig config;

	@Inject
	protected InfoHandler(TzhaarHPTrackerPlugin plugin, TzhaarHPTrackerConfig config)
	{
		this.plugin = plugin;
		this.config = config;
	}

	public void init()
	{
	}

	public void load()
	{
	}

	public void unload()
	{
	}
}
