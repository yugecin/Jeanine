package net.basdon.jeanine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class TestRunner
{
	private static final ArrayList<SB> results;

	public static int runs, passed, failures, totalTime, currentPanelId;

	static
	{
		results = new ArrayList<>();
		currentPanelId = 1000;
	}

	public static void run(JeanineFrame jf)
	{
		long time = System.currentTimeMillis();
		TestEditBuffer.jf = jf;
		run(TestEditBuffer.class);
		startNewPanelInResults();
		TestRawGroupConversion.jf = jf;
		run(TestRawGroupConversion.class);
		startNewPanelInResults();
		run(TestLine.class);
		totalTime = (int) (System.currentTimeMillis() - time);
	}

	public static Iterator<SB> getSummary()
	{
		String[] summary = {
			"TEST SUMMARY:",
			String.format("total  %d in %dms", runs, totalTime),
			String.format("passed %d", passed),
			String.format("failed %d", failures),
		};
		return new Util.StringArray2SBIter(summary);
	}

	public static Iterator<SB> getResults()
	{
		return results.iterator();
	}

	private static void startNewPanelInResults()
	{
		int cpi = currentPanelId;
		int npi = ++currentPanelId;
		results.add(new SB("/*jeanine:p:i:" + npi + ";p:" + cpi + ";a:b;y:3.0;*/"));
	}

	private static void run(Class<?> clazz)
	{
		results.add(new SB(String.format("running tests %s", clazz.getSimpleName())));
		long time = System.currentTimeMillis();
		Object inst;
		toplevel: {
			try {
				inst = clazz.getDeclaredConstructor().newInstance();
			} catch (Throwable e) {
				failures++;
				results.add(new SB("FAILED (couldn't instantiate)"));
				e.printStackTrace();
				break toplevel;
			}
			for (Method method : clazz.getDeclaredMethods()) {
				if (Modifier.isPublic(method.getModifiers())) {
					runtest(null, inst, method);
				}
			}
		}
		for (Class<?> inner : clazz.getClasses()) {
			if (Modifier.isStatic(inner.getModifiers())) {
				String simplename = inner.getSimpleName();
				try {
					inst = inner.getDeclaredConstructor().newInstance();
				} catch (Throwable e) {
					results.add(new SB("FAILED (couldn't instantiate)"));
					failures++;
					e.printStackTrace();
					continue;
				}
				for (Method method : inner.getDeclaredMethods()) {
					if (Modifier.isPublic(method.getModifiers())) {
						runtest(simplename, inst, method);
					}
				}
			}
		}
		time = System.currentTimeMillis() - time;
		results.add(new SB(String.format("done in %sms", time)));
	}

	private static void runtest(String className, Object instance, Method method)
	{
		String testname = method.getName();
		if (className != null) {
			testname = className + '.' + testname;
		}
		runs++;
		try {
			method.invoke(instance);
			passed++;
			results.add(new SB(String.format("ok     %s", testname)));
		} catch (Throwable e) {
			failures++;
			results.add(new SB(String.format("FAILED %s", testname)));
			System.err.printf("%nerror while running test %s%n", testname);
			if (e instanceof InvocationTargetException) {
				e = ((InvocationTargetException) e).getCause();
				System.err.println(e.getMessage());
				boolean isAtStackTraceEnd = false;
				String end = instance.getClass().getName();
				for (StackTraceElement ste : e.getStackTrace()) {
					if (end.equals(ste.getClassName()) &&
						method.getName().equals(ste.getMethodName()))
					{
						isAtStackTraceEnd = true;
					} else if (isAtStackTraceEnd) {
						break;
					}
					System.err.println("  at " + ste.toString());
				}
			} else {
				e.printStackTrace();
			}
		}
	}

	public static void assertEquals(String message, int expected, int actual)
	{
		if (expected != actual) {
			message =
				message + ", " +
				"expected " + expected + ", " +
				"got " + actual;
			throw new RuntimeException(message);
		}
	}

	public static void assertEquals(String message, float expected, float actual, float epsilon)
	{
		if (Math.abs(expected - actual) > epsilon) {
			message =
				message + ", " +
				"expected " + expected + ", " +
				"got " + actual;
			throw new RuntimeException(message);
		}
	}

	public static void assertEquals(String message, Object expected, Object actual)
	{
		if (!Objects.equals(expected, actual)) {
			message =
				message + ", " +
				"expected " + String.valueOf(expected) + ", " +
				"got " + String.valueOf(actual);
			throw new RuntimeException(message);
		}
	}

	public static void assertTrue(String message, boolean actual)
	{
		if (!actual) {
			throw new RuntimeException(message);
		}
	}
}
