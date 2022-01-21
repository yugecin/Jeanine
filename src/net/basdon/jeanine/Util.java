package net.basdon.jeanine;

import java.util.Iterator;
import java.util.List;

public class Util
{
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

	public static class StringArray2SBIter implements Iterator<SB>
	{
		private String[] array;
		private int pos;
		private SB sb;

		public StringArray2SBIter(String[] array)
		{
			this.sb = new SB(4096);
			this.array = array;
		}

		@Override
		public boolean hasNext()
		{
			return this.pos < array.length;
		}

		@Override
		public SB next()
		{
			this.sb.length = 0;
			this.sb.append(this.array[this.pos++]);
			return this.sb;
		}
	}

	public static class CombinedIter implements Iterator<SB>
	{
		private final List<Iterator<SB>> iters;

		private int idx;

		public CombinedIter(List<Iterator<SB>> iters)
		{
			this.iters = iters;
		}

		@Override
		public boolean hasNext()
		{
			for (;;) {
				if (this.idx >= this.iters.size()) {
					return false;
				}
				boolean res = this.iters.get(this.idx).hasNext();
				if (res) {
					break;
				}
				this.idx++;
			}
			return true;
		}

		@Override
		public SB next()
		{
			return this.iters.get(this.idx).next();
		}
	}
}
