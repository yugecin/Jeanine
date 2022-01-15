package net.basdon.jeanine;

import java.awt.*;

public class Colors
{
	public static final Colors border = new Colors("color.border");
	/**Use {@code null} to use the IDA-like gradient color.*/
	public static final Colors bg = new Colors("color.bg");
	public static final Colors textFg = new Colors("color.text.fg");
	public static final Colors textBg = new Colors("color.text.bg");
	public static final Colors headerFg = new Colors("color.header.fg");
	public static final Colors headerBg = new Colors("color.header.bg");
	public static final Colors selectionBg = new Colors("color.selection.bg");
	public static final Colors commentFg = new Colors("color.comment.fg");
	public static final Colors whitespaceBg = new Colors("color.whitespace.bg");
	public static final Colors caretDefault = new Colors("color.caret.default");
	public static final Colors caretInsert = new Colors("color.caret.insert");

	public static final Colors[] ALL = {
		border,
		bg,
		textFg,
		textBg,
		headerFg,
		headerBg,
		selectionBg,
		commentFg,
		whitespaceBg,
		caretDefault,
		caretInsert,
	};

	public static void reset()
	{
		border.col = Color.black;
		bg.col = null;
		textFg.col = Color.black;
		textBg.col = Color.white;
		headerFg.col = textFg.col;
		headerBg.col = new Color(0xdddddd);
		selectionBg.col = new Color(0x66AAFF);
		commentFg.col = Color.gray;
		whitespaceBg.col = new Color(0xff8c69);
		caretDefault.col = Color.red;
		caretInsert.col = Color.green;
	}

	public static void blue()
	{
		border.col = Color.black;
		bg.col = new Color(0x001A7B);
		textFg.col = new Color(0x61D6D6);
		textBg.col = new Color(0x00379B);
		headerBg.col = textBg.col.darker();
		headerFg.col = textFg.col;
		selectionBg.col = new Color(0x008000);
		commentFg.col = new Color(0x3A96DD);
		whitespaceBg.col = new Color(0xff8c69);
		caretDefault.col = Color.red;
		caretInsert.col = Color.green;
	}

	public final String name;

	public Color col;

	private Colors(String name)
	{
		this.name = name;
	}
}
