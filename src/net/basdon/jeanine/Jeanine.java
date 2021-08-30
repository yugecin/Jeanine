package net.basdon.jeanine;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Jeanine
{
	public static void main(String args[])
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable t) {}
		SwingUtilities.invokeLater(Jeanine::show);
	}

	public static void show()
	{
		new JeanineFrame(new Jeanine());
	}

	public final ArrayList<Undo> undolist;

	public Font font;
	public int fx, fy, fmaxascend;
	public int undolistptr;
	public String pastebuffer = "";
	public char commandBuf[];
	public int commandLength;

	public Jeanine()
	{
		this.setFont(new Font("Courier New", Font.BOLD, 14));
		this.undolist = new ArrayList<>();
		this.commandBuf = new char[100];
		this.commandLength = 100;
	}

	public void setFont(Font font)
	{
		this.font = font;
		FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
		this.fx = metrics.charWidth('m');
		this.fy = metrics.getHeight();
		this.fmaxascend = metrics.getMaxAscent();
	}

	public void storeCommand(char[] cmd, int len)
	{
		if (this.commandBuf.length < cmd.length) {
			this.commandBuf = new char[cmd.length];
		}
		System.arraycopy(cmd, 0, this.commandBuf, 0, len);
		this.commandLength = len;
	}
}
