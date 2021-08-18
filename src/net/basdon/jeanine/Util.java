package net.basdon.jeanine;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class Util
{
	private static Field stringBuilderValueField;
	private static Field arrayListSizeField;

	static
	{
		try {
			Class<?> clazz = Class.forName("java.lang.AbstractStringBuilder");
			stringBuilderValueField = clazz.getDeclaredField("value");
			stringBuilderValueField.setAccessible(true);

			arrayListSizeField = ArrayList.class.getDeclaredField("size");
			arrayListSizeField.setAccessible(true);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public static char[] getValue(StringBuilder sb)
	{
		try {
			return (char[]) stringBuilderValueField.get(sb);
		} catch (Throwable e) {
			throw new RuntimeException(e);
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
}
