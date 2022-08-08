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
package com.coxanalytics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;
import net.runelite.client.util.Text;

@Slf4j
public class CoxPointsPanel extends PluginPanel
{
	private final CoxAnalyticsPlugin plugin;

	private static final DecimalFormat POINTS_FORMAT = new DecimalFormat("#,###");

	private final JPanel panelContainer = new JPanel();
	private final JPanel titleContainer = new JPanel();

	//Points
	private final JPanel pointsContainer = new JPanel();
	private final JLabel pointsPanelTitleLabel = new JLabel("CoX Points");

	private final JLabel teamPointsLabel = new JLabel("Team Points: 0");
	private final JLabel teamVirtualPointsHourLabel = new JLabel("Virtual Per Hour: 0");
	private final JLabel teamPointsHourLabel = new JLabel("Per Hour: 0");
	private final JLabel avgTeamPointsLabel = new JLabel("Avg. Points: 0");

	private final JLabel soloPointsLabel = new JLabel("Personal Points: 0");
	private final JLabel soloVirtualPointsHourLabel = new JLabel("Virtual Per Hour: 0");
	private final JLabel soloPointsHourLabel = new JLabel("Per Hour: 0");
	private final JLabel avgSoloPointsLabel = new JLabel("Avg. Points: 0");

	private final JLabel raidsCompletedLabel = new JLabel("Total: 0");
	private final JLabel fastestTimeLabel = new JLabel("Fastest Time: 00:00.0");
	private final JLabel lastTimeLabel = new JLabel("Last Time: 00:00.0");
	private final JLabel timeDifLabel = new JLabel("Difference: ");

	//Splits
	private final JPanel splitsContainer = new JPanel();
	private final JLabel splitsPanelTitleLabel = new JLabel("CoX Splits");
	private final JLabel splitsLabel = new JLabel("");

	private final JLabel blankline = new JLabel("<html><br></html>");
	private final JLabel blankline1 = new JLabel("<html><br></html>");

	//Panel buttons
	private final JButton pointsResetButton = new JButton(RESET);
	private final JButton splitsResetButton = new JButton(RESET);
	private static final ImageIcon RESET;
	private static final ImageIcon RESET_HOVER;

	private final JButton pointsClipboardButton = new JButton(CLIPBOARD);
	private final JButton splitsClipboardButton = new JButton(CLIPBOARD);
	private static final ImageIcon CLIPBOARD;
	private static final ImageIcon CLIPBOARD_HOVER;

	//Support
	private final JPanel supportButtons = new JPanel();

	private final JButton discordButton = new JButton(DISCORD);
	private static final ImageIcon DISCORD;
	private static final ImageIcon DISCORD_HOVER;

	private final JButton githubButton = new JButton(GITHUB);
	private static final ImageIcon GITHUB;
	private static final ImageIcon GITHUB_HOVER;

	private final JButton folderButton = new JButton(FOLDER);
	private static final ImageIcon FOLDER;
	private static final ImageIcon FOLDER_HOVER;

	private final Color color = new Color(37, 197, 79);

	private static final String HTML_LABEL_TEMPLATE = "<html><body style='color:%s'><span style='color:white'>%s</span></body></html>";
	private static final String DIFFERENCE_TEMPLATE = "<html><body style=>Difference: <span style=%s>%s</span></body></html>";

	static
	{
		BufferedImage resetImg = ImageUtil.loadImageResource(CoxAnalyticsPlugin.class, "reset_icon.png");
		RESET = new ImageIcon(resetImg);
		RESET_HOVER = new ImageIcon(ImageUtil.luminanceOffset(resetImg, -80));

		BufferedImage clipboardImg = ImageUtil.loadImageResource(CoxAnalyticsPlugin.class, "clipboard.png");
		CLIPBOARD = new ImageIcon(clipboardImg);
		CLIPBOARD_HOVER = new ImageIcon(ImageUtil.luminanceOffset(clipboardImg, -80));

		BufferedImage discordImg = ImageUtil.loadImageResource(CoxAnalyticsPlugin.class, "discord_icon.png");
		DISCORD = new ImageIcon(discordImg);
		DISCORD_HOVER = new ImageIcon(ImageUtil.luminanceOffset(discordImg, -80));

		BufferedImage githubImg = ImageUtil.loadImageResource(CoxAnalyticsPlugin.class, "github_icon.png");
		GITHUB = new ImageIcon(githubImg);
		GITHUB_HOVER = new ImageIcon(ImageUtil.luminanceOffset(githubImg, -80));

		BufferedImage folderImg = ImageUtil.loadImageResource(CoxAnalyticsPlugin.class, "folder.png");
		FOLDER = new ImageIcon(folderImg);
		FOLDER_HOVER = new ImageIcon(ImageUtil.luminanceOffset(folderImg, -80));
	}

	@Inject
	CoxPointsPanel(CoxAnalyticsPlugin plugin)
	{
		this.plugin = plugin;
	}

	void init()
	{
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		panelContainer.setLayout(new BoxLayout(panelContainer, BoxLayout.Y_AXIS));
		panelContainer.add(buildTitleContainer());
		panelContainer.add(buildSupportContainer());
		add(panelContainer, BorderLayout.NORTH);
		add(tabbedPane(), BorderLayout.CENTER);
	}

	//Add title and banner
	private JPanel buildTitleContainer()
	{
		titleContainer.setBorder(new CompoundBorder(new EmptyBorder(5, 0, 8, 0), new MatteBorder(0, 0, 1, 0, color)));
		titleContainer.setLayout(new BorderLayout());
		BufferedImage image = ImageUtil.loadImageResource(getClass(), "banner.png");
		ImageIcon i = new ImageIcon(image);
		JLabel overallIcon = new JLabel(i);
		titleContainer.add(overallIcon, BorderLayout.NORTH);
		return titleContainer;
	}

	//Add panel tabs
	private JTabbedPane tabbedPane()
	{
		JTabbedPane tabPanel = new JTabbedPane();

		JScrollPane pointPanel = wrapContainer(buildPointsPanel());
		JScrollPane splitPanel = wrapContainer(buildSplitsPanel());

		tabPanel.add("Points", pointPanel);
		tabPanel.add("Splits", splitPanel);

		return tabPanel;
	}

	static JScrollPane wrapContainer(final JPanel container)
	{
		final JPanel wrapped = new JPanel(new BorderLayout());
		wrapped.add(container, BorderLayout.NORTH);

		final JScrollPane scroller = new JScrollPane(wrapped);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

		return scroller;
	}

	//Points Container
	private JPanel buildPointsPanel()
	{
		//Title
		JPanel pointsInfoTitlePanel = new JPanel();
		pointsInfoTitlePanel.setLayout(new BorderLayout());
		pointsInfoTitlePanel.setBorder(new CompoundBorder(new EmptyBorder(4, 0, 4, 0), new MatteBorder(0, 0, 1, 0, Color.GRAY)));
		pointsInfoTitlePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel pointsInfoFirst = new JPanel();
		pointsInfoFirst.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		pointsPanelTitleLabel.setFont(FontManager.getRunescapeBoldFont());
		pointsPanelTitleLabel.setForeground(color);
		JPanel pointsInfoSecond = new JPanel(new GridLayout(1, 2, 10, 0));
		pointsInfoSecond.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		pointsClipboardButton.setRolloverIcon(CLIPBOARD_HOVER);
		SwingUtil.removeButtonDecorations(pointsClipboardButton);
		pointsClipboardButton.setPreferredSize(new Dimension(16, 14));
		pointsClipboardButton.setToolTipText("Copy to Clipboard");
		pointsClipboardButton.addActionListener(e -> {
			StringSelection selection = new StringSelection(copyPointsToClipboard());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
		});

		pointsResetButton.setRolloverIcon(RESET_HOVER);
		SwingUtil.removeButtonDecorations(pointsResetButton);
		pointsResetButton.setPreferredSize(new Dimension(16, 14));
		pointsResetButton.setToolTipText("Reset");
		pointsResetButton.addActionListener(e -> plugin.resetPointsPanel());
		pointsInfoFirst.add(pointsPanelTitleLabel);
		pointsInfoSecond.add(pointsClipboardButton);
		pointsInfoSecond.add(pointsResetButton);
		pointsInfoTitlePanel.add(pointsInfoFirst, BorderLayout.WEST);
		pointsInfoTitlePanel.add(pointsInfoSecond, BorderLayout.EAST);

		//Content
		pointsContainer.setLayout(new BorderLayout());
		pointsContainer.setBorder(new EmptyBorder(4, 10, 4, 10));
		pointsContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		JPanel pointsInfobox = new JPanel(new GridBagLayout());
		pointsInfobox.setLayout(new GridLayout(14, 1, 0, 10));
		pointsInfobox.setBorder(new EmptyBorder(4, 0, 4, 0));
		pointsInfobox.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		teamPointsLabel.setFont(FontManager.getRunescapeSmallFont());
		teamVirtualPointsHourLabel.setFont(FontManager.getRunescapeSmallFont());
		teamPointsHourLabel.setFont(FontManager.getRunescapeSmallFont());
		avgTeamPointsLabel.setFont(FontManager.getRunescapeSmallFont());
		blankline.setFont(FontManager.getRunescapeSmallFont());

		soloPointsLabel.setFont(FontManager.getRunescapeSmallFont());
		soloVirtualPointsHourLabel.setFont(FontManager.getRunescapeSmallFont());
		soloPointsHourLabel.setFont(FontManager.getRunescapeSmallFont());
		avgSoloPointsLabel.setFont(FontManager.getRunescapeSmallFont());
		blankline1.setFont(FontManager.getRunescapeSmallFont());

		raidsCompletedLabel.setFont(FontManager.getRunescapeSmallFont());
		fastestTimeLabel.setFont(FontManager.getRunescapeSmallFont());
		lastTimeLabel.setFont(FontManager.getRunescapeSmallFont());
		timeDifLabel.setFont(FontManager.getRunescapeSmallFont());

		pointsInfobox.add(teamPointsLabel);
		pointsInfobox.add(teamVirtualPointsHourLabel);
		pointsInfobox.add(teamPointsHourLabel);
		pointsInfobox.add(avgTeamPointsLabel);
		pointsInfobox.add(blankline);

		pointsInfobox.add(soloPointsLabel);
		pointsInfobox.add(soloVirtualPointsHourLabel);
		pointsInfobox.add(soloPointsHourLabel);
		pointsInfobox.add(avgSoloPointsLabel);
		pointsInfobox.add(blankline1);

		pointsInfobox.add(raidsCompletedLabel);
		pointsInfobox.add(fastestTimeLabel);
		pointsInfobox.add(lastTimeLabel);
		pointsInfobox.add(timeDifLabel);

		pointsContainer.add(pointsInfoTitlePanel, BorderLayout.NORTH);
		pointsContainer.add(pointsInfobox, BorderLayout.WEST);

		return pointsContainer;
	}

	//Splits Container
	private JPanel buildSplitsPanel()
	{
		//Title
		JPanel splitsInfoTitlePanel = new JPanel();
		splitsInfoTitlePanel.setLayout(new BorderLayout());
		splitsInfoTitlePanel.setBorder(new CompoundBorder(new EmptyBorder(4, 0, 4, 0), new MatteBorder(0, 0, 1, 0, Color.GRAY)));
		splitsInfoTitlePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel splitsInfoFirst = new JPanel();
		splitsInfoFirst.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		splitsPanelTitleLabel.setFont(FontManager.getRunescapeBoldFont());
		splitsPanelTitleLabel.setForeground(color);

		JPanel splitsInfoSecond = new JPanel(new GridLayout(1, 2, 10, 0));
		splitsInfoSecond.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		splitsClipboardButton.setRolloverIcon(CLIPBOARD_HOVER);
		SwingUtil.removeButtonDecorations(splitsClipboardButton);
		splitsClipboardButton.setPreferredSize(new Dimension(16, 14));
		splitsClipboardButton.setToolTipText("Copy to Clipboard");
		splitsClipboardButton.addActionListener(e -> {
			StringSelection selection = new StringSelection(copySplitsToClipboard());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
		});

		splitsResetButton.setRolloverIcon(RESET_HOVER);
		SwingUtil.removeButtonDecorations(splitsResetButton);
		splitsResetButton.setPreferredSize(new Dimension(16, 14));
		splitsResetButton.setToolTipText("Reset");
		splitsResetButton.addActionListener(e -> plugin.resetSplitsPanel());

		splitsInfoFirst.add(splitsPanelTitleLabel);
		splitsInfoSecond.add(splitsClipboardButton);
		splitsInfoSecond.add(splitsResetButton);

		splitsInfoTitlePanel.add(splitsInfoFirst, BorderLayout.WEST);
		splitsInfoTitlePanel.add(splitsInfoSecond, BorderLayout.EAST);

		//Content
		splitsContainer.setLayout(new BorderLayout());
		splitsContainer.setBorder(new EmptyBorder(4, 10, 4, 10));
		splitsContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel splitsInfobox = new JPanel(new GridBagLayout());
		splitsInfobox.setLayout(new GridLayout(1, 1, 0, 10));
		splitsInfobox.setBorder(new EmptyBorder(4, 0, 4, 0));
		splitsInfobox.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		splitsLabel.setFont(FontManager.getRunescapeSmallFont());

		splitsInfobox.add(splitsLabel);

		splitsContainer.add(splitsInfoTitlePanel, BorderLayout.NORTH);
		splitsContainer.add(splitsInfobox, BorderLayout.WEST);

		return splitsContainer;
	}

	//Support
	private JPanel buildSupportContainer()
	{
		supportButtons.setLayout(new BorderLayout());
		supportButtons.setBorder(new EmptyBorder(4, 5, 0, 10));
		JPanel supportInfobox = new JPanel(new GridBagLayout());
		supportInfobox.setLayout(new GridLayout(1, 2, 3, 0));
		supportInfobox.setBorder(new EmptyBorder(5, 5, 5, 5));
		supportInfobox.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		folderButton.setRolloverIcon(FOLDER_HOVER);
		folderButton.setToolTipText("Open Times Folder");
		folderButton.setPreferredSize(new Dimension(24, 24));

		discordButton.setRolloverIcon(DISCORD_HOVER);
		discordButton.setToolTipText("Discord");
		discordButton.setPreferredSize(new Dimension(23, 25));

		githubButton.setRolloverIcon(GITHUB_HOVER);
		githubButton.setToolTipText("Github");
		githubButton.setPreferredSize(new Dimension(20, 23));

		SwingUtil.removeButtonDecorations(folderButton);
		SwingUtil.removeButtonDecorations(githubButton);
		SwingUtil.removeButtonDecorations(discordButton);

		folderButton.addActionListener(e -> {
			try
			{
				Desktop.getDesktop().open(CoxAnalyticsPlugin.getTIMES_DIR());
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		});
		githubButton.addActionListener(e -> LinkBrowser.browse("https://github.com/MoreBuchus/buchus-plugins"));
		discordButton.addActionListener(e -> LinkBrowser.browse("https://discord.gg/mfw63tG9js"));

		supportInfobox.add(folderButton);
		supportInfobox.add(githubButton);
		supportInfobox.add(discordButton);

		supportButtons.add(supportInfobox, BorderLayout.CENTER);

		return supportButtons;
	}

	private String copyPointsToClipboard()
	{
		String timeDifString = Text.removeTags(timeDifLabel.getText());
		return teamPointsLabel.getText() + "\r\n" + teamVirtualPointsHourLabel.getText() + "\r\n" + teamPointsHourLabel.getText() + "\r\n" +
			avgTeamPointsLabel.getText() + "\r\n" + soloPointsLabel.getText() + "\r\n" + soloVirtualPointsHourLabel.getText() + "\r\n" +
			soloPointsHourLabel.getText() + "\r\n" + avgSoloPointsLabel.getText() + "\r\n" + raidsCompletedLabel.getText() + "\r\n" +
			fastestTimeLabel.getText() + "\r\n" + lastTimeLabel.getText() + "\r\n" + timeDifString;
	}

	private String copySplitsToClipboard()
	{
		return plugin.getSplits().replace("<br>", "\r\n");
	}

	void setTeamPoints(int points)
	{
		teamPointsLabel.setText(toPoints("Team Points: ", points));
	}

	void setTeamVirtualPointsHour(int points, int ticks)
	{
		teamVirtualPointsHourLabel.setText(toPointsPerHour("Virtual Per Hour: ", points, ticks));
	}

	void setTeamPointsHour(int points, int ticks)
	{
		teamPointsHourLabel.setText(toPointsPerHour("Per Hour: ", points, ticks));
	}

	void setAvgTeamPoints(int points, int kc)
	{
		avgTeamPointsLabel.setText(toAvgPoints(points, kc));
	}

	void setSoloPoints(int points)
	{
		soloPointsLabel.setText(toPoints("Personal Points: ", points));
	}

	void setSoloVirtualPointsHour(int points, int ticks)
	{
		soloVirtualPointsHourLabel.setText(toPointsPerHour("Virtual Per Hour: ", points, ticks));
	}

	void setSoloPointsHour(int points, int ticks)
	{
		soloPointsHourLabel.setText(toPointsPerHour("Per Hour: ", points, ticks));
	}

	void setAvgSoloPoints(int points, int kc)
	{
		avgSoloPointsLabel.setText(toAvgPoints(points, kc));
	}

	void setCompletions(int regKC, int cmKC)
	{
		raidsCompletedLabel.setText(toCompletions(regKC, cmKC));
	}

	void setFastestTime(String time)
	{
		fastestTimeLabel.setText("Fastest Time: " + time);
	}

	void setLastTime(String time)
	{
		lastTimeLabel.setText("Last Time: " + time);
	}

	void setTimeDif(int difference)
	{
		String hexColor = "";
		String time = "";
		if (difference > 0)
		{
			hexColor = "'color:#25c54f'";
			time = "-" + plugin.raidTime(difference);
		}
		else if (difference < 0)
		{
			hexColor = "'color:#e03c31'";
			time = "+" + plugin.raidTime(difference - (difference * 2));
		}
		String timeDif = String.format(DIFFERENCE_TEMPLATE, hexColor, time);
		timeDifLabel.setText(timeDif);
	}

	void setSplits(String splits)
	{
		splitsLabel.setText(toSplits(splits));
	}

	private static String toCompletions(int regKC, int cmKC)
	{
		return regKC + cmKC == 0 ? "Total: 0" : "Total: " + (regKC + cmKC) + "   |   CoX: " + regKC + "   |   CM: " + cmKC;
	}

	private static String toSplits(String splits)
	{
		return String.format(HTML_LABEL_TEMPLATE, ColorUtil.toHexColor(ColorScheme.LIGHT_GRAY_COLOR), splits);
	}

	private static String toPoints(String key, int value)
	{
		String points = POINTS_FORMAT.format(value);
		return key + points;
	}

	private static String toPointsPerHour(String key, int points, int ticks)
	{
		String pph = ticks == 0 ? "0" : POINTS_FORMAT.format((float) (points / ticks) * 6000);
		return key + pph;
	}

	private static String toAvgPoints(int points, int kc)
	{
		String avgPts = kc == 0 ? "0" : POINTS_FORMAT.format((float) (points / kc));
		return "Avg. Points: " + avgPts;
	}
}
