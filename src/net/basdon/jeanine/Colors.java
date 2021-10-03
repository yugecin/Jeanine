package net.basdon.jeanine;

import java.awt.*;

public class Colors
{
	public static Color border;
	/**Use {@code null} to use the IDA-like gradient color.*/
	public static Color bg;
	public static Color textFg;
	public static Color textBg;
	public static Color headerFg;
	public static Color headerBg;
	public static Color headerBorder;
	public static Color selectionBg;
	public static Color commentFg;
	public static Color whitespaceBg;

	public static void reset()
	{
		border = Color.black;
		bg = null;
		textFg = Color.black;
		textBg = Color.white;
		headerFg = textFg;
		headerBg = new Color(0xdddddd);
		headerBorder = headerBg;
		selectionBg = new Color(0x66AAFF);
		commentFg = Color.gray;
		whitespaceBg = new Color(0xff8c69);
	}
}
