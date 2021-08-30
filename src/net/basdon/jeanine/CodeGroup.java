package net.basdon.jeanine;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.SwingUtilities;

public class CodeGroup
{
	public final JeanineFrame jf;
	public final EditBuffer buffer;
	public final HashMap<Integer, CodeFrame> frames;
	public final Point location;

	public CodeFrame rootFrame;

	public File ownerFile;
	public CodeFrame activeFrame;

	public CodeGroup(JeanineFrame jf)
	{
		this.frames = new HashMap<>();
		this.buffer = new EditBuffer(jf.j, this);
		this.jf = jf;
		this.location = new Point();
	}

	public void readFile(File file)
	{
		this.ownerFile = file;
	}

	public void setContents(String text)
	{
		SB sb = new SB();
		this.buffer.lines.clear();
		this.buffer.lines.add(sb);
		if (text != null) {
			for (char c : text.toCharArray()) {
				if (c == '\n') {
					sb = new SB();
					this.buffer.lines.add(sb);
				} else {
					sb.append(c);
				}
			}
		}

		this.frames.clear();
		Integer id = Integer.valueOf(0);
		this.rootFrame = new CodeFrame(this.jf, this, id, this.buffer, 0, this.buffer.lines.size());
		this.frames.put(id, this.rootFrame);
		this.position(this.rootFrame);
		this.jf.add(this.rootFrame);
		this.rootFrame.setVisible(true);
	}

	public void setLocation(int x, int y)
	{
		this.location.x = x;
		this.location.y = y;
		if (this.rootFrame != null) {
			this.position(this.rootFrame);
		}
	}

	public void position(CodeFrame frame)
	{
		int x = this.location.x;
		int y = this.location.y;
		if (frame.parent != null) {
			CodeFrame parent = this.frames.get(frame.parent.id);
			if (parent != null) {
				Rectangle bounds = parent.getBounds();
				switch (frame.anchor) {
				case 't':
					break;
				case 'r':
					break;
				case 'b':
					x = bounds.x;
					y = bounds.y + bounds.height;
					break;
				case 'l':
					break;
				}
			}
		}
		x += frame.location.x;
		y += frame.location.y;
		frame.suppressNextMovedEvent = true;
		frame.setLocation(x, y);
		frame.requirePositionSizeValidation = false;
		this.framePositionChanged(frame);
	}

	public void framePositionChanged(CodeFrame frame)
	{
		for (CodeFrame child : this.frames.values()) {
			if (child.parent == frame) {
				this.position(child);
			}
		}
	}

	public void split()
	{
		if (this.activeFrame == null) {
			this.jf.setError("can't split, no code panel focused");
			return;
		}

		if (this.buffer.mode != EditBuffer.SELECT_LINE_MODE) {
			this.jf.setError("can't split, need to be in 'select line' mode (ctrl+v)");
			return;
		}
		if (this.frameAtLine(this.buffer.lineselectfrom) !=
			this.frameAtLine(this.buffer.lineselectto - 1))
		{
			this.jf.setError("can't split, selection spans multiple views");
			return;
		}
		if (this.buffer.lineselectfrom == 0 &&
			this.buffer.lineselectto == this.buffer.lines.size())
		{
			this.jf.setError("can't split, no lines left");
			return;
		}

		CodePanel codepanel = this.activeFrame.codepanel;
		this.buffer.mode = EditBuffer.NORMAL_MODE;

		Integer nextId = Integer.valueOf(this.findMaxId() + 1);
		if (this.buffer.lineselectfrom == codepanel.firstline) {
			codepanel.lastline = this.buffer.lineselectto;
			codepanel.recheckMaxLineLength();
			codepanel.ensureCodeViewSize();
			this.activeFrame = this.add(nextId, this.activeFrame, 'b', 0, 10,
				this.buffer.lineselectto,
				this.buffer.lines.size());
		} else {
			int initialLastline = codepanel.lastline;
			codepanel.lastline = this.buffer.lineselectfrom;
			codepanel.recheckMaxLineLength();
			codepanel.ensureCodeViewSize();
			if (this.buffer.lineselectto == initialLastline) {
				this.activeFrame = this.add(nextId, this.activeFrame, 'b', 0, 10,
					this.buffer.lineselectfrom,
					initialLastline);
			} else {
				this.activeFrame = this.add(nextId, this.activeFrame, 'b', 0, 10,
					this.buffer.lineselectfrom,
					this.buffer.lineselectto);
				nextId = Integer.valueOf(nextId.intValue() + 1);
				this.add(nextId, this.activeFrame, 'b', 0, 10,
					this.buffer.lineselectto,
					initialLastline);
			}
		}

		this.activeFrame = this.frameAtLine(this.buffer.carety);
		if (this.activeFrame != null) {
			SwingUtilities.invokeLater(() -> {
				this.activeFrame.codepanel.requestFocusInWindow();
				this.activeFrame.codepanel.repaint();
			});
		}
	}

	private CodeFrame add(
		Integer id,
		CodeFrame parent,
		char anchor,
		int posX,
		int posY,
		int linefrom,
		int lineto)
	{
		CodeFrame cf = new CodeFrame(this.jf, this, id, this.buffer, linefrom, lineto);
		this.frames.put(id, cf);
		cf.parent = parent;
		cf.anchor = (byte) anchor;
		cf.location.x = posX;
		cf.location.y = posY;
		cf.codepanel.recheckMaxLineLength();
		cf.codepanel.ensureCodeViewSize();
		this.position(cf);
		cf.setVisible(true);
		this.jf.add(cf);
		return cf;
	}

	public void requestFocusInWindow()
	{
		if (this.activeFrame != null) {
			this.activeFrame.codepanel.requestFocusInWindow();
		} else {
			Iterator<CodeFrame> iter = this.frames.values().iterator();
			if (iter.hasNext()) {
				(this.activeFrame = iter.next()).codepanel.requestFocusInWindow();
			}
		}
		this.jf.activeGroup = this;
	}

	/**
	 * Call from codepanel when focus is gained.
	 *
	 * @return {@code false} if the request is denied
	 */
	public boolean focusGained(CodeFrame frame)
	{
		if (this.activeFrame != null &&
			this.activeFrame != frame &&
			this.buffer.mode != EditBuffer.NORMAL_MODE)
		{
			return false;
		}
		this.activeFrame = frame;
		return true;
	}

	public void mouseDragged(int dX, int dY)
	{
		Point loc = null;
		for (CodeFrame frame : this.frames.values()) {
			loc = frame.getLocation(loc);
			frame.setLocation(loc.x + dX, loc.y + dY);
		}
	}

	public int findMaxId()
	{
		int max = 0;
		for (Integer i : this.frames.keySet()) {
			if (i.intValue() > max) {
				max = i.intValue();
			}
		}
		return max;
	}

	public CodeFrame frameAtLine(int line)
	{
		for (CodeFrame frame : this.frames.values()) {
			if (frame.codepanel.firstline <= line &&
				line < frame.codepanel.lastline)
			{
				return frame;
			}
		}
		return null;
	}

	public void repaintAll()
	{
		for (CodeFrame frame : this.frames.values()) {
			frame.repaint();
		}
	}

	/**
	 * To dispatch repaints after physical input is handled, because repaints might be needed
	 * if {@link #lineAdded} or {@link #lineRemoved} were invoked.
	 */
	public void dispatchInputEvent(KeyInput event, CodePanel source)
	{
		source.handleInputInternal(event);
		// TODO: deal with frames that are empty now
		CodeFrame newActiveFrame = null;
		for (CodeFrame frame : this.frames.values()) {
			if (frame.codepanel.firstline <= this.buffer.carety &&
				this.buffer.carety < frame.codepanel.lastline)
			{
				newActiveFrame = frame;
				if (event.needCheckLineLength || event.needEnsureViewSize) {
					frame.codepanel.recheckMaxLineLength();
					frame.codepanel.ensureCodeViewSize();
				}
			}
			if (frame.requirePositionSizeValidation) {
				this.position(frame);
			}
		}
		if (newActiveFrame != null && newActiveFrame != this.activeFrame) {
			this.activeFrame.repaint();
			this.activeFrame = newActiveFrame;
			this.activeFrame.codepanel.requestFocusInWindow();
			this.activeFrame.repaint();
		}
	}

	public void beforeLineAdded(int idx)
	{
		for (CodeFrame frame : this.frames.values()) {
			if (frame.codepanel.firstline >= idx && idx != 0) {
				frame.codepanel.firstline++;
				frame.requirePositionSizeValidation = true;
			}
			if (frame.codepanel.lastline >= idx) {
				frame.codepanel.lastline++;
				frame.requirePositionSizeValidation = true;
			}
		}
	}

	public void beforeLineRemoved(int idx)
	{
		for (CodeFrame frame : this.frames.values()) {
			if (frame.codepanel.lastline >= idx) {
				frame.codepanel.lastline--;
				frame.requirePositionSizeValidation = true;
			}
			if (frame.codepanel.firstline > idx) {
				frame.codepanel.firstline--;
				frame.requirePositionSizeValidation = true;
			}
		}
	}

	public void doUndo(Undo undo)
	{
		for (CodeFrame frame : this.frames.values()) {
			if (frame.buffer == undo.buffer) {
				this.activeFrame = frame;
				frame.codepanel.requestFocusInWindow();
				frame.codepanel.handleInput(new KeyInput('u'));
				return;
			}
		}
		// TODO what now
		this.jf.j.undolistptr--;
	}

	public void dispose()
	{
	}
}
