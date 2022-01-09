package net.basdon.jeanine;

import java.util.Arrays;

public class JeanineDialogState extends JeanineState implements LineSelectionListener, Runnable
{
	/**
	 * Convenience method to set all {@link JeanineState} fields and calling
	 * {@link JeanineFrame#pushState(JeanineState)}
	 */
	protected void pushDialogState(JeanineFrame jf, CodeGroup activeGroup, CodeGroup...groups)
	{
		this.lineSelectionListener = this;
		this.postStateLeaveListener = this;
		this.activeGroup = activeGroup;
		this.codegroups = Arrays.asList(groups);
		jf.pushState(this);
	}

	/*LineSelectionListener*/
	@Override
	public void lineSelected(SB line)
	{
	}

	/*Runnable*/
	@Override
	public void run()
	{
	}
}
