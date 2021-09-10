package net.basdon.jeanine;

/**
 * Utils for dealing with {@link CodePanel#link}
 */
public class PanelLink
{
	public static final int BOTTOM = 1;
	public static final int TOP = 2;
	public static final char[] links = { 'r', 'b', 't' };

	public static char getAnchor(int link)
	{
		return links[link & 3];
	}

	public static int getLine(int link)
	{
		return link >>> 2;
	}

	public static int createRightLink(int lineNumber)
	{
		return lineNumber << 2;
	}
}
