package jrm.fx.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;
import jrm.compressors.SevenZipOptions;
import jrm.compressors.ZipLevel;
import jrm.compressors.ZipTempThreshold;
import jrm.fx.ui.misc.DragNDrop;
import jrm.locale.Messages;
import jrm.misc.Log;
import jrm.misc.ProfileSettingsEnum;
import jrm.misc.SettingsEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * FXML controller for the settings panel.
 * <p>
 * Manages application-wide settings including threading, style sheet, backup
 * destination, compressor options (ZIP level, 7z options), and debug log level.
 *
 * @since 2.5
 */
public class SettingsPanelController extends BaseController {
    /** The general settings pane. */
    @FXML
    TitledPane paneGeneral;
    /** The threading choice box. */
    @FXML
    ChoiceBox<ThreadCnt> cbThreading;
    /** The style sheet choice box. */
    @FXML
    ChoiceBox<JRMScene.StyleSheet> cbStyleSheet;
    /** The backup destination enabled checkbox. */
    @FXML
    CheckBox ckbBackupDst;
    /** The backup destination text field. */
    @FXML
    TextField tfBackupDst;
    /** The backup destination browse button. */
    @FXML
    Button btBackupDst;

    /** The compressors settings pane. */
    @FXML
    TitledPane paneCompressors;
    /** The ZIP temp threshold choice box. */
    @FXML
    ChoiceBox<ZipTempThreshold> cbZipTempThreshold;
    /** The ZIP compression level choice box. */
    @FXML
    ChoiceBox<ZipLevel> cbZipLevel;
    /** The 7z options choice box. */
    @FXML
    ChoiceBox<SevenZipOptions> cb7zArgs;
    /** The 7z thread count spinner. */
    @FXML
    Spinner<Integer> tf7zThreads;
    /** The 7z solid archive checkbox. */
    @FXML
    CheckBox ckb7ZSolid;

    /** The debug settings pane. */
    @FXML
    TitledPane paneDebug;
    /** The debug level choice box. */
    @FXML
    ChoiceBox<Level> cbDbgLevel;
    /** The garbage collection button. */
    @FXML
    Button gc;
    /** The status text field. */
    @FXML
    TextField status;

    private static final Level[] levels = new Level[] { Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL };

    /** The scheduler. */
    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initGeneral();
        initCompressors();
        initDebug();
    }

    /**
     * Initializes the general settings section.
     */
    private void initGeneral() {
        ImageView generaliv = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/cog.png"));
        generaliv.setPreserveRatio(true);
        generaliv.getStyleClass().add("icon");
        paneGeneral.setGraphic(generaliv);
        cbThreading.getItems().setAll(ThreadCnt.build());
        cbThreading.getSelectionModel().select(new ThreadCnt(session.getUser().getSettings().getProperty(SettingsEnum.thread_count, Integer.class)));
        cbThreading.getSelectionModel().selectedItemProperty()
                .addListener((_, _, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.thread_count, newValue.getCnt()));
        ckbBackupDst.selectedProperty().addListener((_, _, newValue) -> {
            tfBackupDst.setDisable(!newValue);
            btBackupDst.setDisable(!newValue);
            session.getUser().getSettings().setProperty(ProfileSettingsEnum.backup_dest_dir_enabled, newValue); // $NON-NLS-1$
        });
        ckbBackupDst.setSelected(session.getUser().getSettings().getProperty(ProfileSettingsEnum.backup_dest_dir_enabled, Boolean.class));
        ImageView backupdstiv = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/disk.png"));
        backupdstiv.setPreserveRatio(true);
        backupdstiv.getStyleClass().add("icon");
        btBackupDst.setGraphic(backupdstiv);
        tfBackupDst.setText(session.getUser().getSettings().getProperty(ProfileSettingsEnum.backup_dest_dir)); // $NON-NLS-1$
        new DragNDrop(btBackupDst).addDir(txt -> session.getUser().getSettings().setProperty(ProfileSettingsEnum.backup_dest_dir, txt));
        btBackupDst.setOnAction(_ -> chooseDir(btBackupDst, tfBackupDst.getText(), null, path -> {
            session.getUser().getSettings().setProperty(ProfileSettingsEnum.backup_dest_dir_enabled, path.toString());
            tfBackupDst.setText(path.toString());
        }));
        cbStyleSheet.getItems().setAll(JRMScene.StyleSheet.values());
        cbStyleSheet.getSelectionModel().select(session.getUser().getSettings().getEnumProperty(JRMScene.ScenePrefs.style_sheet, JRMScene.StyleSheet.class));
        cbStyleSheet.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            session.getUser().getSettings().setEnumProperty(JRMScene.ScenePrefs.style_sheet, newValue);
            MainFrame.applyCSS();
        });
    }

    /**
     * Initializes the compressor settings section.
     */
    private void initCompressors() {
        ImageView compressoriv = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/compress.png"));
        compressoriv.setPreserveRatio(true);
        compressoriv.getStyleClass().add("icon");
        paneCompressors.setGraphic(compressoriv);
        cbZipTempThreshold.getItems().setAll(ZipTempThreshold.values());
        cbZipTempThreshold.setConverter(new StringConverter<>() {

            @Override
            public String toString(ZipTempThreshold object) {
                return object.getDesc();
            }

            @Override
            public ZipTempThreshold fromString(String string) {
                return null;
            }
        });
        cbZipTempThreshold.getSelectionModel().select(session.getUser().getSettings().getEnumProperty(SettingsEnum.zip_temp_threshold, ZipTempThreshold.class));
        cbZipTempThreshold.getSelectionModel().selectedItemProperty()
                .addListener((_, _, newValue) -> session.getUser().getSettings().setEnumProperty(SettingsEnum.zip_temp_threshold, newValue));
        cbZipLevel.getItems().setAll(ZipLevel.values());
        cbZipLevel.setConverter(new StringConverter<>() {

            @Override
            public String toString(ZipLevel object) {
                return object.getName();
            }

            @Override
            public ZipLevel fromString(String string) {
                return null;
            }
        });
        cbZipLevel.getSelectionModel().select(session.getUser().getSettings().getEnumProperty(SettingsEnum.zip_compression_level, ZipLevel.class));
        cbZipLevel.getSelectionModel().selectedItemProperty()
                .addListener((_, _, newValue) -> session.getUser().getSettings().setEnumProperty(SettingsEnum.zip_compression_level, newValue));

        cb7zArgs.getItems().setAll(SevenZipOptions.values());
        cb7zArgs.setConverter(new StringConverter<>() {

            @Override
            public String toString(SevenZipOptions object) {
                return object.getName();
            }

            @Override
            public SevenZipOptions fromString(String string) {
                return null;
            }
        });
        cb7zArgs.getSelectionModel().select(session.getUser().getSettings().getEnumProperty(SettingsEnum.sevenzip_level, SevenZipOptions.class));
        tf7zThreads.setValueFactory(new SpinnerValueFactory<Integer>() {
            @Override
            public void decrement(int steps) {
                setValue(Math.max(-1, getValue() - steps));
            }

            @Override
            public void increment(int steps) {
                setValue(getValue() + steps);
            }
        });
        tf7zThreads.getValueFactory().setValue(session.getUser().getSettings().getProperty(SettingsEnum.sevenzip_threads, Integer.class));
        tf7zThreads.valueProperty().addListener((_, _, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.sevenzip_threads, newValue));
        ckb7ZSolid.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.sevenzip_solid, Boolean.class));
        ckb7ZSolid.selectedProperty().addListener((_, _, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.sevenzip_solid, newValue));
    }

    /**
     * Initializes the debug pane with log level selector and memory monitor.
     */
    private void initDebug() {
        ImageView debugiv = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bug.png"));
        debugiv.setPreserveRatio(true);
        debugiv.getStyleClass().add("icon");
        paneDebug.setGraphic(debugiv);
        cbDbgLevel.getItems().setAll(levels);
        cbDbgLevel.getSelectionModel().select(Level.parse(session.getUser().getSettings().getProperty(SettingsEnum.debug_level)));
        cbDbgLevel.getSelectionModel().selectedItemProperty().addListener((ChangeListener<Level>) (_, _, newValue) -> {
            session.getUser().getSettings().setProperty(SettingsEnum.debug_level, newValue.toString());
            Log.setLevel(newValue);
        });
        gc.setOnAction(_ -> {
            System.gc(); // NOSONAR
            updateMemory();
        });
        scheduler.scheduleAtFixedRate(this::updateMemory, 0, 20, TimeUnit.SECONDS);
    }

    private static final String XX_MIB = "%.2f MiB";

    /**
     * Update memory.
     */
    void updateMemory() {
        final Runtime rt = Runtime.getRuntime();
        status.setText(String.format(Messages.getString("MainFrame.MemoryUsage"), String.format(XX_MIB, rt.totalMemory() / 1048576.0), //$NON-NLS-1$
                String.format(XX_MIB, (rt.totalMemory() - rt.freeMemory()) / 1048576.0), String.format(XX_MIB, rt.freeMemory() / 1048576.0),
                String.format(XX_MIB, rt.maxMemory() / 1048576.0))); // $NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

    @RequiredArgsConstructor
    private static class ThreadCnt {
        /**
         * The thread count.
         *
         * @return the thread count
         */
        final @Getter int cnt;
        /**
         * The thread name.
         *
         * @return the thread name
         */
        final @Getter String name;

        public ThreadCnt(int cnt) {
            this.cnt = cnt;
            this.name = null;
        }

        @Override
        public String toString() {
            return name != null ? name : Integer.toString(cnt);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (obj instanceof ThreadCnt tc)
                return this.cnt == tc.cnt;
            return super.equals(obj);
        }

        static ThreadCnt[] build() {
            ArrayList<ThreadCnt> list = new ArrayList<>();
            list.add(new ThreadCnt(-1, "Adaptive"));
            list.add(new ThreadCnt(0, "Max available"));
            for (var i = 1; i <= Runtime.getRuntime().availableProcessors(); i++)
                list.add(new ThreadCnt(i));
            return list.toArray(ThreadCnt[]::new);
        }
    }
}
