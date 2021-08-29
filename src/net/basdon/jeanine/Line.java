package net.basdon.jeanine;

public class Line
{
	public static int visualLength(StringBuilder sb)
	{
		int length = sb.length();
		char[] value = Util.getValue(sb);
		int visualLen = 0;
		for (int i = 0; i < length; i++) {
			if (value[i] == '\t') {
				visualLen += (8 - visualLen % 8);
			} else {
				visualLen++;
			}
		}
		return visualLen;
	}

	public static int logicalToVisualPos(StringBuilder sb, int logicalPos)
	{
		if (logicalPos == 0) {
			return 0;
		}
		int length = sb.length();
		char[] value = Util.getValue(sb);
		int visualPos = 0;
		for (int i = 0; i < length; i++) {
			if (value[i] == '\t') {
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

	public static int visualToLogicalPos(StringBuilder sb, int visualPos)
	{
		int length = sb.length();
		char[] value = Util.getValue(sb);
		for (int i = 0; i < length; i++) {
			if (visualPos <= 0) {
				return i;
			}
			if (value[i] == '\t') {
				visualPos -= (8 - visualPos % 8);
			} else {
				visualPos--;
			}
		}
		return visualPos;
	}

	private static final char[] spaces = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };

	public static String tabs2spaces(StringBuilder sb)
	{
		StringBuilder sb2 = new StringBuilder(sb.capacity());
		int length = sb.length();
		char[] value = Util.getValue(sb);
		int visualPos = 0;
		for (int i = 0; i < length; i++) {
			char c = value[i];
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
