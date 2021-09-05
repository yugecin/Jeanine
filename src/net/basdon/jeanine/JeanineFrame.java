package net.basdon.jeanine;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.WindowConstants;

import static java.awt.KeyboardFocusManager.*;

public class JeanineFrame
extends JFrame
implements KeyListener, MouseListener, MouseMotionListener, CommandBar.Listener
{
	public final CommandBar commandbar;
	public final Jeanine j;

	public List<CodeGroup> codegroups, lastCodegroups;
	public CodeGroup activeGroup, lastActiveGroup;
	public Point location, lastLocation;
	public Point cursorPosBeforeChangingFont;
	public boolean isSelectingFont;

	private Point dragStart;

	public JeanineFrame(Jeanine j)
	{
		this.j = j;
		this.dragStart = new Point();
		this.location = new Point();
		this.codegroups = new ArrayList<>();
		this.setIconImage(this.createLogoImg());
		this.setFocusable(true);
		this.addKeyListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.commandbar = new CommandBar(j);
		this.commandbar.addListener(this);
		this.setContentPane(new BackgroundPanel());
		this.setLocationByPlatform(true);
		this.setError(null);
		this.getLayeredPane().add(this.commandbar, JLayeredPane.POPUP_LAYER);
		this.activeGroup = new CodeGroup(this);
		this.activeGroup.setLocation(30, 30);
		this.activeGroup.setContents(WELCOMETEXT);
		this.codegroups.add(this.activeGroup);
		this.setPreferredSize(new Dimension(800, 800));
		this.pack();
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setVisible(true);
		// make that we get the TAB key events
		KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		fm.setDefaultFocusTraversalKeys(FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
		fm.setDefaultFocusTraversalKeys(BACKWARD_TRAVERSAL_KEYS, Collections.emptySet());
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
		e.consume();
		if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
			return;
		}
		KeyInput event = new KeyInput(e.getKeyChar());
		if (this.activeGroup != null && this.activeGroup.activePanel != null) {
			this.activeGroup.dispatchInputEvent(event, this.activeGroup.activePanel);
			if (!event.error) {
				return;
			}
		}
		if (this.isSelectingFont) {
			Point oldcursorpos = this.findCursorPosition();
			if (event.c == EditBuffer.ESC) {
				this.stopSelectingFont();
				oldcursorpos = cursorPosBeforeChangingFont;
			} else if (event.c == '\n' || event.c == '\r') {
				EditBuffer buffer = this.activeGroup.buffer;
				String fontname = buffer.lines.get(buffer.carety).toString();
				Font font = new Font(fontname, Font.BOLD, 14);
				this.j.setFont(font);
			} else {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			for (CodeGroup group : this.codegroups) {
				group.fontChanged();
			}
			Point newcursorpos = this.findCursorPosition();
			if (newcursorpos != null) {
				if (oldcursorpos != null) {
					this.location.x -= newcursorpos.x - oldcursorpos.x;
					this.location.y -= newcursorpos.y - oldcursorpos.y;
				}
			}
			for (CodeGroup group : this.codegroups) {
				group.updateLocation();
			}
			this.repaint();
			return;
		}
		if (event.c == ':') {
			this.commandbar.show("", this);
			return;
		}
		Toolkit.getDefaultToolkit().beep();
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

	/*MouseMotionListener*/
	@Override
	public void mouseDragged(MouseEvent e)
	{
		int x = e.getX() - this.dragStart.x;
		int y = e.getY() - this.dragStart.y;
		this.location.x += x;
		this.location.y += y;
		this.dragStart.x = e.getX();
		this.dragStart.y = e.getY();

		for (CodeGroup cg : this.codegroups) {
			cg.mouseDragged(x, y);
		}
	}

	/*MouseMotionListener*/
	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	@Override
	/*MouseListener*/
	public void mouseClicked(MouseEvent e)
	{
	}

	/*MouseListener*/
	@Override
	public void mousePressed(MouseEvent e)
	{
		this.dragStart.x = e.getX();
		this.dragStart.y = e.getY();
	}

	/*MouseListener*/
	@Override
	public void mouseReleased(MouseEvent e)
	{
	}

	/*MouseListener*/
	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	/*MouseListener*/
	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	/*CommandBar.Listener*/
	@Override
	public boolean acceptCommand(String command)
	{
		if (this.isSelectingFont) {
			return true;
		}
		if ("bd".equals(command)) {
			if (this.activeGroup == null) {
				this.setError("can't close buffer, no code panel focused");
				return false;
			}
			// TODO: bd
			this.codegroups.remove(this.activeGroup);
			for (CodePanel panel : this.activeGroup.panels.values()) {
				this.getContentPane().remove(panel);
			}
			this.activeGroup.dispose();
			this.activeGroup = null;
			this.repaint();
			return true;
		} else if ("spl".equals(command)) {
			this.activeGroup.split();
			return true;
		} else if ("font".equals(command)) {
			this.startSelectingFont();
			return true;
		}
		this.setError("unknown command: " + command);
		return false;
	}

	public void setError(String error)
	{
		if (error == null || error.isEmpty()) {
			super.setTitle("Jeanine");
		} else {
			super.setTitle("Jeanine - " + error);
			Toolkit.getDefaultToolkit().beep();
		}
	}

	private void startSelectingFont()
	{
		this.cursorPosBeforeChangingFont = this.findCursorPosition();
		this.isSelectingFont = true;
		this.lastActiveGroup = this.activeGroup;
		this.lastCodegroups = this.codegroups;
		this.lastLocation = new Point(this.location);
		this.getContentPane().removeAll();
		StringBuilder sb = new StringBuilder(4096);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fonts = ge.getAvailableFontFamilyNames();
		for (int i = 0;;) {
			sb.append(fonts[i]);
			if (++i < fonts.length) {
				sb.append('\n');
				continue;
			}
			break;
		}
		CodeGroup group = new CodeGroup(this);
		group.buffer.readonly = true;
		group.setContents(sb.toString());
		group.activePanel.setTitle("Font selection");
		group.setLocation(30, 30);
		this.activeGroup = group;
		this.codegroups = Collections.singletonList(group);
		this.repaint();
	}

	private void stopSelectingFont()
	{
		this.isSelectingFont = false;
		this.getContentPane().removeAll();
		this.activeGroup = this.lastActiveGroup;
		this.codegroups = this.lastCodegroups;
		this.location = this.lastLocation;
		this.lastActiveGroup = null;
		this.lastCodegroups = null;
		this.lastLocation = null;
		for (CodeGroup group : this.codegroups) {
			for (CodePanel panel : group.panels.values()) {
				this.getContentPane().add(panel);
			}
		}
		this.repaint();
	}

	private Point findCursorPosition()
	{
		if (this.activeGroup != null) {
			EditBuffer buf = this.activeGroup.buffer;
			CodePanel panel = this.activeGroup.panelAtLine(buf.carety);
			if (panel != null) {
				Point p = panel.getLocation();
				p.x += 3 + this.j.fx * buf.caretx;
				p.y += 3 + this.j.fy * (1 + buf.carety - panel.firstline);
				return p;
			}
		}
		return null;
	}

	private static final String WELCOMETEXT =
		"Welcome to Jeanine, a 2d editor with some Vim-like keybindings\n" +
		"\n" +
		"Movement: h j k l ^ $ w b e gg G\n" +
		"Insertion: i I a A o O p P\n" +
		"Deleting: x dw db diw dd dj dk\n" +
		"Changing: cw cb ciw\n" +
		"Other: . u\n" +
		"Selecting: ctrl-v\n" +
		"\n" +
		"Commands:\n" +
		":spl - split current view based on the visual line selection (ctrl-v)";
}
