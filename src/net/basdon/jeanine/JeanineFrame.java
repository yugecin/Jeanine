package net.basdon.jeanine;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.WindowConstants;

public class JeanineFrame extends JFrame implements KeyListener, CommandBar.Listener
{
	public final CommandBar commandbar;

	private final Jeanine j;
	private final BackgroundPanel contentPane;

	public JeanineFrame(Jeanine j)
	{
		this.j = j;
		this.contentPane = new BackgroundPanel(j, this);

		this.addKeyListener(this);
		this.commandbar = new CommandBar(j);
		this.setContentPane(this.contentPane);
		this.setLocationByPlatform(true);
		this.setTitle("Jeanine");
		this.getLayeredPane().add(this.commandbar, JLayeredPane.POPUP_LAYER);
		CodePanel cp = new CodePanel(this.contentPane);
		cp.setLocation(30, 30);
		cp.setCodeViewSize(20, 20);
		this.add(cp);
		this.setPreferredSize(new Dimension(800, 800));
		this.pack();
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		if (e.getKeyChar() == ':') {
			this.commandbar.show("", this);
		}
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	@Override
	public Component getComponentToFocus()
	{
		return null;
	}

	@Override
	public void acceptCommand(String command)
	{
		if ("aaa".equals(command)) {
			CodePanel cp = new CodePanel(this.contentPane);
			cp.setLocation(30, 30);
			cp.setCodeViewSize(20, 20);
			this.add(cp);
		}
	}
}
