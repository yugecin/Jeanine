package net.basdon.jeanine;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.WindowConstants;

public class JeanineFrame extends JFrame implements KeyListener, CommandBar.Listener
{
	public final CommandBar commandbar;
	public final Jeanine j;

	private final BackgroundPanel contentPane;

	public JeanineFrame(Jeanine j)
	{
		this.j = j;
		this.contentPane = new BackgroundPanel(j, this);
		this.setIconImage(this.createLogoImg());
		this.setFocusable(true);
		this.addKeyListener(this);
		this.commandbar = new CommandBar(j);
		this.commandbar.addListener(this);
		this.setContentPane(this.contentPane);
		this.setLocationByPlatform(true);
		this.setTitle("Jeanine");
		this.getLayeredPane().add(this.commandbar, JLayeredPane.POPUP_LAYER);
		CodeFrame cp = new CodeFrame(this, WELCOMETEXT);
		cp.setLocation(30, 30);
		this.add(cp);
		this.setPreferredSize(new Dimension(800, 800));
		this.pack();
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	private Image createLogoImg()
	{
		char[] logodata = (
			"\0\0\0\0\1\2\2\2\2\2\2\2\0\0\0\0" +
			"\0\0\0\1\2\2\2\2\3\3\3\2\2\0\0\0" +
			"\0\0\1\2\2\2\2\3\4\4\5\3\2\2\0\0" +
			"\0\1\2\2\2\2\2\3\6\6\5\3\2\2\2\0" +
			"\1\2\2\2\2\2\2\3\6\6\5\3\2\2\2\2" +
			"\2\2\2\2\2\2\2\3\6\6\5\3\2\2\2\2" +
			"\2\2\2\2\2\2\2\3\6\6\5\3\2\2\2\2" +
			"\2\2\2\2\2\2\2\3\6\6\5\3\2\2\2\2" +
			"\2\2\2\3\3\2\2\3\6\6\5\3\2\2\2\2" +
			"\2\2\3\4\4\3\2\3\6\6\5\3\2\2\2\2" +
			"\2\2\3\6\6\4\3\3\6\6\5\3\2\2\2\2" +
			"\7\2\3\6\6\6\6\6\6\6\5\3\2\2\2\2" +
			"\0\7\3\6\6\6\6\6\6\6\5\3\2\2\2\0" +
			"\0\0\7\3\5\5\5\5\5\5\3\2\2\2\0\0" +
			"\0\0\0\7\3\3\3\3\3\3\2\2\2\0\0\0" +
			"\0\0\0\0\7\2\2\2\2\2\2\2\0\0\0\0" +
			"").toCharArray();
                int logocolors[] = {
			0x00000000, 0xffffbbaa, 0xffff7766, 0xff000000,
			0xffffffff, 0xff888888, 0xffcccccc, 0xffff4433,
		};
                int[] d = new int[256];
                for (int i = 0; i < d.length; i++) {
			d[i] = logocolors[logodata[i]];
                }
		BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB_PRE);
		img.setRGB(0, 0, 16, 16, d, 0, 16);
		return img;
	}

	/*KeyListener*/
	@Override
	public void keyTyped(KeyEvent e)
	{
		if (e.getKeyChar() == ':') {
			this.commandbar.show("", this);
			e.consume();
		}
	}

	/*KeyListener*/
	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	/*KeyListener*/
	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	/*CommandBar.Listener*/
	@Override
	public boolean acceptCommand(String command)
	{
		if ("aaa".equals(command)) {
			CodeFrame cp = new CodeFrame(this, "empty");
			cp.setLocation(30, 30);
			cp.setCodeViewSize(20, 20);
			this.add(cp);
			return true;
		}
		return false;
	}

	private static final String WELCOMETEXT =
		"Welcome to Jeanine, a 2d editor with some Vim-like keybindings\n" +
		"\n" +
		"Movement: hjkl ^ $\n" +
		"Insertion: i I a A o O p P\n" +
		"Deleting: x\n" +
		"Changing: cw cb ciw\n";
}
