package net.basdon.jeanine;

public class DialogSimpleMessage extends JeanineDialogState
{
	private final JeanineFrame jf;

	public DialogSimpleMessage(JeanineFrame jf, String title, String...contents)
	{
		this.jf = jf;
		CodeGroup group = new CodeGroup(this.jf);
		group.title = title;
		group.buffer.readonly = true;
		group.setContents(new Util.StringArray2SBIter(contents), true);
		this.pushDialogState(jf, group, group);
	}

	/*LineSelectionListener*/
	@Override
	public void lineSelected(SB line)
	{
		this.jf.popState();
	}
}
