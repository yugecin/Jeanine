package net.basdon.jeanine;

import java.awt.Point;
import java.util.ArrayList;

public class VimOps
{
	private static char getCharClass(char c)
	{
		if (('a' <= c && c <= 'z') ||
			('A' <= c && c <= 'Z') ||
			('0' <= c && c <= '9'))
		{
			return 0;
		}
		if (c == ' ' || c == '\t') {
			return 2;
		}
		return 1;
	}

	/**
	 * @return if no next, point with same coordinates as input
	 */
	public static Point nextWord(ArrayList<StringBuilder> lines, int caretx, int carety)
	{
		Point p = new Point(caretx, carety);
		StringBuilder sb = lines.get(carety);
		int length = sb.length();
		char[] line = Line.getValue(sb);
		char currentClass = 3;
		if (caretx < length) {
			currentClass = getCharClass(line[caretx]);
		}

		for (;;) {
			caretx++;
			if (caretx >= length) {
				p.x = caretx - 1;
				if (p.x < 0) {
					p.x = 0;
				}
				carety++;
				if (carety >= lines.size()) {
					return p;
				}
				sb = lines.get(carety);
				length = sb.length();
				line = Line.getValue(sb);
				p.x = 0;
				p.y = carety;
				caretx = -1;
				continue;
			}
			char clazz = getCharClass(line[caretx]);
			if (clazz != 2 && clazz != currentClass) {
				p.x = caretx;
				return p;
			}
			currentClass = clazz;
		}
	}
}
