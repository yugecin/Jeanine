package net.basdon.jeanine;

public class KeyInput
{
	public char c;
	public boolean error;
	public boolean needRepaint;
	/**Used fe when a line selection, which can span multiple views, is changed.*/
	public boolean needGlobalRepaint;
	/**
	 * ignored when {@link #needRepaint} is already {@code true}
	 */
	public boolean needRepaintCaret;
	public boolean needCheckLineLength;
	public boolean needEnsureViewSize;

	public KeyInput(char c)
	{
		this.c = c;
	}

	public KeyInput()
	{
	}
}
