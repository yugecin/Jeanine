package net.basdon.jeanine;

import java.util.Locale;

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
		this(sb.value, 0, sb.length, 16);
	}

	public SB(SB sb, int padding)
	{
		this(sb.value, 0, sb.length, padding);
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

	public SB(char[] value, int start, int to)
	{
		this(value, start, to, 16);
	}

	public SB(char[] value, int start, int to, int padding)
	{
		int len = to - start;
		this.value = new char[len + padding];
		this.append(value, start, to);
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
			int newlen = Math.max(cap, this.value.length << 1);
			char[] newvalue = new char[newlen];
			System.arraycopy(this.value, 0, newvalue, 0, this.length);
			this.value = newvalue;
		}
	}

	public SB lf()
	{
		return this.append('\n');
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

	public SB append(float value, int precision)
	{
		return this.append(String.format(Locale.ENGLISH, "%." + precision + "f", value));
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

	public int indexOf(char[] needle, int from)
	{
		if (needle.length == 0) {
			return -1;
		}
		for (int i = from; i <= this.value.length - needle.length; i++) {
			for (int j = 0;;) {
				if (this.value[i + j] != needle[j]) {
					break;
				}
				j++;
				if (j == needle.length) {
					return i;
				}
			}
		}
		return -1;
	}

	public boolean startsWith(String prefix)
	{
		if (prefix.length() > this.length) {
			return false;
		}
		char[] chars = prefix.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (this.value[i] != chars[i]) {
				return false;
			}
		}
		return true;
	}

	public boolean endsWith(String suffix)
	{
		int len = suffix.length();
		if (len > this.length) {
			return false;
		}
		char[] chars = suffix.toCharArray();
		for (int i = len; i > 0; ) {
			i--;
			if (this.value[this.length - len + i] != chars[i]) {
				return false;
			}
		}
		return true;
	}

	public int lastIndexOf(char[] needle, int from)
	{
		int len = needle.length;
		int offset = this.length - len;
		if (from < offset) {
			offset = from;
		}
		for (; offset >= 0; offset--) {
			for (int i = 0; ; i++) {
				if (i == len) {
					return offset;
				}
				if (this.value[offset + i] != needle[i]) {
					break;
				}
			}
		}
		return -1;
	}

	public int lastIndexOf(String needle)
	{
		int len = needle.length();
		int maxOffset = this.length - len;
		if (maxOffset < 0) {
			return -1;
		}
		char[] chars = needle.toCharArray();
o:
		for (int offset = 0; offset <= maxOffset; offset++) {
			for (int i = len; i > 0; ) {
				i--;
				if (this.value[this.length - len - offset + i] != chars[i]) {
					continue o;
				}
			}
			return this.length - len - offset;
		}
		return -1;
	}

	@Override
	public String toString()
	{
		return new String(this.value, 0, this.length);
	}

	public boolean equals(String value)
	{
		if (value.length() != this.length) {
			return false;
		}
		char[] c = value.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] != this.value[i]) {
				return false;
			}
		}
		return true;
	}
}
