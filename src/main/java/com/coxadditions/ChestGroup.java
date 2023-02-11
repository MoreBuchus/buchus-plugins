package com.coxadditions;

import java.awt.Color;
import lombok.Getter;
import net.runelite.client.util.ColorUtil;

public enum ChestGroup
{
	//Green
	GROUP_1_1(48, 27, 1, ColorUtil.fromHex("#FF25C54F")),
	GROUP_1_2(47, 26, 1, ColorUtil.fromHex("#FF25C54F")),
	GROUP_1_3(49, 24, 1, ColorUtil.fromHex("#FF25C54F")),
	GROUP_1_4(48, 22, 1, ColorUtil.fromHex("#FF25C54F")),

	//White
	GROUP_2_1(48, 21, 2, ColorUtil.fromHex("#FFE0E0E0")),
	GROUP_2_2(50, 20, 2, ColorUtil.fromHex("#FFE0E0E0")),
	GROUP_2_3(50, 17, 2, ColorUtil.fromHex("#FFE0E0E0")),
	GROUP_2_4(47, 18, 2, ColorUtil.fromHex("#FFE0E0E0")),

	//Red
	GROUP_3_1(51, 26, 3, ColorUtil.fromHex("#FFE03C31")),
	GROUP_3_2(53, 25, 3, ColorUtil.fromHex("#FFE03C31")),
	GROUP_3_3(50, 24, 3, ColorUtil.fromHex("#FFE03C31")),
	GROUP_3_4(52, 21, 3, ColorUtil.fromHex("#FFE03C31")),

	//Yellow
	GROUP_4_1(54, 24, 4, ColorUtil.fromHex("#FFF1FF00")),
	GROUP_4_2(56, 24, 4, ColorUtil.fromHex("#FFF1FF00")),
	GROUP_4_3(57, 23, 4, ColorUtil.fromHex("#FFF1FF00")),
	GROUP_4_4(55, 22, 4, ColorUtil.fromHex("#FFF1FF00")),

	//Purple
	GROUP_5_1(50, 15, 5, ColorUtil.fromHex("#FF6A3DFF")),
	GROUP_5_2(54, 14, 5, ColorUtil.fromHex("#FF6A3DFF")),
	GROUP_5_3(52, 12, 5, ColorUtil.fromHex("#FF6A3DFF")),
	GROUP_5_4(50, 13, 5, ColorUtil.fromHex("#FF6A3DFF")),

	//Mint
	GROUP_6_1(46, 11, 6, ColorUtil.fromHex("#FF00FF9A")),
	GROUP_6_2(48, 11, 6, ColorUtil.fromHex("#FF00FF9A")),
	GROUP_6_3(51, 10, 6, ColorUtil.fromHex("#FF00FF9A")),
	GROUP_6_4(49, 8, 6, ColorUtil.fromHex("#FF00FF9A")),
	GROUP_6_5(46, 9, 6, ColorUtil.fromHex("#FF00FF9A")),

	//Light blue
	GROUP_7_1(55, 13, 7, ColorUtil.fromHex("#FF12C1E5")),
	GROUP_7_2(56, 11, 7, ColorUtil.fromHex("#FF12C1E5")),
	GROUP_7_3(54, 10, 7, ColorUtil.fromHex("#FF12C1E5")),
	GROUP_7_4(56, 9, 7, ColorUtil.fromHex("#FF12C1E5")),

	//Orange
	GROUP_8_1(47, 7, 8, ColorUtil.fromHex("#FFFF9000")),
	GROUP_8_2(49, 7, 8, ColorUtil.fromHex("#FFFF9000")),
	GROUP_8_3(47, 5, 8, ColorUtil.fromHex("#FFFF9000")),
	GROUP_8_4(49, 4, 8, ColorUtil.fromHex("#FFFF9000")),

	//Pink
	GROUP_9_1(58, 14, 9, ColorUtil.fromHex("#FFFF4398")),
	GROUP_9_2(60, 14, 9, ColorUtil.fromHex("#FFFF4398")),
	GROUP_9_3(61, 12, 9, ColorUtil.fromHex("#FFFF4398")),
	GROUP_9_4(60, 11, 9, ColorUtil.fromHex("#FFFF4398")),
	GROUP_9_5(57, 11, 9, ColorUtil.fromHex("#FFFF4398")),
	GROUP_9_6(60, 9, 9, ColorUtil.fromHex("#FFFF4398")),
	GROUP_9_7(58, 8, 9, ColorUtil.fromHex("#FFFF4398")),

	//Magenta
	GROUP_10_1(56, 8, 10, ColorUtil.fromHex("#FFB700FF")),
	GROUP_10_2(57, 6, 10, ColorUtil.fromHex("#FFB700FF")),
	GROUP_10_3(54, 6, 10, ColorUtil.fromHex("#FFB700FF")),
	GROUP_10_4(55, 4, 10, ColorUtil.fromHex("#FFB700FF"));

	@Getter
	private final int regionX;
	@Getter
	private final int regionY;
	@Getter
	private final int group;
	@Getter
	private final Color color;

	ChestGroup(int regionX, int regionY, int group, Color color)
	{
		this.regionX = regionX;
		this.regionY = regionY;
		this.group = group;
		this.color = color;
	}
}
