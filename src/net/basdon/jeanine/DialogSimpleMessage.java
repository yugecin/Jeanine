package net.basdon.jeanine;

import java.util.Iterator;

public class DialogSimpleMessage extends JeanineDialogState
{
	private final JeanineFrame jf;

	public DialogSimpleMessage(JeanineFrame jf, String title, String...contents)
	{
		this(jf, title, new Util.StringArray2SBIter(contents));
	}

	public DialogSimpleMessage(JeanineFrame jf, String title, Iterator<SB> contents)
	{
		this(jf, title, contents, true);
	}

	public DialogSimpleMessage(
		JeanineFrame jf,
		String title,
		Iterator<SB> contents,
		boolean interpret
	) {
		this.jf = jf;
		CodeGroup group = new CodeGroup(this.jf);
		group.title = title;
		group.buffer.readonly = true;
		group.setContents(contents, interpret);
		this.pushDialogState(jf, group, group);
	}

	/*LineSelectionListener*/
	@Override
	public void lineSelected(LineSelectionListener.Info info)
	{
		this.jf.popState();
	}
}
