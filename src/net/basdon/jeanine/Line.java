package net.basdon.jeanine;

public class Line
{
	public static int visualLength(SB sb)
	{
		int visualLen = 0;
		for (int i = 0; i < sb.length; i++) {
			if (sb.value[i] == '\t') {
				visualLen += (8 - visualLen % 8);
			} else {
				visualLen++;
			}
		}
		return visualLen;
	}

	public static int logicalToVisualPos(SB sb, int logicalPos)
	{
		if (logicalPos == 0) {
			return 0;
		}
		int visualPos = 0;
		for (int i = 0; i < sb.length; i++) {
			if (sb.value[i] == '\t') {
				visualPos += (8 - visualPos % 8);
			} else {
				visualPos++;
			}
			if (--logicalPos == 0) {
				return visualPos;
			}
		}
		return visualPos;
	}

	public static int visualToLogicalPos(SB sb, int visualPos)
	{
		for (int i = 0; i < sb.length; i++) {
			if (visualPos <= 0) {
				return i;
			}
			if (sb.value[i] == '\t') {
				visualPos -= (8 - visualPos % 8);
			} else {
				visualPos--;
			}
		}
		return visualPos;
	}

	private static final char[] spaces = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };

	public static String tabs2spaces(SB sb)
	{
		SB sb2 = new SB(sb.value.length + 32);
		int visualPos = 0;
		for (int i = 0; i < sb.length; i++) {
			char c = sb.value[i];
			if (c == '\t') {
				int n = (8 - visualPos % 8);
				visualPos += n;
				sb2.append(spaces, 0, n);
			} else {
				sb2.append(c);
				visualPos++;
			}
		}
		return sb2.toString();
	}
}
