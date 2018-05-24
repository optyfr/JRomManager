package jrm.ui.controls;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Predicate;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class JCheckBoxList<E> extends JList<E>
{
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public JCheckBoxList()
	{
		setCellRenderer(new CellRenderer());

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				final int index = locationToIndex(e.getPoint());

				if(index != -1 && e.getButton()==MouseEvent.BUTTON1)
				{
					checkboxes[index].setSelected(!checkboxes[index].isSelected());
					for(final ListSelectionListener l : getListSelectionListeners())
						l.valueChanged(new ListSelectionEvent(this, index, index, false));
					repaint();
				}
				super.mousePressed(e);
			}
		});

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	@Override
	public void setModel(final ListModel<E> model)
	{
		super.setModel(model);
		setEnabled(model.getSize()>0);
	}

	@Override
	public boolean isSelectedIndex(final int index)
	{
		return checkboxes[index].isSelected();
	}

	public void selectAll()
	{
		for(final JCheckBox checkbox : checkboxes)
			checkbox.setSelected(true);
		for(final ListSelectionListener l : getListSelectionListeners())
			l.valueChanged(new ListSelectionEvent(this, 0, checkboxes.length-1, false));
		repaint();
	}

	public void selectNone()
	{
		for(final JCheckBox checkbox : checkboxes)
			checkbox.setSelected(false);
		for(final ListSelectionListener l : getListSelectionListeners())
			l.valueChanged(new ListSelectionEvent(this, 0, checkboxes.length-1, false));
		repaint();
	}

	public void selectInvert()
	{
		for(final JCheckBox checkbox : checkboxes)
			checkbox.setSelected(!checkbox.isSelected());
		for(final ListSelectionListener l : getListSelectionListeners())
			l.valueChanged(new ListSelectionEvent(this, 0, checkboxes.length-1, false));
		repaint();
	}

	public void select(Predicate<E> predicate, boolean selected)
	{
		for(int i = 0; i < checkboxes.length; i++)
		{
			if(predicate.test(getModel().getElementAt(i)))
			{
				checkboxes[i].setSelected(selected);
				for(final ListSelectionListener l : getListSelectionListeners())
					l.valueChanged(new ListSelectionEvent(this, i, i, false));
			}
		}
		repaint();
	}

	
	JTristateCheckBox checkboxes[] = null;

	public class CellRenderer implements ListCellRenderer<E>
	{

		@Override
		public Component getListCellRendererComponent(final JList<? extends E> list, final E value, final int index, final boolean isSelected, final boolean cellHasFocus)
		{
			if(checkboxes==null || checkboxes.length!=list.getModel().getSize()) checkboxes = new JTristateCheckBox[list.getModel().getSize()];
			if(checkboxes[index]==null)
			{
				checkboxes[index] = new JTristateCheckBox();
				checkboxes[index].setFont(getFont());
				checkboxes[index].setFocusPainted(false);
				checkboxes[index].setBorderPainted(true);
			}
			checkboxes[index].setText(value.toString());
			checkboxes[index].setEnabled(isEnabled());
			checkboxes[index].setBackground(isSelected ? getSelectionBackground() : getBackground());
			checkboxes[index].setForeground(isSelected ? getSelectionForeground() : getForeground());
			checkboxes[index].setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : JCheckBoxList.noFocusBorder); //$NON-NLS-1$
			return checkboxes[index];
		}
	}
}
