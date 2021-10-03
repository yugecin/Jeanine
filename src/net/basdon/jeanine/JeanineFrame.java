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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import static java.awt.KeyboardFocusManager.*;

public class JeanineFrame
extends JFrame
implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, ActionListener
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
	public char[] liveSearchText;
	public long searchHighlightTimeout;

	private Point caretPosBeforeSearch;
	private CodeGroup activeGroupBeforeSearch;
	private Point locationMoveFrom, locationMoveTo;
	private long locationMoveStartTime;
	private Point dragStart;
	private char[] activeSearch;

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
		this.addMouseWheelListener(this);
		this.commandbar = new CommandBar(j);
		this.setContentPane(new BackgroundPanel());
		this.setGlassPane(this.overlay = new OverlayPanel(this));
		this.overlay.setVisible(true);
		this.setLocationByPlatform(true);
		this.setError(null);
		this.getLayeredPane().add(this.commandbar, JLayeredPane.POPUP_LAYER);
		this.welcomeCodeGroup = new CodeGroup(this);
		if (Jeanine.argsNumFilesToOpen > 0) {
			for (int i = 0; i < Jeanine.argsNumFilesToOpen; i++) {
				this.openFile(Jeanine.argsFilesToOpen[i]);
			}
		} else {
			this.activeGroup = this.welcomeCodeGroup;
			this.activeGroup.setLocation(30, 30);
			this.activeGroup.setContents(new Util.LineIterator(WELCOMETEXT), true);
		}
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
			if (this.commandbar.active) {
				if (this.commandbar.isSearch()) {
					String searchTxt = this.commandbar.val.toString();
					if (searchTxt == null) {
						this.liveSearchText = null;
					} else {
						this.liveSearchText = searchTxt.toCharArray();
					}
					this.doSearch(this.liveSearchText, true, true);
				}
				return;
			}
			if (this.commandbar.isCommand()) {
				if (this.commandbar.val.length() != 0) {
					this.acceptCommand(this.commandbar.val.toString());
				}
			} else if (this.commandbar.isSearch()) {
				this.liveSearchText = null;
				if (this.commandbar.val.length() != 0) {
					StringBuilder sb = this.commandbar.val;
					this.activeSearch = new char[sb.length()];
					sb.getChars(0, sb.length(), this.activeSearch, 0);
				} else {
					this.activeGroup = this.activeGroupBeforeSearch;
					if (this.activeGroup != null) {
						EditBuffer buf = this.activeGroup.buffer;
						buf.caretx = this.caretPosBeforeSearch.x;
						buf.virtualCaretx = this.caretPosBeforeSearch.x;
						buf.carety = this.caretPosBeforeSearch.y;
						this.ensureCaretInView();
					}
				}
				this.activeGroupBeforeSearch = null;
				this.caretPosBeforeSearch = null;
			}
			this.repaintActivePanel();
			return;
		}
		if (this.activeGroup != null && this.activeGroup.activePanel != null) {
			this.activeGroup.dispatchInputEvent(event, this.activeGroup.activePanel);
			this.ensureCaretInView();
			if (!event.error) {
				return;
			}
		}
		EditBuffer buffer = this.activeGroup == null ? null : this.activeGroup.buffer;
		switch (event.c) {
		case EditBuffer.ESC:
			if (this.isSelectingFont) {
				this.stopSelectingFont();
				Point oldcursorpos = cursorPosBeforeChangingFont;
				for (CodeGroup group : this.codegroups) {
					group.fontChanged();
				}
				this.moveToGetCursorAtPosition(oldcursorpos);
				return;
			}
			break;
		case '\n':
		case '\r':
			if (this.isSelectingFont && buffer != null) {
				String directive = buffer.lines.get(buffer.carety).toString();
				if (directive.length() > 3 && directive.charAt(0) == 'f') {
					String fontName = directive.substring(2);
					Font font = new Font(fontName, Font.BOLD, 14);
					this.j.setFont(font);
				} else if (directive.length() > 2 && directive.charAt(0) == 's') {
					int size = Integer.parseInt(directive.substring(2));
					this.j.setFont(this.j.font.deriveFont((float) size));
				} else if (directive.length() > 0 && directive.charAt(0) == 'b') {
					this.j.setFont(this.j.font.deriveFont(Font.BOLD));
				} else if (directive.length() > 0 && directive.charAt(0) == 'p') {
					this.j.setFont(this.j.font.deriveFont(Font.PLAIN));
				} else {
					return;
				}
				Point oldcursorpos = this.findCursorPosition();
				for (CodeGroup group : this.codegroups) {
					group.fontChanged();
				}
				this.moveToGetCursorAtPosition(oldcursorpos);
				return;
			}
			break;
		case 'z':
			this.centerCaret();
			return;
		case 'n':
			this.doSearch(this.activeSearch, false, true);
			return;
		case 'N':
			this.doSearch(this.activeSearch, false, false);
			return;
		case '*':
			this.doSearchWordUnderCaret();
			return;
		case ':':
			if (this.isSelectingFont) {
				break;
			}
			this.commandbar.showForCommand();
			this.repaintActivePanel(); // to update caret because it lost focus
			return;
		case '/':
			this.activeGroupBeforeSearch = this.activeGroup;
			if (this.activeGroup != null) {
				this.caretPosBeforeSearch = new Point();
				this.caretPosBeforeSearch.x = this.activeGroup.buffer.caretx;
				this.caretPosBeforeSearch.y = this.activeGroup.buffer.carety;
			}
			this.commandbar.showForSearch();
			this.repaintActivePanel(); // to update caret because it lost focus
			return;
		case 'l':
			if (buffer == null) {
				return;
			}
			if (buffer.mode == EditBuffer.NORMAL_MODE) {
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
			}
			break;
		case 'h':
			if (buffer == null) {
				return;
			}
			if (buffer.mode == EditBuffer.NORMAL_MODE) {
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
			}
			break;
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

	/*MouseWheelListener*/
	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
			int units = e.getUnitsToScroll() * 2;
			if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
				this.location.x -= units;
			} else {
				this.location.y -= units * 2;
			}
			for (CodeGroup cg : this.codegroups) {
				cg.updateLocation();
			}
		}
	}

	/*ActionListener*/
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (this.timer == e.getSource()) {
			if (this.locationMoveFrom != null) {
				long time = System.currentTimeMillis() - this.locationMoveStartTime;
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
			if (this.liveSearchText != null &&
				this.searchHighlightTimeout < System.currentTimeMillis())
			{
				this.liveSearchText = null;
				this.searchHighlightTimeout = Long.MAX_VALUE;
				if (this.activeGroup != null) {
					// TODO: this might need to repaint all groups,
					// if active group changed during this time (otherwise
					// search highlighting won't be cleared for earlier active
					// group)
					this.activeGroup.repaintAll();
				}
			}
		}
	}

	// The search on this doesn't check if the word is isolated, unlike in vim.
	private void doSearchWordUnderCaret()
	{
		if (this.activeGroup == null) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		EditBuffer buf = this.activeGroup.buffer;
		SB line = buf.lines.get(buf.carety);
		if (buf.caretx >= line.length) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		Point p = VimOps.getWordUnderCaret(line, buf.caretx);
		this.activeSearch = new char[p.y - p.x];
		System.arraycopy(line.value, p.x, this.activeSearch, 0, p.y - p.x);
		this.doSearch(this.activeSearch, false, true);
	}

	private void doSearch(char[] text, boolean live, boolean forwards)
	{
		if (text == null) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		if (this.activeGroup == null) {
			return;
		}
		EditBuffer buf = this.activeGroup.buffer;
		Point p;
		int fromx = buf.caretx;
		if (forwards) {
			if (!live) {
				// If not live, we're searching for the next occurence, so start
				// searching from the position after where the caret is currently.
				fromx++;
			}
			p = buf.find(text, buf.carety, fromx);
			// search from start again
			if (p == null) {
				p = buf.find(text, 0, 0);
			}
		} else {
			if (!live) {
				// If not live, we're searching for the prev occurence, so start
				// searching from the position before where the caret is currently.
				fromx--;
			}
			p = buf.findBackwards(text, buf.carety, fromx);
			// search from end again
			if (p == null) {
				p = buf.findBackwards(text, Integer.MAX_VALUE, Integer.MAX_VALUE);
			}
		}
		// If not live searching, also disregard finds that are already at cursor pos again
		if (p == null || (!live && p.y == buf.carety && p.x == buf.caretx)) {
			Toolkit.getDefaultToolkit().beep();
		} else {
			buf.caretx = p.x;
			buf.virtualCaretx = p.x;
			buf.carety = p.y;
			this.activeGroup.activePanel = this.activeGroup.panelAtLine(p.y);
		}
		// Assign search to liveSearchText, to make highlighting happen
		this.liveSearchText = text;
		// Keep highlighting for live search until not searching anymore,
		// and let the highlighting only stay for a short while for non-live searches.
		if (live) {
			this.searchHighlightTimeout = Long.MAX_VALUE;
		} else {
			this.searchHighlightTimeout = System.currentTimeMillis() + 175;
		}
		this.activeGroup.repaintAll();
		this.ensureCaretInView();
	}

	public boolean shouldBlockInput()
	{
		return this.commandbar.active;
	}

	public boolean shouldDrawCaret()
	{
		return !this.commandbar.active || this.commandbar.isSearch();
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
			if (this.activeGroup == null) {
				this.setError("no active panel");
			} else {
				this.activeGroup.split();
			}
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
				this.openFile(file);
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
		} else if (parts[0].length() > 0 &&
			'0' <= parts[0].charAt(0) &&
			parts[0].charAt(0) < '9')
		{
			try {
				if (this.activeGroup == null) {
					this.setError("can't jump to line - no active group");
				}
				int linenr = Integer.parseInt(parts[0]) - 1;
				if (linenr < 0) {
					linenr = 0;
				} else if (linenr >= this.activeGroup.buffer.lines.size()) {
					linenr = this.activeGroup.buffer.lines.size() - 1;
				}
				this.activeGroup.buffer.carety = linenr;
				this.activeGroup.buffer.caretx = 0;
				this.activeGroup.buffer.virtualCaretx = 0;
				this.ensureCaretInView();
				this.activeGroup.activePanel = this.activeGroup.panelAtLine(linenr);
				this.activeGroup.repaintAll();
			} catch (Throwable t) {
				this.setError("expected linenumber but got unparsable int");
			}
		} else {
			this.setError("unknown command: " + parts[0]);
		}
	}

	private void openFile(File file)
	{
		for (CodeGroup group : this.codegroups) {
			if (file.equals(group.ownerFile)) {
				this.activeGroup = group;
				return;
			}
		}
		this.activeGroup = new CodeGroup(this);
		try {
			this.activeGroup.readFile(file);
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
		this.codegroups.add(this.activeGroup);
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
		ArrayList<String> lines = new ArrayList<>();
		lines.add("c Welcome to font selection.");
		lines.add("c Put the caret on a setting and press enter.");
		lines.add("c Exit by pressing ESC.");
		lines.add("/*jeanine:p:i:2;p:0;x:0;y:20;a:b*/");
		lines.add("c Font size:");
		lines.add("s 6");
		lines.add("s 7");
		lines.add("s 8");
		lines.add("s 9");
		lines.add("s 10");
		lines.add("s 11");
		lines.add("s 12");
		lines.add("s 13");
		lines.add("s 14");
		lines.add("s 15");
		lines.add("s 16");
		lines.add("s 17");
		lines.add("s 18");
		lines.add("s 19");
		lines.add("s 20");
		lines.add("/*jeanine:p:i:3;p:2;x:0;y:20;a:b*/");
		lines.add("c Font style:");
		lines.add("b bold");
		lines.add("p plain");
		lines.add("/*jeanine:p:i:1;p:0;x:20;y:0;a:t*/");
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fonts = ge.getAvailableFontFamilyNames();
		for (String font : fonts) {
			lines.add("f " + font);
		}
		CodeGroup group = new CodeGroup(this);
		group.title = "Font selection";
		group.buffer.readonly = true;
		group.setContents(new Util.String2SBIter(lines.iterator()), true);
		group.buffer.carety = 22;
		group.activePanel = group.panelAtLine(group.buffer.carety);
		group.setLocation(0, 30);
		this.activeGroup = group;
		this.ensureCaretInView();
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
		int padxright = (int) (size.width / 3f);
		int padxleft = (int) (size.width / 18f);
		int pady = size.height / 8;
		int dx = padxleft - pt.x;
		if (dx < 0) {
			dx = (size.width - padxright) - pt.x;
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
		"Move: h j k l ^ $ w b e gg G ^D ^U { }\n" +
		"Insert: i I a A o O p P\n" +
		"Delete: x dw db dd dj dk diw di' di\" di[ di( di{ da' da\" da[ da( da{ d$\n" +
		"Change: cw cb ciw ci' ci\" ci[ ci( ci{ ca' ca\" ca[ ca( ca{ c$ r s\n" +
		"Indent: << >> (< > in selection)\n" +
		"Other: . u J\n" +
		"Select: ctrl-v\n" +
		"View: z\n" +
		"Search: / n N *\n" +
		"\n" +
		"Commands:\n" +
		":e - open a file for editing\n" +
		":bd - close the active editing group\n" +
		":spl - split current view based on the visual line selection (ctrl-v)\n" +
		":link <bot|right|top> <id> - link a child" +
		":raw - toggle between raw and 2d mode\n" +
		":<number> - jump to a line number\n" +
		":font - change the font\n" +
		"";
}
