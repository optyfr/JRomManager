package jrm.fx.ui.controls;

import javafx.scene.control.ListCell;
import javafx.scene.text.Font;
import jrm.profile.scan.options.Descriptor;

public final class DescriptorCellFactory extends ListCell<Descriptor>
{
	@Override
	protected void updateItem(Descriptor item, boolean empty)
	{
		setFont(new Font(10));
		super.updateItem(item, empty);
		if (item == null || empty)
			setGraphic(null);
		else
			setText(item.getDesc());
	}
}