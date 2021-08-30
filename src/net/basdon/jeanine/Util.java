package net.basdon.jeanine;

import java.lang.reflect.Field;
import java.util.ArrayList;

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
}
