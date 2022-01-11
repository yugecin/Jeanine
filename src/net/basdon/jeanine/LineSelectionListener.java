package net.basdon.jeanine;

public interface LineSelectionListener
{
	void lineSelected(Info info);

	public static class Info
	{
		public CodeGroup group;
		public CodePanel panel;
		public SB lineContent;
		public int lineNumber;
	}
}
