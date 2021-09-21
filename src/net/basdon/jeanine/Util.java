package net.basdon.jeanine;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

public class Util
{
	private static Field arrayListSizeField;

	static
	{
		try {
			arrayListSizeField = ArrayList.class.getDeclaredField("size");
			arrayListSizeField.setAccessible(true);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public static void setArrayListSize(ArrayList<?> list, int newSize)
	{
		try {
			arrayListSizeField.set(list, newSize);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static Integer parseInt(String s)
	{
		try {
			return Integer.valueOf(Integer.parseInt(s));
		} catch (Throwable t) {
			return null;
		}
	}

	public static class LineIterator implements Iterator<SB>
	{
		private char[] text;
		private int pos;
		private SB sb;
		private boolean hasNext;

		public LineIterator(String text)
		{
			this.text = text.toCharArray();
			this.sb = new SB();
		}

		@Override
		public boolean hasNext()
		{
			if (!this.hasNext && this.pos < this.text.length) {
				this.hasNext = true;
				this.sb.length = 0;
				for (; this.pos < this.text.length; this.pos++) {
					if (this.text[this.pos] == '\n') {
						this.pos++;
						return true;
					} else {
						this.sb.append(this.text[this.pos]);
					}
				}
			}
			return this.hasNext;
		}

		@Override
		public SB next()
		{
			this.hasNext = false;
			return this.sb;
		}
	}

	public static class String2SBIter implements Iterator<SB>
	{
		private Iterator<String> iter;
		private SB sb;

		public String2SBIter(Iterator<String> iter)
		{
			this.sb = new SB(4096);
			this.iter = iter;
		}

		@Override
		public boolean hasNext()
		{
			return this.iter.hasNext();
		}

		@Override
		public SB next()
		{
			this.sb.length = 0;
			this.sb.append(this.iter.next());
			return this.sb;
		}
	}
}
