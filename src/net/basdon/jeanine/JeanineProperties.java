package net.basdon.jeanine;

public class JeanineProperties
{
	private final static char MAX_PROPS = 'z' + 1;

	public static JeanineProperties parse(SB sb, int from)
	{
		JeanineProperties props = new JeanineProperties();
		while (from < sb.length + 2 && sb.value[from] != '*') {
			char c = sb.value[from];
			from += 2;
			if (c < MAX_PROPS && sb.value[from - 1] == ':') {
				props.isPresent[c] = true;
				props.strValue[c] = null;
				props.isValidInt[c] = false;
				props.intValue[c] = 0;
				int length = 0;
				boolean negativeInt = false;
				while (from < sb.length) {
					char d = sb.value[from++];
					if (d == '*') {
						break;
					} else if (d == ';') {
						break;
					} else if ('0' <= d && d <= '9') {
						props.intValue[c] *= 10;
						props.intValue[c] += d - '0';
						if (length == 0 ||
							(length == 1 && negativeInt))
						{
							props.isValidInt[c] = true;
						}
					} else if (length == 0 && d == '-') {
						negativeInt = true;
					} else {
						props.isValidInt[c] = false;
					}
					length++;
				}
				if (negativeInt) {
					props.intValue[c] = -props.intValue[c];
				}
				props.strValue[c] = new String(sb.value, from - 1 - length, length);
			} else {
				while (from < sb.length && sb.value[from] != '*') {
					if (sb.value[from++] == ';') {
						break;
					}
				}
			}
		}
		return props;
	}

	public String[] strValue = new String[MAX_PROPS];
	public int[] intValue = new int[MAX_PROPS];
	public boolean[] isValidInt = new boolean[MAX_PROPS];
	public boolean[] isPresent = new boolean[MAX_PROPS];
}
