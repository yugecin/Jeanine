package net.basdon.jeanine;

public class Undo
{
	public EditBuffer buffer;
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

	public Undo(EditBuffer buffer, int caretx, int carety)
	{
		this.buffer = buffer;
		this.tox = this.fromx = this.caretx = caretx;
		this.toy = this.fromy = this.carety = carety;
	}
}
