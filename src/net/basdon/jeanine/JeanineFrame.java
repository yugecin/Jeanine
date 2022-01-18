package net.basdon.jeanine;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.util.ArrayDeque;
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

	public ArrayDeque<JeanineState> pushedStates;
	public List<CodeGroup> codegroups, lastCodegroups;
	public CodeGroup activeGroup, lastActiveGroup;
	public Point location, lastLocation;
	public LineSelectionListener lineSelectionListener;
	public Runnable postStateLeaveListener;

	public char[] liveSearchText;
	public long searchHighlightTimeout;

	/**
	 * Div by 10 to get real scale.
	 */
	public int scale;

	private Point caretPosBeforeSearch;
	private CodeGroup activeGroupBeforeSearch;
	private Point locationMoveFrom, locationMoveTo;
	private long locationMoveStartTime;
	private Point locationZoominTo;
	private long locationZoominStartTime;
	private Point dragStart;
	private char[] activeSearch;

	public JeanineFrame(Jeanine j)
	{
		this.j = j;
		this.scale = 10;
		this.pushedStates = new ArrayDeque<>();
		this.dragStart = new Point();
		this.location = new Point();
		this.codegroups = new ArrayList<>();
		this.setIconImage(this.createLogoImg());
		this.setFocusable(true);
		this.addKeyListener(this);
		this.commandbar = new CommandBar(j);
		this.setContentPane(new BackgroundPanel());
		// mouse event coordinates on jframe are offset by window chrome, so use contentpane
		this.getContentPane().addMouseWheelListener(this);
		this.getContentPane().addMouseListener(this);
		this.getContentPane().addMouseMotionListener(this);
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
			this.addCodeGroup(this.activeGroup);
		}
		this.setPreferredSize(new Dimension(800, 800));
		this.pack();
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Preferences.interpretAndApply(this);
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
		if (this.scale != 10) {
			if (event.c == EditBuffer.ESC) {
				this.scale = 10;
				for (CodeGroup group : this.codegroups) {
					group.forceResizeAndReposition();
				}
			} else {
				this.setError("can't type while zoomed");
			}
			return;
		}
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
			if (!this.pushedStates.isEmpty()) {
				Runnable listener = this.postStateLeaveListener;
				this.popState();
				if (listener != null) {
					listener.run();
				}
				return;
			}
			break;
		case '\n':
		case '\r':
			if (buffer != null && this.lineSelectionListener != null) {
				CodePanel panel = this.activeGroup.panelAtLine(buffer.carety);
				if (panel != null) {
					panel.invokeLineSelectionListener(buffer.carety);
				}
				return;
			}
			break;
		case 'z':
			this.centerCaret(false);
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
			if (!this.pushedStates.isEmpty()) {
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
			// TODO check secondary links
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
			// TODO check secondary links
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
		this.panByMouseDragUpdate(e.getX(), e.getY());
	}

	/*MouseMotionListener*/
	@Override
	public void mouseMoved(MouseEvent e)
	{
		this.updateZoomedOverlayMouseHover(e.getX(), e.getY(), (CodePanel) null);
	}

	/*MouseListener*/
	@Override
	public void mouseClicked(MouseEvent e)
	{
	}

	/*MouseListener*/
	@Override
	public void mousePressed(MouseEvent e)
	{
		this.panByMouseDragStart(e.getX(), e.getY());
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
			int units = e.getUnitsToScroll();
			if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
				if (this.commandbar.active) {
					return;
				}
				// cancel any moving 'animation'
				this.locationMoveFrom = null;
				this.locationMoveTo = null;
				int delta;
				if (units < 0) {
					if (this.scale >= 10) {
						return;
					}
					delta = 1;
				} else {
					if (this.scale <= 1) {
						return;
					}
					delta = -1;
				}
				this.doZoom(e.getX(), e.getY(), delta);
				CodePanel panel = this.findHoveringPanel(e.getX(), e.getY());
				this.updateZoomedOverlayMouseHover(e.getX(), e.getY(), panel);
				return;
			} else if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
				units *= Preferences.hscrollPercentage;
				this.location.x -= (int) (units / 100f);
			} else {
				units *= Preferences.vscrollPercentage;
				this.location.y -= (int) (units / 100f);
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
				int delay = Preferences.smoothScrollTimeMs;
				if (time >= delay) {
					this.location = this.locationMoveTo;
					this.locationMoveFrom = null;
					this.locationMoveTo = null;
				} else {
					double t = -Math.pow(2, -10 * (time / (float) delay)) + 1;
					int dx = this.locationMoveTo.x - this.locationMoveFrom.x;
					int dy = this.locationMoveTo.y - this.locationMoveFrom.y;
					this.location.x = this.locationMoveFrom.x + (int) (dx * t);
					this.location.y = this.locationMoveFrom.y + (int) (dy * t);
				}
				for (CodeGroup group : this.codegroups) {
					group.updateLocation();
				}
			}
			if (this.locationZoominTo != null &&
				// this doesn't matter if timerinterval=25, but keeping it for now
				this.locationZoominStartTime + 15 < System.currentTimeMillis())
			{
				this.locationZoominStartTime = System.currentTimeMillis();
				this.doZoom(this.locationZoominTo.x, this.locationZoominTo.y, 1);
				if (this.scale == 10) {
					this.locationZoominTo = null;
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

	public void panByMouseDragStart(int mouseX, int mouseY)
	{
		this.dragStart.x = mouseX;
		this.dragStart.y = mouseY;
	}

	public void panByMouseDragUpdate(int mouseX, int mouseY)
	{
		int x = mouseX - this.dragStart.x;
		int y = mouseY - this.dragStart.y;
		this.location.x += x;
		this.location.y += y;
		this.dragStart.x = mouseX;
		this.dragStart.y = mouseY;

		for (CodeGroup cg : this.codegroups) {
			cg.updateLocation();
		}
	}

	/**
	 * @param x panel local click position x
	 * @param y panel local click position y
	 */
	public void panelClickedWhileZoomed(CodePanel panel, int x, int y)
	{
		// TODO: setting to disable or something
		this.locationZoominStartTime = System.currentTimeMillis();
		this.locationZoominTo = new Point(x + panel.getX(), y + panel.getY());
		this.updateZoomedOverlayMouseHover(0, 0, (CodePanel) null);
		this.activeGroup = panel.group;
		this.activeGroup.activePanel = panel;
		x /= (this.scale / 10f);
		y /= (this.scale / 10f);
		panel.putCaretFromMouseInput(x, y);
	}

	/**
	 * @param x [0,this.width]
	 * @param y [0,this.height]
	 * @param delta -1 to zoom out, +1 to zoom in
	 */
	private void doZoom(int x, int y, int delta)
	{
		float mx = x - this.location.x;
		float umx = mx / (this.scale / 10f);
		float my = y - this.location.y;
		float umy = my / (this.scale / 10f);
		this.scale += delta;
		float nmx = umx * this.scale / 10f;
		float nmy = umy * this.scale / 10f;
		this.location.x -= nmx - mx;
		this.location.y -= nmy - my;
		for (CodeGroup group : this.codegroups) {
			group.forceResizeAndReposition();
		}
	}

	/**
	 * To draw info panel on overlay for hovering {@link CodePanel} while zoomed out.
	 */
	public void updateZoomedOverlayMouseHover(int x, int y, CodePanel hoveredPanel)
	{
		if (this.scale == 10 || this.locationZoominTo != null) {
			this.overlay.showInfoForPanel(x, y, null);
		} else {
			this.overlay.showInfoForPanel(x, y, hoveredPanel);
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
		// Assign search to liveSearchText, to make highlighting happen
		this.liveSearchText = text;
		if (text.length == 0) {
			// text length may be 0 when deleting the last char in the search bar
			// still have to repaint since there could be match highlights from before
			this.activeGroup.repaintAll();
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
		// Keep highlighting for live search until not searching anymore,
		// and let the highlighting only stay for a short while for non-live searches.
		if (live) {
			this.searchHighlightTimeout = Long.MAX_VALUE;
		} else {
			this.searchHighlightTimeout = System.currentTimeMillis();
			this.searchHighlightTimeout += Preferences.searchHighlightTime;
		}
		this.activeGroup.repaintAll();
		this.ensureCaretInView();
	}

	public boolean shouldBlockInput()
	{
		return this.commandbar.active || this.scale != 10;
	}

	public boolean shouldDrawCaret()
	{
		return !this.commandbar.active || this.commandbar.isSearch();
	}

	public void acceptCommand(String command)
	{
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
			new DialogFontSelection(this);
		} else if ("prefs".equals(parts[0])) {
			new DialogPreferences(this);
		} else if ("link".equals(parts[0])) {
			CodePanel[] child = new CodePanel[1];
			String[] position = new String[1];
			if (this.doLinkCommandPrechecks(parts, child, position)) {
				this.activeGroup.reChild(child[0], position[0]);
			}
		} else if ("slink".equals(parts[0])) {
			CodePanel[] child = new CodePanel[1];
			String[] position = new String[1];
			if (this.doLinkCommandPrechecks(parts, child, position)) {
				this.activeGroup.slink(child[0], position[0]);
			}
		} else if ("unlink".equals(parts[0])) {
			CodePanel[] child = new CodePanel[1];
			String[] position = new String[1];
			if (this.doLinkCommandPrechecks(parts, child, position)) {
				this.activeGroup.unlink(child[0], position[0]);
			}
		} else if ("raw".equals(parts[0])) {
			if (this.activeGroup == null) {
				this.setError("no active panel");
			} else {
				// TODO don't allow while in line selection mode (it gets offset)
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
		} else if ("bp".equals(parts[0])) {
			int size = this.codegroups.size();
			for (int i = 0; i < size; i++) {
				if (this.codegroups.get(i) == this.activeGroup) {
					if (i > 0) {
						this.activeGroup = this.codegroups.get(i - 1);
					} else {
						this.activeGroup = this.codegroups.get(size - 1);
					}
					this.ensureCaretInView();
					break;
				}
			}
		} else if ("bn".equals(parts[0])) {
			int size = this.codegroups.size();
			for (int i = 0; i < size; i++) {
				if (this.codegroups.get(i) == this.activeGroup) {
					if (i + 1 < size) {
						this.activeGroup = this.codegroups.get(i + 1);
					} else {
						this.activeGroup = this.codegroups.get(0);
					}
					this.ensureCaretInView();
					break;
				}
			}
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

	/**
	 * Validates command parameters, activeGroup/activePanel and valid childId
	 */
	private boolean doLinkCommandPrechecks(
		String[] cmdParts,
		CodePanel[] outChild,
		String[] outPosition)
	{
		Integer childId;
		if (cmdParts.length != 3 ||
			(!"bot".equals(outPosition[0] = cmdParts[1]) &&
			!"right".equals(outPosition[0]) &&
			!"top".equals(outPosition[0])) ||
			(childId = Util.parseInt(cmdParts[2])) == null)
		{
			this.setError("syntax: :" + cmdParts[0] + " <bot|right|top> <id>");
			return false;
		} else if (this.activeGroup == null || this.activeGroup.activePanel == null) {
			this.setError("can't " + cmdParts[0] + ", no active group or panel");
			return false;
		} else if ((outChild[0] = this.activeGroup.panels.get(childId)) == null) {
			this.setError("can't " + cmdParts[0] + ", unknown child");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Creates a new codegroup linked to the file, or if an existing codegroup is already
	 * linked to the file, then that codegroup will become the {@link #activeGroup}.
	 */
	private void openFile(File file)
	{
		for (CodeGroup group : this.codegroups) {
			if (file.equals(group.ownerFile)) {
				this.activeGroup = group;
				return;
			}
		}
		this.activeGroup = new CodeGroup(this);
		Rectangle rect = this.getGroupsBounds();
		this.activeGroup.setLocation(rect.x + rect.width + 25, rect.y);
		try {
			this.activeGroup.readFile(file);
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
		this.addCodeGroup(this.activeGroup);
	}

	private void addCodeGroup(CodeGroup group)
	{
		this.codegroups.add(group);
		for (CodePanel panel : group.panels.values()) {
			this.getContentPane().add(panel);
		}
		group.revalidateSizesAndReposition();
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

	public void pushState(JeanineState state)
	{
		JeanineState oldState = new JeanineState();
		oldState.activeGroup = this.activeGroup;
		oldState.codegroups = this.codegroups;
		oldState.location = new Point(this.location);
		oldState.lineSelectionListener = this.lineSelectionListener;
		oldState.postStateLeaveListener = this.postStateLeaveListener;
		oldState.font = this.j.font;
		this.pushedStates.push(oldState);
		this.activeGroup = state.activeGroup;
		this.codegroups = state.codegroups;
		this.lineSelectionListener = state.lineSelectionListener;
		this.postStateLeaveListener = state.postStateLeaveListener;
		this.getContentPane().removeAll();
		for (CodeGroup group : this.codegroups) {
			for (CodePanel panel : group.panels.values()) {
				this.getContentPane().add(panel);
			}
			group.revalidateSizesAndReposition();
		}
		this.centerCaret(true);
		this.repaint();
	}

	public void popState()
	{
		JeanineState state = this.pushedStates.pop();
		this.codegroups = state.codegroups;
		this.activeGroup = state.activeGroup;
		this.location = state.location;
		this.lineSelectionListener = state.lineSelectionListener;
		this.postStateLeaveListener = state.postStateLeaveListener;
		this.getContentPane().removeAll();
		if (!this.j.font.equals(state.font)) {
			for (CodeGroup grp : this.codegroups) {
				grp.fontChanged();
			}
			this.moveToGetCursorAtPosition(state.cursorPos);
		}
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
					/*padding title up/down*/ 2 +
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

	public void updateFontKeepCursorFrozen()
	{
		Point oldcursorpos = this.findCursorPosition();
		this.j.updateFont();
		this.j.ensureFontMetrics((Graphics2D) this.getGraphics());
		for (CodeGroup group : this.codegroups) {
			group.fontChanged();
		}
		this.moveToGetCursorAtPosition(oldcursorpos);
	}

	public void ensureCaretInView()
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
			this.panView(dx, dy);
		}
	}

	private void centerCaret(boolean forceSnappyPan)
	{
		Point pt = this.findCursorPosition();
		Dimension size = this.getContentPane().getSize();
		int dx = size.width / 2 - pt.x;
		int dy = size.height / 2 - pt.y;
		this.panView(dx, dy, forceSnappyPan);
	}

	private void panView(int dx, int dy)
	{
		this.panView(dx, dy, false);
	}

	private void panView(int dx, int dy, boolean forceSnappyPan)
	{
		if (Preferences.smoothScrollTimeMs == 0 || forceSnappyPan) {
			this.location.x += dx;
			this.location.y += dy;
			for (CodeGroup group : this.codegroups) {
				group.updateLocation();
			}
		} else {
			this.locationMoveFrom = new Point(this.location);
			this.locationMoveTo = new Point(this.location.x + dx, this.location.y + dy);
			this.locationMoveStartTime = System.currentTimeMillis();
		}
	}

	private CodePanel findHoveringPanel(int x, int y)
	{
		for (CodeGroup group : this.codegroups) {
			for (CodePanel panel : group.panels.values()) {
				int px = panel.getX(), py = panel.getY();
				if (px <= x && x <= px + panel.getWidth() &&
					py <= y && y <= py + panel.getHeight())
				{
					return panel;
				}
			}
		}
		return null;
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

	public Rectangle getGroupsBounds()
	{
		if (this.codegroups.isEmpty()) {
			return new Rectangle(0, 0, 0, 0);
		}
		Rectangle rect = new Rectangle(), rv = new Rectangle();
		rv.x = Integer.MAX_VALUE;
		rv.y = Integer.MAX_VALUE;
		rv.width = Integer.MIN_VALUE;
		rv.height = Integer.MIN_VALUE;
		for (CodeGroup group : this.codegroups) {
			group.getBounds(rect);
			rv.x = Math.min(rv.x, rect.x);
			rv.y = Math.min(rv.y, rect.y);
			rv.width = Math.max(rv.width, rect.x + rect.width);
			rv.height = Math.max(rv.height, rect.y + rect.height);
		}
		rv.width -= rv.x;
		rv.height -= rv.y;
		// Group bounds include the location of JeanineFrame, so subtract it here
		rv.x -= this.location.x;
		rv.y -= this.location.y;
		return rv;
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
		"Copy: yy (y in selection)\n" +
		"View: z\n" +
		"Search: / n N *\n" +
		"\n" +
		"mousedrag or (shift+)scroll to pan view\n" +
		"ctrl+scroll to zoom\n" +
		"\n" +
		"Commands:\n" +
		":e - open a file for editing\n" +
		":bd - close the active editing group\n" +
		":bp/:bn - go to previous/next editing group\n" +
		":spl - split current view based on the visual line selection (ctrl-v)\n" +
		":link <bot|right|top> <id> - link a child\n" +
		":slink <bot|right|top> <id> - like :link but as a secondary link\n" +
		":unlink <bot|right|top> <id> - like :slink but removes a secondary link\n" +
		":raw - toggle between raw and 2d mode\n" +
		":<number> - jump to a line number\n" +
		":font - change the font\n" +
		":prefs - change preferences\n" +
		"\n" +
		"Some dialogs have lines that can be interacted with by\n" +
		"pressing ENTER or double clicking\n" +
		"";
}
