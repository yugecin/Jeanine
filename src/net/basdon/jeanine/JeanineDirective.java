package net.basdon.jeanine;

/**
 * Parses a jeanine directive as part of a jeanine comment.
 * See {@code readme-jeanine-comments.txt} for the syntax.
 */
public class JeanineDirective
{
	private final static char MAX_PROPS = 'z' + 1;

	/**
	 * @param from offset in {@code sb} where the directive starts (not the comment start)
	 */
	public static JeanineDirective parse(SB sb, int from)
	{
		// val[from+0] is the directive
		// val[from+1] is a colon
		JeanineDirective dir = new JeanineDirective();
		if (!(from < sb.length - 2)) {
			return dir;
		}
		dir.directive = sb.value[from];
		from += 2;
		while (from < sb.length - 2 && sb.value[from] != '*') {
			char c = sb.value[from];
			if (c == '*') { // end of jeanine comment
				break;
			} else if (c == ':') { // end of this directive
				dir.nextDirectiveOffset = from + 1;
				break;
			} else if (c < MAX_PROPS && sb.value[from + 1] == ':') {
				from += 2;
				dir.isPresent[c] = true;
				dir.strValue[c] = null;
				dir.isValidInt[c] = false;
				dir.intValue[c] = 0;
				int length = 0;
				boolean negativeInt = false;
				while (from < sb.length) {
					char d = sb.value[from++];
					if (d == '*') {
						dir.errors.append("col " + from +
							": unexpected comment end\n");
						break;
					} else if (d == ';') {
						break;
					} else if ('0' <= d && d <= '9') {
						dir.intValue[c] *= 10;
						dir.intValue[c] += d - '0';
						if (length == 0 || (length == 1 && negativeInt)) {
							dir.isValidInt[c] = true;
						}
					} else if (length == 0 && d == '-') {
						negativeInt = true;
					} else {
						dir.isValidInt[c] = false;
					}
					length++;
				}
				if (negativeInt) {
					dir.intValue[c] = -dir.intValue[c];
				}
				dir.strValue[c] = new String(sb.value, from - 1 - length, length);
				dir.strLength[c] = length;
			} else {
				dir.errors.append("col " + from + ": unexpected character '" + c
					+ "'," + " skipping to next semicolon\n");
				while (from < sb.length && sb.value[from++] != ';');
			}
		}
		return dir;
	}

	public SB errors = new SB();
	public char directive;
	public String[] strValue = new String[MAX_PROPS];
	public int[] intValue = new int[MAX_PROPS];
	public boolean[] isValidInt = new boolean[MAX_PROPS];
	public boolean[] isPresent = new boolean[MAX_PROPS];
	public int[] strLength = new int[MAX_PROPS];
	/**
	 * Value is {@code 0} when no next directive.
	 */
	public int nextDirectiveOffset = -1;
}
