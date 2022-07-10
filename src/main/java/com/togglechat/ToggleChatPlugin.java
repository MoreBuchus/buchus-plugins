/*
 * Copyright (c) 2022, Buchus <http://github.com/MoreBuchus>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.togglechat;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.VarClientStr;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import javax.inject.Inject;
import java.awt.event.KeyEvent;

@Slf4j
@PluginDescriptor(
	name = "Toggle Chat",
	description = "Uses a hotkey to open/close chat",
	tags = {"hotkey", "toggle", "chat"},
	enabledByDefault = false
)
public class ToggleChatPlugin extends Plugin implements KeyListener
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ToggleChatConfig config;

	@Provides
	ToggleChatConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ToggleChatConfig.class);
	}

	@Override
	protected void startUp()
	{
		keyManager.registerKeyListener(this);
	}

	@Override
	protected void shutDown()
	{
		keyManager.unregisterKeyListener(this);
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		if (event.getScriptId() == 179)
		{
			if (config.gameChat())
				client.setVarcIntValue(44, 0);

			if (config.publicChat())
				client.setVarcIntValue(45, 0);

			if (config.privateChat())
				client.setVarcIntValue(46, 0);

			if (config.clanChat())
				client.setVarcIntValue(47, 0);

			if (config.tradeChat())
				client.setVarcIntValue(48, 0);

			if (config.channelChat())
				client.setVarcIntValue(438, 0);
		}
	}

	private void removeHotkey() throws InterruptedException
	{
		String typedText = client.getVarcStrValue(VarClientStr.CHATBOX_TYPED_TEXT);
		if (typedText.length() > 0)
		{
			String subTypedText = typedText.substring(0, typedText.length() - 1);
			char a = (char) KeyEvent.getExtendedKeyCodeForChar(typedText.substring(typedText.length() - 1).toCharArray()[0]);
			char b = (char) config.hotKey().getKeyCode();
			if (a == b)
			{
				client.setVarcStrValue(VarClientStr.CHATBOX_TYPED_TEXT, subTypedText);
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (config.hotKey().matches(e))
		{
			clientThread.invokeLater(() -> {
				try
				{
					removeHotkey();
					//Opens chatbox to the selected tab
					client.runScript(175, 1, config.defaultTab().getTab());
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			});
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}
}


