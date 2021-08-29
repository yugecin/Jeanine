package net.basdon.jeanine;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.JInternalFrame;

public class CodeFrame extends JInternalFrame implements VetoableChangeListener, ComponentListener
{
	private final Jeanine j;

	public final CodeGroup group;

	private int rows, cols;

	public final Integer id;
	public final EditBuffer buffer;
	public final CodePanel codepanel;

	public Dimension chromesize;
	public CodeFrame parent;
	/**
	 * Relative to non-null parent's anchor, or relative to belonging group's location.
	 */
	public Point location;
	/**one of {@code t,r,b,l}*/
	public byte anchor;
	public boolean suppressNextMovedEvent;
	public boolean requirePositionSizeValidation;

	private Point lastLocation;

	public CodeFrame(JeanineFrame jf, CodeGroup group, Integer id, EditBuffer buffer, int linefrom, int lineto)
	{
		super(String.valueOf(id), false, false, false, false);
		this.j = jf.j;
		this.id = id;
		this.buffer = buffer;
		this.group = group;
		this.chromesize = new Dimension();
		this.location = new Point();
		this.setFrameIcon(null);
		this.setContentPane(this.codepanel = new CodePanel(this, jf));
		this.setFocusable(false);
		this.addVetoableChangeListener(this);
		this.addComponentListener(this);
		this.lastLocation = this.getLocation();
		this.codepanel.firstline = linefrom;
		this.codepanel.lastline = lineto;
	}

	/*VetoableChangeListener*/
	@Override
	public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException
	{
		if (JInternalFrame.IS_SELECTED_PROPERTY.equals(evt.getPropertyName())) {
			if (!this.group.focusGained(this)) {
				throw new PropertyVetoException("", evt);
			}
		}
	}

	/*ComponentListener*/
	@Override
	public void componentResized(ComponentEvent e)
	{
	}

	/*ComponentListener*/
	@Override
	public void componentMoved(ComponentEvent e)
	{
		Point now = this.getLocation();
		if (!this.suppressNextMovedEvent) {
			this.location.x += now.x - this.lastLocation.x;
			this.location.y += now.y - this.lastLocation.y;
			this.group.framePositionChanged(this);
		}
		this.lastLocation.x = now.x;
		this.lastLocation.y = now.y;
		this.suppressNextMovedEvent = false;
	}

	/*ComponentListener*/
	@Override
	public void componentShown(ComponentEvent e)
	{
	}

	/*ComponentListener*/
	@Override
	public void componentHidden(ComponentEvent e)
	{
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

	public void setCodeViewSize(int rows, int cols)
	{
		this.rows = rows;
		this.cols = cols;
		Dimension size = new Dimension();
		size.width = /*padding*/ 2 + cols * this.j.fx;
		size.height = /*padding*/ 2 + rows * this.j.fy;
		this.rootPane.setMinimumSize(size);
		this.rootPane.setMaximumSize(size);
		this.rootPane.setPreferredSize(size);
		this.pack();
		this.chromesize.width = this.getWidth() - this.rootPane.getWidth();
		this.chromesize.height = this.getHeight() - this.rootPane.getHeight();
	}
}
