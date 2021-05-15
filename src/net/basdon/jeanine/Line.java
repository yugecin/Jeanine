package net.basdon.jeanine;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This basically only exists to handle tabs.
 */
public class Line
{
	public ArrayList<Segment> segments;

	public Line()
	{
		this.segments = new ArrayList<>();
	}

	public Line(String content)
	{
		this();
		this.insert(0, content);
	}

	public void insert(int logicalPosition, char c)
	{
		if (c == '\t') {
			this.insertTab(logicalPosition);
		} else {
			this.insertImpl(logicalPosition, String.valueOf(c));
		}
	}

	public void insert(int logicalPosition, String str)
	{
		int from = str.length();
		int idx;
		for (;;) {
			idx = str.lastIndexOf('\t', from - 1);
			if (idx == -1) {
				if (from > 0) {
					this.insertImpl(logicalPosition, str.substring(0, from));
				}
				return;
			}
			if (from > idx + 1) {
				this.insertImpl(logicalPosition, str.substring(idx + 1, from));
			}
			this.insertTab(logicalPosition);
			from = idx;
		}
	}

	private void insertTab(int logicalPosition)
	{
		int visualPosition = 0;
		for (int i = 0, max = this.segments.size(); i < max; i++) {
			Segment seg = this.segments.get(i);
			if (logicalPosition == 0) {
				if (seg.isTab()) {
					this.segments.add(i + 1, Segment.tab());
				} else {
					Segment tab = Segment.tab();
					tab.visualLength = 8 - visualPosition % 8;
					this.segments.add(i, tab);
				}
				return;
			} else if (logicalPosition < seg.text.length()) {
				visualPosition += logicalPosition;
				Segment tab = Segment.tab();
				tab.visualLength = 8 - visualPosition % 8;
				this.segments.add(i, tab);
				this.segments.add(i, new Segment(seg.text.substring(0, logicalPosition)));
				seg.text.delete(0, logicalPosition);
				seg.visualLength -= logicalPosition;
				return;
			}
			logicalPosition -= seg.text.length();
			visualPosition += seg.visualLength;
		}
		Segment tab = Segment.tab();
		tab.visualLength = 8 - visualPosition % 8;
		this.segments.add(tab);
	}

	private void insertImpl(int logicalPosition, String str)
	{
		for (int i = 0, max = this.segments.size(); i < max; i++) {
			Segment seg = this.segments.get(i);
			if (logicalPosition < seg.text.length()) {
				if (seg.isTab()) {
					this.segments.add(i, new Segment(str));
					seg.visualLength -= str.length() % 8;
					if (seg.visualLength < 0) {
						seg.visualLength += 8;
					}
					return;
				} else {
					seg.text.insert(logicalPosition, str);
				}
			} else if (!seg.isTab() && logicalPosition == seg.text.length()) {
				seg.text.append(str);
			} else {
				logicalPosition -= seg.text.length();
				continue;
			}
			int change = str.length();
			seg.visualLength += change;
			change %= 8;
			if (change != 0 && i + 1 < max) {
				Segment s = this.segments.get(i + 1);
				assert s.isTab();
				s.visualLength -= change;
				if (s.visualLength <= 0) {
					s.visualLength += 8;
				}
			}
			return;
		}
		this.segments.add(new Segment(str));
	}

	public void delete(int fromLogical, int toLogicalExclusive)
	{
		int lengthLeft = toLogicalExclusive - fromLogical;
		for (int i = 0, max = this.segments.size(); i < max; i++) {
			Segment seg = this.segments.get(i);
			int seglen = seg.text.length();
			if (fromLogical < seglen) {
				if (seg.isTab()) {
					lengthLeft--;
					seg.text = null;
				} else if (fromLogical + lengthLeft > seglen) {
					seg.text.delete(fromLogical, seglen);
					lengthLeft -= seglen - fromLogical;
				} else {
					seg.text.delete(fromLogical, fromLogical + lengthLeft);
					break;
				}
				fromLogical = 0;
			} else {
				fromLogical -= seg.text.length();
			}
		}
		this.fixup();
	}

	/**
	 * Combines text segments, ensures visual lengths of tabs etc etc.
	 * To call after messing with the contents.
	 */
	private void fixup()
	{
		Iterator<Segment> iter = this.segments.iterator();
		Segment prev = null;
		int visualLength = 0;
		while (iter.hasNext()) {
			Segment seg = iter.next();
			if (seg.text == null) {
				iter.remove();
				continue;
			} else if (seg.isTab()) {
				seg.visualLength = 8 - (visualLength % 8);
				visualLength += seg.visualLength;
			} else {
				seg.visualLength = seg.text.length();
				visualLength += seg.visualLength;
				if (prev != null && !prev.isTab()) {
					prev.text.append(seg.text);
					prev.visualLength += seg.text.length();
					visualLength += seg.text.length();
					iter.remove();
					continue;
				}
			}
			prev = seg;
		}
	}

	public int logicalToVisualPosition(int logicalPosition)
	{
		int visualPos = 0;
		for (int i = 0, max = segments.size(); i < max; i++) {
			Segment seg = segments.get(i);
			if (logicalPosition < seg.text.length()) {
				return visualPos + logicalPosition;
			}
			visualPos += seg.visualLength;
			logicalPosition -= seg.text.length();
		}
		return visualPos;
	}

	public int calcVisualLength()
	{
		int visualLen = 0;
		for (int i = 0, max = segments.size(); i < max; i++) {
			visualLen += segments.get(i).visualLength;
		}
		return visualLen;
	}

	public int calcLogicalLength()
	{
		int logicalLen = 0;
		for (int i = 0, max = segments.size(); i < max; i++) {
			logicalLen += segments.get(i).text.length();
		}
		return logicalLen;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0, max = segments.size(); i < max; i++) {
			sb.append(this.segments.get(i).text);
		}
		return sb.toString();
	}

	public static final class Segment
	{
		public static Segment tab()
		{
			Segment seg = new Segment("\t");
			seg.visualLength = 8;
			return seg;
		}

		public StringBuilder text;
		public int visualLength;

		public Segment()
		{
			this.text = new StringBuilder();
		}

		public Segment(String text)
		{
			this.text = new StringBuilder(text);
			this.visualLength = this.text.length();
		}

		public boolean isTab()
		{
			return this.text.length() == 1 && this.text.charAt(0) == '\t';
		}
	}
}
