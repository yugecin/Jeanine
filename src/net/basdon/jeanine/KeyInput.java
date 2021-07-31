package net.basdon.jeanine;

public class KeyInput
{
	public char c;
	public boolean consumed;
	public boolean error;
	public boolean needRepaint;
	/**
	 * ignored when {@link #needRepaint} is already {@code true}
	 */
	public boolean needRepaintCaret;
	public boolean needCheckLineLength;
	public boolean needEnsureViewSize;
}
