package net.basdon.jeanine;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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

		this.setFocusable(true);
		this.addKeyListener(this);
		this.commandbar = new CommandBar(j);
		this.commandbar.addListener(this);
		this.setContentPane(this.contentPane);
		this.setLocationByPlatform(true);
		this.setTitle("Jeanine");
		this.getLayeredPane().add(this.commandbar, JLayeredPane.POPUP_LAYER);
		CodeFrame cp = new CodeFrame(this);
		cp.setLocation(30, 30);
		this.add(cp);
		this.setPreferredSize(new Dimension(800, 800));
		this.pack();
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setVisible(true);
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
			CodeFrame cp = new CodeFrame(this);
			cp.setLocation(30, 30);
			cp.setCodeViewSize(20, 20);
			this.add(cp);
			return true;
		}
		return false;
	}
}
