package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class CodePanel
extends JPanel
implements MouseListener, MouseMotionListener
{
	private static final char[] BLOCK_COMMENT_START = new char[] { '/', '*' };
	private static final char[] BLOCK_COMMENT_END = new char[] { '*', '/' };
	private static final char[] COMMENT_START = new char[] { '/', '/' };

	public final CodeGroup group;
	public final JeanineFrame jf;
	public final Jeanine j;
	public final EditBuffer buffer;
	public final Integer id;

	public int firstline;
	/**
	 * Exclusive
	 */
	public int lastline;
	public boolean requireValidation;
	public boolean needRepaint;
	public CodePanel parent;
	public int link;
	/**
	 * Outgoing links, unlike the {@link #parent}/{@link #link} incoming link.
	 *
	 * {@link #parent} and {@link #link} describe to which panel this panel is linked to,
	 * whereas {@link #secondaryLinks} describes what panels are linked to this panel.
	 */
	public ArrayList<SecondaryLink> secondaryLinks;
	/**
	 * The location as a factor of font xy size.
	 */
	public Point2D.Float location;

	private int maxLineLength;
	private int rows, cols;
	private boolean isDragging;
	private Point dragStart;
	/**
	 * Inner contents as painted by {@link #paintInnerContents} to use when painting contents
	 * when this panel is drawn scaled. This image should already be scaled.
	 */
	private BufferedImage cachedPaintedInnerContents;

	public CodePanel(CodeGroup group, Integer id, int linefrom, int lineto)
	{
		this.jf = group.jf;
		this.j = this.jf.j;
		this.id = id;
		this.group = group;
		this.buffer = group.buffer;
		this.firstline = linefrom;
		this.lastline = lineto;
		this.secondaryLinks = new ArrayList<>();
		this.location = new Point2D.Float();
		this.setFocusable(false);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.recheckMaxLineLength();
		this.ensureCodeViewSize();
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		if (this.j.ensureFontMetrics((Graphics2D) g)) {
			this.setCodeViewSize(this.rows, this.cols);
			return;
		}
		Point thisloc = this.getLocationOnScreen();
		Point contentloc = this.jf.getContentPane().getLocationOnScreen();
		Dimension contentsize = this.jf.getContentPane().getSize();
		int heightleft = contentsize.height - (thisloc.y - contentloc.y);
		int hiddenHeight = contentloc.y - thisloc.y;

		int w = this.getWidth() - 1;
		int h = this.getHeight() - 1;

		g.setFont(this.j.font);
		// border
		g.setColor(Colors.border.col);
		g.drawRect(0, 0, w, h);
		g.translate(1, 1);
		w--;
		h--;
		if (this.jf.scale == 10) {
			// bg
			g.setColor(Colors.textBg.col);
			g.fillRect(0, 0, w, h);
			this.paintInnerContents(g, w, h, hiddenHeight, heightleft);
		} else {
			if (this.cachedPaintedInnerContents == null) {
				BufferedImage i, i2;
				Graphics g2;
				Dimension s = this.calculateSize(this.rows, this.cols);
				int _w = s.width - Padding.BORDER - Padding.BORDER;
				int _h = s.height - Padding.BORDER - Padding.BORDER;
				i = new BufferedImage(_w, _h, BufferedImage.TYPE_INT_RGB);
				g2 = i.createGraphics();
				g2.setFont(this.j.font);
				this.paintInnerContents(g2, _w, _h, 0, Integer.MAX_VALUE);
				g2.dispose();
				// prescale it
				this.scaleSize(s);
				_w = s.width - Padding.BORDER - Padding.BORDER;
				_h = s.height - Padding.BORDER - Padding.BORDER;
				i2 = new BufferedImage(_w, _h, BufferedImage.TYPE_INT_RGB);
				g2 = i2.createGraphics();
				g2.drawImage(i, 0, 0, _w, _h, null);
				g2.dispose();
				this.cachedPaintedInnerContents = i2;
			}
			if (w != this.cachedPaintedInnerContents.getWidth() ||
				h != this.cachedPaintedInnerContents.getHeight())
			{
				// We don't want to scale the image every paint, so fix the
				// dimension mismatch if you ever see a codepanel completely red.
				g.setColor(Color.red);
				g.fillRect(0, 0, w, h);
			} else {
				g.drawImage(this.cachedPaintedInnerContents, 0, 0, w, h, null);
			}
		}
	}

	private void paintInnerContents(Graphics g, int w, int h, int hiddenHeight, int heightleft)
	{
		g.setColor(Colors.textBg.col);
		g.fillRect(0, 0, w, h);
		g.translate(Padding.LEFT, Padding.TOP);
		w -= Padding.LEFT + Padding.RIGHT;
		h -= Padding.TOP + Padding.BOT;
		hiddenHeight -= Padding.BORDER + Padding.TOP;
		if (hiddenHeight <= this.j.fy) { // title
			g.setColor(Colors.headerBg.col);
			g.fillRect(0, 0, w, Padding.IN_HEADER + this.j.fy + Padding.IN_HEADER);
			g.setColor(Colors.headerFg.col);
			SB title = new SB(100);
			title.append(String.valueOf(this.id));
			if (this.parent != null) {
				title.append('^').append(String.valueOf(this.parent.id));
			}
			if (this.group.title != null) {
				title.append('|').append(this.group.title);
			}
			title.append('|').append(this.firstline).append('-').append(this.lastline);
			if (this.buffer.readonly) {
				title.append("|RO");
			}
			if (this.group.raw) {
				title.append("|RAW");
			}
			g.drawString(title.toString(), 0, Padding.IN_HEADER + this.j.fmaxascend);
		}

		{
			int offset =
				Padding.IN_HEADER +
				this.j.fy +
				Padding.IN_HEADER +
				Padding.BETWEEN_HEADER_AND_CONTENTS;
			hiddenHeight -= offset;
			heightleft -= offset;
			h -= offset;
			g.translate(0, offset);
		}

		// line selection
		if (this.buffer.mode == EditBuffer.SELECT_LINE_MODE) {
			g.setColor(Colors.selectionBg.col);
			int fromy = Math.max(this.buffer.lineselectfrom, this.firstline);
			int toy = Math.min(this.buffer.lineselectto, this.lastline);
			fromy -= this.firstline;
			toy -= this.firstline;
			if (fromy < toy) {
				int y = fromy * this.j.fy;
				int height = this.j.fy * toy - y;
				g.fillRect(0, y, w, height);
			}
		}

		boolean needCaret = this.group.shouldDrawCaret(this);

		// code & caret
		int ma = this.j.fmaxascend;
		boolean inBlockComment = false;
		for (int i = this.firstline; i < this.lastline && heightleft > 0; i++) {
			if (hiddenHeight < this.j.fy) {
				SB l = this.buffer.lines.get(i);
				SB line = Line.tabs2spaces(l);
				// mark ending whitespace
				if (this.buffer.mode == EditBuffer.NORMAL_MODE ||
					this.buffer.carety != i)
				{
					int wsfrom = line.length, wsto = line.length;
					while (wsfrom > 0) {
						char c = line.value[wsfrom - 1];
						if (c != '\t' && c != ' ') {
							break;
						}
						wsfrom--;
					}
					if (wsfrom != wsto) {
						int f = wsfrom * this.j.fx;
						int t = wsto * this.j.fx - f;
						g.setColor(Colors.whitespaceBg.col);
						g.fillRect(f, 0, t, this.j.fy);
					}
				}
				// search highlighting
				if (this.jf.liveSearchText != null) {
					int idx = 0;
					for (;;) {
						idx = line.indexOf(this.jf.liveSearchText, idx);
						if (idx != -1) {
							int f = Line.logicalToVisualPos(line, idx);
							int t = idx + this.jf.liveSearchText.length;
							t = Line.logicalToVisualPos(line, t) - f;
							f *= this.j.fx;
							g.setColor(Color.cyan);
							g.fillRect(f, 0, t * this.j.fx, this.j.fy);
							idx++;
						} else {
							break;
						}
					}
				}
				// caret
				if (needCaret && this.buffer.carety == i) {
					int y = 0, height = this.j.fy;
					if (this.buffer.mode == EditBuffer.INSERT_MODE) {
						g.setColor(Colors.caretInsert.col);
					} else if (this.buffer.mode == EditBuffer.NORMAL_MODE) {
						g.setColor(Colors.caretDefault.col);
					} else {
						y = height = this.j.fy / 2;
						g.setColor(Colors.caretDefault.col);
					}
					int x = Line.logicalToVisualPos(l, this.buffer.caretx);
					g.fillRect(x * this.j.fx, y, this.j.fx, height);
				}
				// the code text
				// try coloring comments
				// this should check if comment start is not in string, but oh well
				Color commentfg = Colors.commentFg.col;
				Color normalfg = Colors.textFg.col;
				// use selection fg if in selection
				if (Colors.selectionFg.col != null &&
					this.buffer.mode == EditBuffer.SELECT_LINE_MODE &&
					this.buffer.lineselectfrom <= i &&
					i < this.buffer.lineselectto)
				{
					commentfg = Colors.selectionFg.col;
					normalfg = Colors.selectionFg.col;
				}
				int from = 0;
				int to = line.length;
				int x = 0;
				int len;
				while (from < to) {
					if (inBlockComment) {
						int idx = line.indexOf(BLOCK_COMMENT_END, from);
						if (idx == -1) {
							g.setColor(commentfg);
							len = to - from;
							g.drawChars(line.value, from, len, x, ma);
							break;
						}
						idx += 2;
						g.setColor(commentfg);
						len = idx - from;
						g.drawChars(line.value, from, len, x, ma);
						inBlockComment = false;
						x += this.j.fx * len;
						from = idx;
					} else {
						int lidx = line.indexOf(COMMENT_START, from);
						int bidx = line.indexOf(BLOCK_COMMENT_START, from);
						if (lidx != -1 && bidx != -1) {
							if (lidx < bidx) {
								bidx = -1;
							} else {
								lidx = -1;
							}
						}
						if (bidx == -1 && lidx == -1) {
							// no comment
							g.setColor(normalfg);
							len = to - from;
							g.drawChars(line.value, from, len, x, ma);
							break;
						} else if (bidx != -1) {
							// block comment
							g.setColor(normalfg);
							len = bidx - from;
							g.drawChars(line.value, from, len, x, ma);
							x += this.j.fx * len;
							inBlockComment = true;
							from = bidx;
						} else {
							// line comment
							g.setColor(normalfg);
							len = lidx - from;
							g.drawChars(line.value, from, len, x, ma);
							x += this.j.fx * len;
							g.setColor(commentfg);
							len = to - lidx;
							g.drawChars(line.value, lidx, len, x, ma);
							break;
						}
					}
				}
			} else {
				hiddenHeight -= this.j.fy;
			}
			g.translate(0, this.j.fy);
			heightleft -= this.j.fy;
		}

		this.needRepaint = false;
	}

	@Override
	public Rectangle getBounds(Rectangle rv)
	{
		rv = super.getBounds(rv);
		if (SwingUtilities.getWindowAncestor(this) == null) {
			// Size from getBounds is unreliable when this panel isn't in a window,
			// so do manual calculation.
			this.recheckMaxLineLength();
			int rows = this.lastline - this.firstline;
			int cols = this.maxLineLength + /*caret*/1;
			Dimension size = this.calculateSize(rows, cols);
			this.scaleSize(size);
			rv.width = size.width;
			rv.height = size.height;
		}
		return rv;
	}

	/*MouseListener*/
	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (!this.jf.shouldBlockInput() &&
			e.getClickCount() == 2 &&
			this.jf.lineSelectionListener != null)
		{
			int line = this.getLocalLineAtY(e.getY());
			if (-1 < line && line < this.lastline - this.firstline) {
				this.invokeLineSelectionListener(line + this.firstline);
			}
		}
		if (this.jf.scale != 10) {
			this.jf.panelClickedWhileZoomed(this, e.getX(), e.getY());
		}
	}

	/*MouseListener*/
	@Override
	public void mousePressed(MouseEvent e)
	{
		if (!this.jf.shouldBlockInput()) {
			int dragAreaHeight =
				Padding.BORDER +
				Padding.TOP +
				Padding.IN_HEADER +
				this.j.fy +
				Padding.IN_HEADER;
			if (e.getY() <= dragAreaHeight) {
				this.isDragging = true;
				this.dragStart = e.getLocationOnScreen();
			} else if (this.group.focusGained(this)) {
				this.putCaretFromMouseInput(e.getX(), e.getY());
			}
		} else if (this.jf.scale != 10) {
			int x = e.getX() + this.getX();
			int y = e.getY() + this.getY();
			this.jf.panByMouseDragStart(x, y);
		}
	}

	/*MouseListener*/
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (!this.jf.shouldBlockInput()) {
			if (this.isDragging) {
				this.isDragging = false;
				this.dragStart = null;
			} else if (this.group.hasFocus(this)) {
				this.putCaretFromMouseInput(e.getX(), e.getY());
			}
		}
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

	/*MouseMotionListener*/
	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (!this.jf.shouldBlockInput()) {
			if (this.isDragging) {
				Point now = e.getLocationOnScreen();
				this.location.x += (now.x - this.dragStart.x) / (float) this.j.fx;
				this.location.y += (now.y - this.dragStart.y) / (float) this.j.fy;
				this.group.position(this);
				this.dragStart = now;
				this.jf.overlay.repaint();
			} else if (this.group.hasFocus(this)) {
				this.putCaretFromMouseInput(e.getX(), e.getY());
			}
		} else if (this.jf.scale != 10) {
			int x = e.getX() + this.getX();
			int y = e.getY() + this.getY();
			this.jf.panByMouseDragUpdate(x, y);
			this.jf.updateZoomedOverlayMouseHover(x, y, this);
		}
	}

	/*MouseMotionListener*/
	@Override
	public void mouseMoved(MouseEvent e)
	{
		int x = e.getX() + this.getX();
		int y = e.getY() + this.getY();
		this.jf.updateZoomedOverlayMouseHover(x, y, this);
	}

	public void handleInput(KeyInput event)
	{
		this.buffer.handlePhysicalInput(event);
		if (event.needGlobalRepaint) {
			this.group.repaintAll();
		} else if (event.needRepaint ||
			/*TODO: only repaint caret*/
			event.needRepaintCaret)
		{
			this.repaint();
		}
		if (event.needCheckLineLength) {
			this.recheckMaxLineLength();
		}
		if (event.needEnsureViewSize) {
			this.ensureCodeViewSize();
		}
	}

	public void putCaretFromMouseInput(int x, int y)
	{
		x -= Padding.BORDER + Padding.LEFT;
		x /= this.j.fx;
		y = this.getLineAtY(y);
		SB line = this.buffer.lines.get(y);
		int len = Line.visualLength(line);
		x = Math.min(x, len - 1);
		if (x < 0) {
			x = 0;
		}
		x = Line.visualToLogicalPos(line, x);
		if (this.buffer.carety != y || this.buffer.caretx != x) {
			this.buffer.carety = y;
			this.buffer.caretx = x;
			this.buffer.virtualCaretx = x;
			this.repaint(); // TODO: should only really repaint caret
		}
	}

	/**
	 * @param y relative to this component
	 * @return local line not adjusted with {@link #firstline} yet
	 */
	private int getLocalLineAtY(int y)
	{
		y -=
			Padding.BORDER +
			Padding.TOP +
			Padding.IN_HEADER +
			this.j.fy +
			Padding.IN_HEADER +
			Padding.BETWEEN_HEADER_AND_CONTENTS;
		y /= this.j.fy;
		return y;
	}

	private int getLineAtY(int y)
	{
		y = this.getLocalLineAtY(y);
		y += this.firstline;
		y = Math.min(y, this.lastline - 1);
		if (y < this.firstline) {
			y = this.firstline;
		}
		return y;
	}

	public void invokeLineSelectionListener(int lineNumber)
	{
		LineSelectionListener.Info info = new LineSelectionListener.Info();
		info.panel = this;
		info.group = this.group;
		info.lineNumber = lineNumber;
		info.lineContent = this.buffer.lines.get(lineNumber);
		this.jf.lineSelectionListener.lineSelected(info);
	}

	/**
	 * Invoke to force next {@link #ensureCodeViewSize} to always resize the panel.
	 */
	public void invalidateSize()
	{
		this.cols = -1;
		this.rows = -1;
	}

	public void ensureCodeViewSize()
	{
		int rows = this.lastline - this.firstline;
		this.ensureCodeViewSize(rows, this.maxLineLength + /*caret*/1);
	}

	public void recheckMaxLineLength()
	{
		this.maxLineLength = 0;
		for (int i = this.firstline; i < this.lastline; i++) {
			int visualLen = Line.tabs2spaces(this.buffer.lines.get(i)).length;
			if (visualLen > this.maxLineLength) {
				this.maxLineLength = visualLen;
			}
		}
	}

	public void ensureCodeViewSize(int rows, int cols)
	{
		if (cols < 30) {
			cols = 30;
		}
		if (this.rows != rows || this.cols != cols) {
			this.setCodeViewSize(rows, cols);
		}
	}

	public void validateSize()
	{
		this.setCodeViewSize(this.rows, this.cols);
	}

	public void setCodeViewSize(int rows, int cols)
	{
		this.rows = rows;
		this.cols = cols;
		Dimension size = this.calculateSize(rows, cols);
		this.scaleSize(size);
		if (this.getWidth() != size.width) {
			this.cachedPaintedInnerContents = null;
			this.setSize(size);
			this.jf.overlay.repaint(); // TODO: hit a lot when multiple panels resize
			this.group.positionChildrenOf(this);
		} else if (this.getHeight() != size.height) {
			this.setSize(size);
		}
	}

	private Dimension calculateSize(int rows, int cols)
	{
		Dimension size = new Dimension();
		size.width =
			Padding.BORDER +
			Padding.LEFT +
			cols * this.j.fx +
			Padding.RIGHT +
			Padding.BORDER;
		size.height =
			Padding.BORDER +
			Padding.TOP +
			Padding.IN_HEADER +
			this.j.fy +
			Padding.IN_HEADER +
			Padding.BETWEEN_HEADER_AND_CONTENTS +
			rows * this.j.fy +
			Padding.BOT +
			Padding.BORDER;
		return size;
	}

	private void scaleSize(Dimension size)
	{
		if (this.jf.scale != 10) {
			float scale = this.jf.scale / 10f;
			size.width = (int) (size.width * scale);
			size.height = (int) (size.height * scale);
		}
	}

	public boolean isEventualParentOf(CodePanel other)
	{
		while (other != null) {
			if (other.parent == this) {
				return true;
			}
			other = other.parent;
		}
		return false;
	}
}
