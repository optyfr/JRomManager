package jrm.fx.ui.controls;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import jrm.aui.basic.SrcDstResult;
import jrm.fx.ui.misc.DragNDrop;
import jrm.fx.ui.misc.DragNDrop.SetFilesCallBack;

public class DropCell extends TableCell<SrcDstResult, String>
{
	public interface DropCellCallback
	{
		void call(List<SrcDstResult> sdrlist, List<File> files);
	}
	
	public DropCell(TableView<SrcDstResult> view, DropCell.DropCellCallback cb, boolean dirOnly)
	{
		final SetFilesCallBack drop = files -> {
			int dropidx = this.getIndex();
			int count = view.getItems().size();
			if (dropidx > count)
				dropidx = count;
			final var sdrlist = new ArrayList<SrcDstResult>(); 
			for (int i = 0; i < files.size(); i++)
			{
				if(dropidx + i >= count)
					view.getItems().add(new SrcDstResult());
				sdrlist.add(view.getItems().get(dropidx + i));
			}
			cb.call(sdrlist, files);
		};
		if(dirOnly)
			new DragNDrop(this).addDirs(drop);
		else
			new DragNDrop(this).addAny(drop);
	}
	
	@Override
	protected void updateItem(String item, boolean empty)
	{
		super.updateItem(item, empty);
		setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
		setText(empty?"":item);
		setGraphic(null);
	}
}