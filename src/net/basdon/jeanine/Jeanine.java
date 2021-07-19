package net.basdon.jeanine;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Jeanine
{
	public static void main(String args[])
	{
		Line.init();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable t) {}
		SwingUtilities.invokeLater(Jeanine::show);
	}

	public static void show()
	{
		new JeanineFrame(new Jeanine());
	}

	public final Font font;
	public final int fx, fy, fmaxascend;

	public String pastebuffer = "";

	public Jeanine()
	{
		this.font = new Font("Courier New", Font.BOLD, 14);
		FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
		fx = metrics.charWidth('m');
		fy = metrics.getHeight();
		fmaxascend = metrics.getMaxAscent();
	}
}
