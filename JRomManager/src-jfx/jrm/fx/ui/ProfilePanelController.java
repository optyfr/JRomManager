package jrm.fx.ui;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import jrm.fx.ui.controls.DateCellFactory;
import jrm.fx.ui.controls.NameCellFactory;
import jrm.fx.ui.controls.VersionCellFactory;
import jrm.fx.ui.profile.manager.DirItem;
import jrm.fx.ui.profile.manager.HaveNTotalCellFactory;
import jrm.profile.manager.Dir;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.manager.ProfileNFOStats.HaveNTotal;
import jrm.security.Session;
import jrm.security.Sessions;
import lombok.Setter;


public class ProfilePanelController implements Initializable
{
	@FXML Button btnLoad;
	@FXML Button btnImportDat;
	@FXML Button btnImportSL;
	@FXML TreeView<Dir> profilesTree;
	@FXML TableView<ProfileNFO> profilesList;
	@FXML TableColumn<ProfileNFO, String> profileCol;
	@FXML TableColumn<ProfileNFO, String> profileVersionCol;
	@FXML TableColumn<ProfileNFO, HaveNTotal> profileHaveSetsCol;
	@FXML TableColumn<ProfileNFO, HaveNTotal> profileHaveRomsCol;
	@FXML TableColumn<ProfileNFO, HaveNTotal> profileHaveDisksCol;
	@FXML TableColumn<ProfileNFO, Date> profileCreatedCol;
	@FXML TableColumn<ProfileNFO, Date> profileLastScanCol;
	@FXML TableColumn<ProfileNFO, Date> profileLastFixCol;

	final Session session = Sessions.getSingleSession();
	private @Setter ProfileLoader profileLoader;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		btnLoad.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/add.png")));
		btnImportDat.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/script_go.png")));
		btnImportSL.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/application_go.png")));
		profilesTree.setRoot(new DirItem(session.getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile()));
		profilesTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> profilesList.setItems(FXCollections.observableArrayList(ProfileNFO.list(session, newValue.getValue().getFile()))));
		profilesTree.getSelectionModel().select(0);
		profileCol.setCellFactory(param -> new NameCellFactory<>());
		profileCol.setCellValueFactory(param -> new ObservableValueBase<String>()
		{
			@Override
			public String getValue()
			{
				return param.getValue().getName();
			}
		});
		profileVersionCol.setCellFactory(param -> new VersionCellFactory<>());
		profileVersionCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public String getValue()
			{
				return param.getValue().getStats().getVersion();
			}
		});
		profileHaveSetsCol.setCellFactory(param -> new HaveNTotalCellFactory<>());
		profileHaveSetsCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public HaveNTotal getValue()
			{
				return param.getValue().getStats().getSets();
			}
		});
		profileHaveRomsCol.setCellFactory(param -> new HaveNTotalCellFactory<>());
		profileHaveRomsCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public HaveNTotal getValue()
			{
				return param.getValue().getStats().getRoms();
			}
		});
		profileHaveDisksCol.setCellFactory(param -> new HaveNTotalCellFactory<>());
		profileHaveDisksCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public HaveNTotal getValue()
			{
				return param.getValue().getStats().getDisks();
			}
		});
		profileCreatedCol.setCellFactory(param -> new DateCellFactory());
		profileCreatedCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public Date getValue()
			{
				return param.getValue().getStats().getCreated();
			}
		});
		profileLastScanCol.setCellFactory(param -> new DateCellFactory());
		profileLastScanCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public Date getValue()
			{
				return param.getValue().getStats().getScanned();
			}
		});
		profileLastFixCol.setCellFactory(param -> new DateCellFactory());
		profileLastFixCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public Date getValue()
			{
				return param.getValue().getStats().getFixed();
			}
		});
		profilesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> btnLoad.setDisable(newValue == null));
		profilesList.setRowFactory(tv -> {
			final var row = new TableRow<ProfileNFO>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && !row.isEmpty())
					profileLoader.loadProfile(session, row.getItem());
			});
			return row;
		});
	}

	@FXML void actionLoad(ActionEvent e)
	{
		final var profile = profilesList.getSelectionModel().getSelectedItem();
		if (profile != null)
			profileLoader.loadProfile(session, profile);
	}
	
	
	
}
