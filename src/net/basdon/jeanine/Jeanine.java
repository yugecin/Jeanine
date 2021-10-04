package net.basdon.jeanine;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Jeanine
{
	public static File[] argsFilesToOpen;
	public static int argsNumFilesToOpen;

	public static void main(String args[])
	{
		Jeanine.argsFilesToOpen = new File[args.length];
		for (int i = 0; i < args.length; i++) {
			Jeanine.argsFilesToOpen[i] = new File(args[i]);
			Jeanine.argsNumFilesToOpen++;
		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable t) {}
		Colors.reset();
		Jeanine j = new Jeanine();
		Preferences.load(j);
		SwingUtilities.invokeLater(() -> new JeanineFrame(j));
	}

	public final ArrayList<Undo> undolist;

	public Font font;
	public String fontFamily;
	public int fontFlags;
	public int fontSize;
	public int fx, fy, fmaxascend;
	public int undolistptr;
	public String pastebuffer = "";
	public char commandBuf[];
	public int commandLength;

	public Jeanine()
	{
		this.fontFamily = "Courier New";
		this.fontFlags = Font.BOLD;
		this.fontSize = 14;
		this.updateFont();
		this.undolist = new ArrayList<>();
		this.commandBuf = new char[100];
		this.commandLength = 100;
	}

	public void updateFont()
	{
		this.font = new Font(this.fontFamily, this.fontFlags, this.fontSize);
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
