package net.basdon.jeanine;

import java.awt.Point;
import java.util.ArrayList;

public class VimOps
{
	public static char getCharClass(char c)
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
	 * Unlike vim, skips whitespace lines
	 *
	 * @return if no next, point with same coordinates as input
	 */
	public static Point forwardsEx(ArrayList<StringBuilder> lines, int caretx, int carety)
	{
		Point p = new Point(caretx, carety);
		StringBuilder sb = lines.get(carety);
		int length = sb.length();
		char[] line = Line.getValue(sb);
		char currentClass = 3;
		if (caretx < length && length > 0) {
			currentClass = getCharClass(line[caretx]);
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

	/**
	 * Unlike vim, skips whitespace lines
	 *
	 * @return if no next, point with same coordinates as input
	 */
	public static Point forwards(ArrayList<StringBuilder> lines, int caretx, int carety)
	{
		Point p = new Point(caretx, carety);
		StringBuilder sb = lines.get(carety);
		int length = sb.length();
		char[] line = Line.getValue(sb);
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
				length = sb.length();
				line = Line.getValue(sb);
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
	public static Point backwards(ArrayList<StringBuilder> lines, int caretx, int carety)
	{
		Point p = new Point(caretx, carety);
		StringBuilder sb = lines.get(carety);
		int length = sb.length();
		char[] line = Line.getValue(sb);
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
				line = Line.getValue(sb);
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
}
