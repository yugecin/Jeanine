package net.basdon.jeanine;

public class Undo
{
	public EditBuffer buffer;
	/**
	 * Whether buffer was in raw mode when this edit was made.
	 */
	public boolean raw;
	/**Start position x of the content that should be replaced with
	{@link #replacement} when performing the undo.*/
	public int fromx;
	/**Start position y of the content that should be replaced with
	{@link #replacement} when performing the undo.*/
	public int fromy;
	/**exclusive*/
	public int tox;
	public int toy;
	/**Caret position before this operation
	(can be different from {@link #fromx} eg when the O command was used).*/
	public int caretx;
	/**Caret position before this operation
	(can be different from {@link #fromy} eg when the O command was used).*/
	public int carety;
	public SB replacement = new SB();
	/**If linked, performing this undo means the previous undo must also be performed*/
	public boolean linkPrevious;

	public Undo(EditBuffer buffer, int caretx, int carety, boolean raw)
	{
		this.buffer = buffer;
		this.tox = this.fromx = this.caretx = caretx;
		this.toy = this.fromy = this.carety = carety;
		this.raw = raw;
	}
}
