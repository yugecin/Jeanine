package net.basdon.jeanine;

public class Directive
{
	private final static char HIGHEST_PROP_CHAR = 'z';

	/**
	 * Parses a single directive as part of a jeanine comment.
	 * See {@code readme-jeanine-comments.txt} for the syntax.
	 *
	 * @param from offset in {@code sb} where the directive starts (not the comment start),
	 * 	e.g. given the line {@code /*jeanine:p:i:1;:s:b:8;*\/}: from should be 10 and
	 * 	the retuned instance's {@link Directive#nextDirectiveOffset} will be set to 17.
	 */
	public static Directive parse(SB sb, int from)
	{
		// val[from+0] is the directive
		// val[from+1] is a colon
		Directive dir = new Directive();
		if (!(from < sb.length - 2)) {
			return dir;
		}
		dir.directive = sb.value[from];
		from += 2;
		while (from < sb.length - 2) {
			char c = sb.value[from];
			if (c == '*' && sb.value[from + 1] == '/') { // end of jeanine comment
				break;
			} else if (c == ':') { // end of this directive
				dir.nextDirectiveOffset = from + 1;
				break;
			} else if (c <= HIGHEST_PROP_CHAR && sb.value[from + 1] == ':') {
				from += 2;
				dir.isPresent[c] = true;
				dir.strValue[c] = null;
				dir.isValidInt[c] = false;
				dir.isValidFloat[c] = false;
				dir.intValue[c] = 0;
				int length = 0;
				boolean negativeInt = false;
				while (from < sb.length) {
					char d = sb.value[from++];
					if (d == '*' && (from >= sb.length || sb.value[from] == '/')) {
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
						dir.isValidFloat[c] = d == '.' && dir.isValidInt[c];
						dir.isValidInt[c] = false;
					}
					length++;
				}
				if (negativeInt) {
					dir.intValue[c] = -dir.intValue[c];
				}
				dir.strValue[c] = new String(sb.value, from - 1 - length, length);
				dir.strLength[c] = length;
				if (dir.isValidFloat[c]) {
					dir.floatValue[c] = Float.parseFloat(dir.strValue[c]);
				}
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
	public String[] strValue = new String[HIGHEST_PROP_CHAR + 1];
	public int[] intValue = new int[HIGHEST_PROP_CHAR + 1];
	public float[] floatValue = new float[HIGHEST_PROP_CHAR + 1];
	public boolean[] isValidInt = new boolean[HIGHEST_PROP_CHAR + 1];
	public boolean[] isValidFloat = new boolean[HIGHEST_PROP_CHAR + 1];
	public boolean[] isPresent = new boolean[HIGHEST_PROP_CHAR + 1];
	public int[] strLength = new int[HIGHEST_PROP_CHAR + 1];
	/**
	 * Value is {@code -1} when no next directive.
	 * See doc of {@link #parse} for example behavior.
	 */
	public int nextDirectiveOffset = -1;
}
