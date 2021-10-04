package net.basdon.jeanine;

import java.awt.Point;
import java.util.ArrayList;

public class VimOps
{
	public static char getCharClass(char c)
	{
		if (('a' <= c && c <= 'z') ||
			('A' <= c && c <= 'Z') ||
			('0' <= c && c <= '9') || c == '_')
		{
			return 0;
		}
		if (c == ' ' || c == '\t') {
			return 2;
		}
		return 1;
	}

	/**
	 * Unlike vim, skips whitespace lines
	 *
	 * @return if no next, point with same coordinates as input
	 */
	public static Point forwardsEx(ArrayList<SB> lines, int caretx, int carety)
	{
		Point p = new Point(caretx, carety);
		SB sb = lines.get(carety);
		int length = sb.length;
		char currentClass = 3;
		if (caretx < length && length > 0) {
			currentClass = getCharClass(sb.value[caretx]);
		}

		for (;;) {
			caretx++;
			if (caretx >= length) {
				currentClass = 3;
				p.x = length - 1;
				if (p.x < 0) {
					p.x = 0;
				}
				carety++;
				if (carety >= lines.size()) {
					return p;
				}
				sb = lines.get(carety);
				length = sb.length;
				p.x = 0;
				p.y = carety;
				caretx = -1;
				continue;
			}
			char clazz = getCharClass(sb.value[caretx]);
			if (clazz != 2 && clazz != currentClass) {
				p.x = caretx;
				return p;
			}
			currentClass = clazz;
		}
	}

	/**
	 * Unlike vim, skips whitespace lines
	 *
	 * @return if no next, point with same coordinates as input
	 */
	public static Point forwards(ArrayList<SB> lines, int caretx, int carety)
	{
		Point p = new Point(caretx, carety);
		SB sb = lines.get(carety);
		int length = sb.length;
		char[] line = sb.value;
		char currentClass = 3;
		if (caretx < length && length > 0) {
			currentClass = getCharClass(line[caretx]);
			if (caretx + 1 < length &&
				currentClass != 2 &&
				getCharClass(line[caretx + 1]) == currentClass)
			{
				do {
					caretx++;
				} while (caretx + 1 < length && getCharClass(line[caretx + 1]) == currentClass);
				p.x = caretx;
				return p;
			}
		}

		for (;;) {
			caretx++;
			if (caretx >= length) {
				currentClass = 3;
				p.x = length - 1;
				if (p.x < 0) {
					p.x = 0;
				}
				carety++;
				if (carety >= lines.size()) {
					return p;
				}
				sb = lines.get(carety);
				length = sb.length;
				line = sb.value;
				p.x = 0;
				p.y = carety;
				caretx = -1;
				continue;
			}
			char clazz = getCharClass(line[caretx]);
			if (clazz != 2 && clazz != currentClass) {
				while (caretx + 1 < length && getCharClass(line[caretx + 1]) == clazz) {
					caretx++;
				}
				p.x = caretx;
				return p;
			}
			currentClass = clazz;
		}
	}

	/**
	 * Unlike vim, skips whitespace lines
	 *
	 * @return if no next, point with same coordinates as input
	 */
	public static Point backwards(ArrayList<SB> lines, int caretx, int carety)
	{
		Point p = new Point(caretx, carety);
		SB sb = lines.get(carety);
		int length = sb.length;
		char[] line = sb.value;
		char currentClass = 3;
		if (caretx < length && length > 0) {
			currentClass = getCharClass(line[caretx]);
			if (caretx > 0 &&
				currentClass != 2 &&
				getCharClass(line[caretx - 1]) == currentClass)
			{
				do {
					caretx--;
				} while (caretx > 0 && getCharClass(line[caretx - 1]) == currentClass);
				p.x = caretx;
				return p;
			}
		}

		for (;;) {
			caretx--;
			if (caretx < 0) {
				currentClass = 3;
				p.x = 0;
				carety--;
				if (carety < 0) {
					return p;
				}
				sb = lines.get(carety);
				length = sb.length();
				line = sb.value;
				p.x = length - 1;
				if (p.x < 0) {
					p.x = 0;
				}
				p.y = carety;
				caretx = length;
				continue;
			}
			char clazz = getCharClass(line[caretx]);
			if (clazz != 2 && clazz != currentClass) {
				while (caretx > 0 && getCharClass(line[caretx - 1]) == clazz) {
					caretx--;
				}
				p.x = caretx;
				return p;
			}
			currentClass = clazz;
		}
	}

	public static Point getWordUnderCaret(SB line, int caretx)
	{
		char clazz = VimOps.getCharClass(line.value[caretx]);
		int from = caretx;
		int to = caretx + 1;
		while (from > 0 && clazz == VimOps.getCharClass(line.value[from - 1])) {
			from--;
		}
		while (to < line.length() && clazz == VimOps.getCharClass(line.value[to])) {
			to++;
		}
		return new Point(from, to);
	}
}
