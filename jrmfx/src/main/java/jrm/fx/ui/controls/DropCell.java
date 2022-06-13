package jrm.fx.ui.controls;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import jrm.fx.ui.misc.DragNDrop;
import jrm.fx.ui.misc.DragNDrop.SetFilesCallBack;
import jrm.fx.ui.misc.SrcDstResult;

public class DropCell extends TableCell<SrcDstResult, String>
{
	public interface DropCellCallback
	{
		void call(List<SrcDstResult> sdrlist, List<File> files);
	}
	
	public DropCell(TableView<SrcDstResult> view, DropCell.DropCellCallback cb, Predicate<File> filter)
	{
		final SetFilesCallBack drop = files -> process(view, getIndex(), files, cb);
		setFont(new Font(10));
		new DragNDrop(this).addFiltered(filter, drop);
	}
	
	public static void process(TableView<SrcDstResult> view, int startIndex, List<File> files, DropCell.DropCellCallback cb)
	{
		int count = view.getItems().size();
		if (startIndex > count || startIndex < 0)
			startIndex = count;
		final var sdrlist = new ArrayList<SrcDstResult>(); 
		for (int i = 0; i < files.size(); i++)
		{
			if(startIndex + i >= count)
				view.getItems().add(new SrcDstResult());
			sdrlist.add(view.getItems().get(startIndex + i));
		}
		cb.call(sdrlist, files);
	}
	
	@Override
	protected void updateItem(String item, boolean empty)
	{
		setFont(new Font(10));
		super.updateItem(item, empty);
		if(empty)
			setText("");
		else
		{
			setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
			setText(item);
			setTooltip(new Tooltip(item));
		}
		setGraphic(null);
	}
}