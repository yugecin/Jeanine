package net.basdon.jeanine;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import static java.awt.KeyboardFocusManager.*;

public class JeanineFrame
extends JFrame
implements KeyListener, MouseListener, MouseMotionListener, ActionListener
{
	private final Timer timer;
	private final CodeGroup welcomeCodeGroup;

	public final CommandBar commandbar;
	public final OverlayPanel overlay;
	public final Jeanine j;

	public List<CodeGroup> codegroups, lastCodegroups;
	public CodeGroup activeGroup, lastActiveGroup;
	public Point location, lastLocation;
	public Point cursorPosBeforeChangingFont;
	public boolean isSelectingFont;

	private Point locationMoveFrom, locationMoveTo;
	private long locationMoveStartTime;
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
		this.setContentPane(new BackgroundPanel());
		this.setGlassPane(this.overlay = new OverlayPanel(this));
		this.overlay.setVisible(true);
		this.setLocationByPlatform(true);
		this.setError(null);
		this.getLayeredPane().add(this.commandbar, JLayeredPane.POPUP_LAYER);
		this.activeGroup = this.welcomeCodeGroup = new CodeGroup(this);
		this.activeGroup.setLocation(30, 30);
		this.activeGroup.setContents(new Util.LineIterator(WELCOMETEXT), true);
		this.codegroups.add(this.activeGroup);
		this.setPreferredSize(new Dimension(800, 800));
		this.pack();
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.ensureCaretInView();
		this.timer = new Timer(25, this);
		this.timer.start();
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
		if (this.commandbar.active) {
			this.commandbar.handleKey(event.c);
			if (!this.commandbar.active) {
				if (this.commandbar.cmd.length() != 0) {
					this.acceptCommand(this.commandbar.cmd.toString());
				}
				this.repaintActivePanel();
			}
			return;
		}
		if (this.activeGroup != null && this.activeGroup.activePanel != null) {
			this.activeGroup.dispatchInputEvent(event, this.activeGroup.activePanel);
			this.ensureCaretInView();
			if (!event.error) {
				return;
			}
		}
		if (event.c == 'z') {
			this.centerCaret();
			return;
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
			this.moveToGetCursorAtPosition(oldcursorpos);
			return;
		}
		if (this.activeGroup != null) {
			EditBuffer buffer;
			switch (event.c) {
			case ':':
				this.commandbar.show("");
				this.repaintActivePanel();
				return;
			case 'l':
				buffer = this.activeGroup.buffer;
				if (buffer.mode != EditBuffer.NORMAL_MODE) {
					break;
				}
				for (CodePanel panel : this.activeGroup.panels.values()) {
					if (panel.parent == this.activeGroup.activePanel &&
						PanelLink.getAnchor(panel.link) == 'r' &&
						PanelLink.getLine(panel.link) == buffer.carety)
					{
						buffer.carety = panel.firstline;
						buffer.caretx = 0;
						buffer.virtualCaretx = 0;
						this.activeGroup.activePanel = panel;
						this.ensureCaretInView();
						panel.repaint();
						panel.parent.repaint();
						return;
					}
				}
				break;
			case 'h':
				buffer = this.activeGroup.buffer;
				if (buffer.mode != EditBuffer.NORMAL_MODE) {
					break;
				}
				CodePanel panel = this.activeGroup.activePanel;
				if (panel != null && panel.parent != null &&
					PanelLink.getAnchor(panel.link) == 'r' &&
					panel.firstline == buffer.carety)
				{
					buffer.carety = PanelLink.getLine(panel.link);
					buffer.caretx = buffer.lines.get(buffer.carety).length - 1;
					if (buffer.caretx < 0) {
						buffer.caretx = 0;
					}
					buffer.virtualCaretx = buffer.caretx;
					this.activeGroup.activePanel = panel.parent;
					this.ensureCaretInView();
					panel.repaint();
					panel.parent.repaint();
					return;
				}
				break;
			}
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
		if (this.shouldBlockInput()) {
			return;
		}
		int x = e.getX() - this.dragStart.x;
		int y = e.getY() - this.dragStart.y;
		this.location.x += x;
		this.location.y += y;
		this.dragStart.x = e.getX();
		this.dragStart.y = e.getY();

		for (CodeGroup cg : this.codegroups) {
			cg.updateLocation();
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

	/*ActionListener*/
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (this.timer == e.getSource()) {
			if (locationMoveFrom != null) {
				long time = System.currentTimeMillis() - locationMoveStartTime;
				if (time >= 300) {
					this.location = this.locationMoveTo;
					this.locationMoveFrom = null;
					this.locationMoveTo = null;
				} else {
					double t = -Math.pow(2, -10 * (time / 300.0f)) + 1;
					int dx = this.locationMoveTo.x - this.locationMoveFrom.x;
					int dy = this.locationMoveTo.y - this.locationMoveFrom.y;
					this.location.x = this.locationMoveFrom.x + (int) (dx * t);
					this.location.y = this.locationMoveFrom.y + (int) (dy * t);
				}
				for (CodeGroup group : this.codegroups) {
					group.updateLocation();
				}
			}
		}
	}

	public boolean shouldBlockInput()
	{
		return this.commandbar.active;
	}

	public void acceptCommand(String command)
	{
		if (this.isSelectingFont) {
			return;
		}
		String[] parts = command.split(" ");
		if ("bd".equals(parts[0])) {
			if (this.activeGroup == null) {
				this.setError("can't close buffer, no active panel");
				return;
			}
			// TODO: bd
			this.codegroups.remove(this.activeGroup);
			for (CodePanel panel : this.activeGroup.panels.values()) {
				this.getContentPane().remove(panel);
			}
			this.activeGroup.dispose();
			this.activeGroup = null;
			this.repaint();
		} else if ("spl".equals(parts[0])) {
			this.activeGroup.split();
		} else if ("font".equals(parts[0])) {
			this.startSelectingFont();
		} else if ("link".equals(parts[0])) {
			Integer childId;
			String position;
			if (parts.length != 3 ||
				(!"bot".equals(position = parts[1]) &&
				!"right".equals(position) &&
				!"top".equals(position)) ||
				(childId = Util.parseInt(parts[2])) == null)
			{
				this.setError("syntax: :link <bot|right|top> <id>");
			} else if (this.activeGroup == null) {
				this.setError("can't rechild, no active group or panel");
			} else {
				this.activeGroup.reChild(childId, position);
			}
		} else if ("raw".equals(parts[0])) {
			if (this.activeGroup == null) {
				this.setError("no active panel");
			} else {
				this.activeGroup.toggleRaw();
			}
		} else if ("e".equals(parts[0])) {
			FileDialog dlg = new FileDialog(this, "Open file");
			dlg.setVisible(true);
			File[] files = dlg.getFiles();
			if (files.length == 0) {
				return;
			}
			if (this.activeGroup == this.welcomeCodeGroup) {
				this.codegroups.remove(this.activeGroup);
				this.activeGroup.dispose();
			}
			CodeGroup lastActiveGroup = this.activeGroup;
			this.activeGroup = null;
			if (lastActiveGroup != null) {
				if (lastActiveGroup.activePanel != null) {
					lastActiveGroup.activePanel.repaint(); // for cursor
				}
			}
			for (File file : files) {
				this.activeGroup = new CodeGroup(this);
				try {
					this.activeGroup.readFile(file);
				} catch (IOException e) {
					// TODO
					e.printStackTrace();
				}
				this.codegroups.add(this.activeGroup);
			}
			this.ensureCaretInView();
		} else if ("w".equals(parts[0])) {
			if (this.activeGroup == null) {
				this.setError("no active panel");
			} else {
				try {
					this.activeGroup.saveFile();
				} catch (IOException e) {
					// TODO
					e.printStackTrace();
				}
			}
		} else {
			this.setError("unknown command: " + parts[0]);
		}
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
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fonts = ge.getAvailableFontFamilyNames();
		CodeGroup group = new CodeGroup(this);
		group.title = "Font selection";
		group.buffer.readonly = true;
		group.setContents(new Iterator<SB>()
		{
			private int i;
			private SB sb = new SB();

			@Override
			public boolean hasNext()
			{
				return i < fonts.length;
			}

			@Override
			public SB next()
			{
				this.sb.length = 0;
				this.sb.append(fonts[this.i++]);
				return this.sb;
			}
		}, true);
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

	public Point findCursorPosition()
	{
		if (this.activeGroup != null) {
			EditBuffer buf = this.activeGroup.buffer;
			CodePanel panel = this.activeGroup.panelAtLine(buf.carety);
			if (panel != null) {
				Point p = panel.getLocation();
				p.x +=
					/*border*/ 1 +
					/*padding*/ 1 +
					this.j.fx * buf.caretx;
				p.y +=
					/*border*/ 1 +
					/*padding title up/down*/ 1 +
					/*title*/ this.j.fy +
					/*padding content*/ 1 +
					this.j.fy * (buf.carety - panel.firstline);
				return p;
			}
		}
		return null;
	}

	public void moveToGetCursorAtPosition(Point pos)
	{
		Point newcursorpos = this.findCursorPosition();
		if (newcursorpos != null && pos != null) {
			this.location.x -= newcursorpos.x - pos.x;
			this.location.y -= newcursorpos.y - pos.y;
			for (CodeGroup group : this.codegroups) {
				group.updateLocation();
			}
			this.getGlassPane().repaint();
		}
	}

	private void ensureCaretInView()
	{
		Point pt = this.findCursorPosition();
		if (pt == null) {
			return;
		}
		Dimension size = this.getContentPane().getSize();
		int padx = (int) (size.width / 3f);
		int pady = size.height / 8;
		int dx = padx - pt.x;
		if (dx < 0) {
			dx = (size.width - padx) - pt.x;
			if (dx > 0) {
				dx = 0;
			}
		}
		int dy = pady - pt.y;
		if (dy < 0) {
			dy = (size.height - pady) - pt.y;
			if (dy > 0) {
				dy = 0;
			}
		}
		if (dx != 0 || dy != 0) {
			this.locationMoveFrom = new Point(this.location);
			this.locationMoveTo = new Point(this.location.x + dx, this.location.y + dy);
			this.locationMoveStartTime = System.currentTimeMillis();
		}
	}

	private void centerCaret()
	{
		Point pt = this.findCursorPosition();
		Dimension size = this.getContentPane().getSize();
		int dx = size.width / 2 - pt.x;
		int dy = size.height / 2 - pt.y;
		this.locationMoveFrom = new Point(this.location);
		this.locationMoveTo = new Point(this.location.x + dx, this.location.y + dy);
		this.locationMoveStartTime = System.currentTimeMillis();
	}

	private void repaintActivePanel()
	{
		if (this.activeGroup != null && this.activeGroup.activePanel != null) {
			this.activeGroup.activePanel.repaint();
		}
	}

	/**
	 * Call from codegroup when focus is gained.
	 *
	 * @return {@code false} if the request is denied
	 */
	public boolean focusGained(CodeGroup codegroup)
	{
		if (this.shouldBlockInput()) {
			return false;
		}
		if (this.activeGroup == codegroup) {
			return true;
		}
		CodeGroup lastActive = this.activeGroup;
		this.activeGroup = codegroup;
		if (lastActive.activePanel != null) {
			lastActive.activePanel.repaint(); // for cursor
		}
		return true;
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
		"View: z\n" +
		"\n" +
		"Commands:\n" +
		":spl - split current view based on the visual line selection (ctrl-v)\n" +
		":link <bot|right|top> <id> - link a child";
}
