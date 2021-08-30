package net.basdon.jeanine;

public class SB implements CharSequence
{
	public char[] value;
	public int length;


	public SB()
	{
		this(32);
	}

	public SB(int capacity)
	{
		this.value = new char[capacity];
	}

	public SB(SB sb)
	{
		this(sb.value, 0, sb.length);
	}

	public SB(String value)
	{
		this(value.length() + 16);
		this.length = value.length();
		value.getChars(0, this.length, this.value, 0);
	}

	public SB(char[] value)
	{
		this(value, 0, value.length);
	}

	public SB(char[] value, int start, int len)
	{
		this.value = new char[len + 16];
		this.append(value, start, len);
	}

	@Override
	public int length()
	{
		return this.length;
	}

	@Override
	public char charAt(int index)
	{
		return this.value[index];
	}

	@Override
	public CharSequence subSequence(int start, int end)
	{
		return new SB(this.value, start, end - start);
	}

	public void ensureCapacity(int cap)
	{
		if (this.value.length < cap) {
			int newlen = this.value.length << 1;
			char[] newvalue = new char[newlen];
			System.arraycopy(this.value, 0, newvalue, 0, this.length);
			this.value = newvalue;
		}
	}

	public SB append(String value)
	{
		int len = value.length();
		this.ensureCapacity(this.length + len);
		value.getChars(0, len, this.value, this.length);
		this.length += len;
		return this;
	}

	public SB append(int value)
	{
		return this.append(String.valueOf(value));
	}

	public SB append(char[] value)
	{
		return this.append(value, 0, value.length);
	}

	public SB append(char[] value, int from, int to)
	{
		int len = to - from;
		this.ensureCapacity(this.length + len);
		System.arraycopy(value, from, this.value, this.length, len);
		this.length += len;
		return this;
	}

	public SB append(SB sb)
	{
		this.append(sb.value, 0, sb.length);
		return this;
	}

	public SB append(char c)
	{
		this.ensureCapacity(this.length + 1);
		this.value[this.length++] = c;
		return this;
	}

	public SB setLength(int length)
	{
		this.ensureCapacity(length);
		this.length = length;
		return this;
	}

	public SB insert(int idx, String str)
	{
		int len = str.length();
		this.ensureCapacity(this.length + len);
		System.arraycopy(this.value, idx, this.value, idx + len, this.length - idx);
		str.getChars(0, len, this.value, idx);
		this.length += len;
		return this;
	}

	public SB insert(int idx, char c)
	{
		this.ensureCapacity(this.length + 1);
		System.arraycopy(this.value, idx, this.value, idx + 1, this.length - idx);
		this.value[idx] = c;
		this.length++;
		return this;
	}

	public SB delete(int from, int to)
	{
		int len = this.length - to;
		System.arraycopy(this.value, to, this.value, from, len);
		this.length = from + len;
		return this;
	}

	public int indexOf(char c, int from)
	{
		for (; from < this.length; from++) {
			if (this.value[from] == c) {
				return from;
			}
		}
		return -1;
	}

	@Override
	public String toString()
	{
		return new String(this.value, 0, this.length);
	}
}