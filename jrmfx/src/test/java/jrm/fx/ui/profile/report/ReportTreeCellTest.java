package jrm.fx.ui.profile.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Cell;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jrm.profile.data.AnywareBase;
import jrm.profile.data.Container;
import jrm.profile.data.Entity;
import jrm.profile.data.Entry;
import jrm.profile.report.ContainerTZip;
import jrm.profile.report.ContainerUnknown;
import jrm.profile.report.ContainerUnneeded;
import jrm.profile.report.EntryAdd;
import jrm.profile.report.EntryMissing;
import jrm.profile.report.EntryMissingDuplicate;
import jrm.profile.report.EntryOK;
import jrm.profile.report.EntryUnneeded;
import jrm.profile.report.EntryWrongHash;
import jrm.profile.report.EntryWrongName;
import jrm.profile.report.Note;
import jrm.profile.report.RomSuspiciousCRC;
import jrm.profile.report.SubjectSet;

/**
 * Tests for {@code ReportViewController.ReportTreeCell} private inner class.
 * This class handles rendering different types of report entries in the tree view.
 *
 * @since 3.0.5
 */
@TestFxApplication(ReportTreeCellTest.TestApp.class)
@DisplayName("ReportTreeCell Tests")
class ReportTreeCellTest {

	private static TreeCell<Object> reportTreeCell;
	private static Class<?> reportTreeCellClass;
	private static Method updateItemMethod;

	/**
	 * Test application for ReportTreeCell.
	 */
	public static class TestApp extends Application implements TestFxRecordedStage {
		private Stage primaryStage;

		@SuppressWarnings("unchecked")
		@Override
		public void start(Stage primaryStage) {
			this.primaryStage = primaryStage;
			VBox root = new VBox();
			Scene scene = new Scene(root, 800, 600);
			primaryStage.setScene(scene);
			primaryStage.show();

			try {
				// Initialize ReportTreeCell via reflection
				for (Class<?> clazz : ReportViewController.class.getDeclaredClasses()) {
					if (clazz.getSimpleName().equals("ReportTreeCell")) /* NOSONAR */ {
						reportTreeCellClass = clazz;
						break;
					}
				}

				if (reportTreeCellClass != null) {
					// updateItem is declared on Cell, not TreeCell
					updateItemMethod = Cell.class.getDeclaredMethod("updateItem", Object.class, boolean.class);
					updateItemMethod.setAccessible(true);

					Constructor<?> constructor = reportTreeCellClass.getDeclaredConstructor();
					constructor.setAccessible(true);
					reportTreeCell = (TreeCell<Object>) constructor.newInstance();
				}
			} catch (Exception e) {
				String details = String.format("classFound=%s, updateMethodFound=%s, error: %s",
					reportTreeCellClass != null,
					updateItemMethod != null,
					e.toString());
				throw new RuntimeException(details, e);
			}
		}

		@Override
		public Stage recordedStage() {
			return primaryStage;
		}
	}

	/**
	 * Invoke the protected updateItem method via reflection.
	 */
	private void callUpdateItem(Object item, boolean empty) {
		try {
			updateItemMethod.invoke(reportTreeCell, item, empty);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@BeforeEach
	void setUp() {
		reportTreeCell.setGraphic(null);
		reportTreeCell.setText(null);
		callUpdateItem(null, true);
	}

	@Test
	@DisplayName("Should handle empty cell")
	void shouldHandleEmptyCell() {
		callUpdateItem(null, true);
		assertThat(reportTreeCell.getGraphic()).isNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render SubjectSet with MISSING status")
	void shouldRenderSubjectSetMissing() {
		SubjectSet subjectSet = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		when(subjectSet.getStatus()).thenReturn(SubjectSet.Status.MISSING);
		when(subjectSet.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(ware.getDescription()).thenReturn("Description");
		when(subjectSet.getNotes()).thenReturn(Collections.emptyList());

		callUpdateItem(subjectSet, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render SubjectSet with FOUND status and no notes")
	void shouldRenderSubjectSetFoundNoNotes() {
		SubjectSet subjectSet = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		when(subjectSet.getStatus()).thenReturn(SubjectSet.Status.FOUND);
		when(subjectSet.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(ware.getDescription()).thenReturn("Description");
		when(subjectSet.getNotes()).thenReturn(Collections.emptyList());
		when(subjectSet.hasNotes()).thenReturn(false);

		callUpdateItem(subjectSet, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render SubjectSet with FOUND status and fixable notes")
	void shouldRenderSubjectSetFoundFixable() {
		SubjectSet subjectSet = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		Note note = mock(Note.class);
		when(subjectSet.getStatus()).thenReturn(SubjectSet.Status.FOUND);
		when(subjectSet.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(ware.getDescription()).thenReturn("Description");
		when(subjectSet.getNotes()).thenReturn(Collections.singletonList(note));
		when(subjectSet.hasNotes()).thenReturn(true);
		when(subjectSet.isFixable()).thenReturn(true);

		callUpdateItem(subjectSet, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render SubjectSet with FOUND status and non-fixable notes")
	void shouldRenderSubjectSetFoundNonFixable() {
		SubjectSet subjectSet = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		Note note = mock(Note.class);
		when(subjectSet.getStatus()).thenReturn(SubjectSet.Status.FOUND);
		when(subjectSet.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(ware.getDescription()).thenReturn("Description");
		when(subjectSet.getNotes()).thenReturn(Collections.singletonList(note));
		when(subjectSet.hasNotes()).thenReturn(true);
		when(subjectSet.isFixable()).thenReturn(false);

		callUpdateItem(subjectSet, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render SubjectSet with CREATE status fixable")
	void shouldRenderSubjectSetCreateFixable() {
		SubjectSet subjectSet = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		when(subjectSet.getStatus()).thenReturn(SubjectSet.Status.CREATE);
		when(subjectSet.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(ware.getDescription()).thenReturn("Description");
		when(subjectSet.getNotes()).thenReturn(Collections.emptyList());
		when(subjectSet.isFixable()).thenReturn(true);

		callUpdateItem(subjectSet, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render SubjectSet with CREATE status non-fixable")
	void shouldRenderSubjectSetCreateNonFixable() {
		SubjectSet subjectSet = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		when(subjectSet.getStatus()).thenReturn(SubjectSet.Status.CREATE);
		when(subjectSet.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(ware.getDescription()).thenReturn("Description");
		when(subjectSet.getNotes()).thenReturn(Collections.emptyList());
		when(subjectSet.isFixable()).thenReturn(false);

		callUpdateItem(subjectSet, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render SubjectSet with CREATEFULL status")
	void shouldRenderSubjectSetCreateFull() {
		SubjectSet subjectSet = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		when(subjectSet.getStatus()).thenReturn(SubjectSet.Status.CREATEFULL);
		when(subjectSet.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(ware.getDescription()).thenReturn("Description");
		when(subjectSet.getNotes()).thenReturn(Collections.emptyList());
		when(subjectSet.isFixable()).thenReturn(true);

		callUpdateItem(subjectSet, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render SubjectSet with UNNEEDED status")
	void shouldRenderSubjectSetUnneeded() {
		SubjectSet subjectSet = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		when(subjectSet.getStatus()).thenReturn(SubjectSet.Status.UNNEEDED);
		when(subjectSet.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(ware.getDescription()).thenReturn("Description");
		when(subjectSet.getNotes()).thenReturn(Collections.emptyList());

		callUpdateItem(subjectSet, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render SubjectSet with UNKNOWN status")
	void shouldRenderSubjectSetUnknown() {
		SubjectSet subjectSet = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		when(subjectSet.getStatus()).thenReturn(SubjectSet.Status.UNKNOWN);
		when(subjectSet.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(ware.getDescription()).thenReturn("Description");
		when(subjectSet.getNotes()).thenReturn(Collections.emptyList());

		callUpdateItem(subjectSet, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render ContainerTZip")
	void shouldRenderContainerTZip() {
		ContainerTZip container = mock(ContainerTZip.class);
		when(container.toString()).thenReturn("test.zip");

		callUpdateItem(container, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render ContainerUnknown")
	void shouldRenderContainerUnknown() {
		ContainerUnknown container = mock(ContainerUnknown.class);
		when(container.toString()).thenReturn("unknown.zip");

		callUpdateItem(container, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render ContainerUnneeded")
	void shouldRenderContainerUnneeded() {
		ContainerUnneeded container = mock(ContainerUnneeded.class);
		when(container.toString()).thenReturn("unneeded.zip");

		callUpdateItem(container, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render RomSuspiciousCRC")
	void shouldRenderRomSuspiciousCRC() {
		RomSuspiciousCRC rom = mock(RomSuspiciousCRC.class);
		when(rom.toString()).thenReturn("suspicious.rom");

		callUpdateItem(rom, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render EntryOK")
	void shouldRenderEntryOK() {
		EntryOK entry = mock(EntryOK.class);
		SubjectSet parent = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		Entity entity = mock(Entity.class);
		
		when(entry.getParent()).thenReturn(parent);
		when(parent.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(entry.getEntity()).thenReturn(entity);
		when(entity.getNormalizedName()).thenReturn("test.rom");

		callUpdateItem(entry, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@ParameterizedTest(name = "{0}")
	@CsvSource({
		"SHA1, abc123, , ",
		"MD5,  , def456, ",
		"CRC,  , , 789abc"
	})
	@DisplayName("Should render EntryMissing with Entity hash")
	void shouldRenderEntryMissingWithHash(String hashType, String sha1, String md5, String crc) {
		EntryMissing entry = mock(EntryMissing.class);
		SubjectSet parent = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		Entity entity = mock(Entity.class);

		when(entry.getParent()).thenReturn(parent);
		when(parent.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(entry.getEntity()).thenReturn(entity);
		when(entity.getName()).thenReturn("test.rom");
		when(entity.getSha1()).thenReturn(sha1);
		when(entity.getMd5()).thenReturn(md5);
		when(entity.getCrc()).thenReturn(crc);

		callUpdateItem(entry, false);

		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render EntryMissingDuplicate")
	void shouldRenderEntryMissingDuplicate() {
		EntryMissingDuplicate entry = mock(EntryMissingDuplicate.class);
		SubjectSet parent = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		Entry entryItem = mock(Entry.class);
		Entity entity = mock(Entity.class);
		
		when(entry.getParent()).thenReturn(parent);
		when(parent.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(entry.getEntry()).thenReturn(entryItem);
		when(entryItem.getRelFile()).thenReturn("path/to/file.rom");
		when(entry.getEntity()).thenReturn(entity);
		when(entity.getName()).thenReturn("test.rom");

		callUpdateItem(entry, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render EntryAdd")
	void shouldRenderEntryAdd() {
		EntryAdd entry = mock(EntryAdd.class);
		SubjectSet parent = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		Entity entity = mock(Entity.class);
		Entry entryItem = mock(Entry.class);
		Container parentContainer = mock(Container.class);
		
		when(entry.getParent()).thenReturn(parent);
		when(parent.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(entry.getEntity()).thenReturn(entity);
		when(entity.getNormalizedName()).thenReturn("test.rom");
		when(entry.getEntry()).thenReturn(entryItem);
		when(entryItem.getParent()).thenReturn(parentContainer);
		when(parentContainer.getRelFile()).thenReturn(new java.io.File("parent.zip"));
		when(entryItem.getRelFile()).thenReturn("path/to/file.rom");

		callUpdateItem(entry, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render EntryUnneeded with SHA1")
	void shouldRenderEntryUnneededWithSHA1() {
		EntryUnneeded entry = mock(EntryUnneeded.class);
		SubjectSet parent = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		Entry entryItem = mock(Entry.class);
		
		when(entry.getParent()).thenReturn(parent);
		when(parent.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(entry.getEntry()).thenReturn(entryItem);
		when(entryItem.getRelFile()).thenReturn("path/to/file.rom");
		when(entryItem.getSha1()).thenReturn("abc123");

		callUpdateItem(entry, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render EntryUnneeded with MD5")
	void shouldRenderEntryUnneededWithMD5() {
		EntryUnneeded entry = mock(EntryUnneeded.class);
		SubjectSet parent = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		Entry entryItem = mock(Entry.class);
		
		when(entry.getParent()).thenReturn(parent);
		when(parent.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(entry.getEntry()).thenReturn(entryItem);
		when(entryItem.getRelFile()).thenReturn("path/to/file.rom");
		when(entryItem.getSha1()).thenReturn(null);
		when(entryItem.getMd5()).thenReturn("def456");

		callUpdateItem(entry, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render EntryUnneeded with CRC")
	void shouldRenderEntryUnneededWithCRC() {
		EntryUnneeded entry = mock(EntryUnneeded.class);
		SubjectSet parent = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		Entry entryItem = mock(Entry.class);
		
		when(entry.getParent()).thenReturn(parent);
		when(parent.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(entry.getEntry()).thenReturn(entryItem);
		when(entryItem.getRelFile()).thenReturn("path/to/file.rom");
		when(entryItem.getSha1()).thenReturn(null);
		when(entryItem.getMd5()).thenReturn(null);
		when(entryItem.getCrc()).thenReturn("789abc");

		callUpdateItem(entry, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render EntryWrongHash with no SHA1 or MD5 (uses CRC)")
	void shouldRenderEntryWrongHashWithCRC() {
		EntryWrongHash entry = mock(EntryWrongHash.class);
		SubjectSet parent = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		Entry entryItem = mock(Entry.class);
		
		when(entry.getParent()).thenReturn(parent);
		when(parent.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(entry.getEntry()).thenReturn(entryItem);
		when(entryItem.getRelFile()).thenReturn("path/to/file.rom");
		when(entryItem.getSha1()).thenReturn(null);
		when(entryItem.getMd5()).thenReturn(null);
		when(entryItem.getCrc()).thenReturn("abc123");
		when(entry.getCrc()).thenReturn("def456");

		callUpdateItem(entry, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render EntryWrongHash with MD5 (no SHA1)")
	void shouldRenderEntryWrongHashWithMD5() {
		EntryWrongHash entry = mock(EntryWrongHash.class);
		SubjectSet parent = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		Entry entryItem = mock(Entry.class);
		
		when(entry.getParent()).thenReturn(parent);
		when(parent.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(entry.getEntry()).thenReturn(entryItem);
		when(entryItem.getRelFile()).thenReturn("path/to/file.rom");
		when(entryItem.getSha1()).thenReturn(null);
		when(entryItem.getMd5()).thenReturn("abc123");
		when(entry.getMd5()).thenReturn("def456");

		callUpdateItem(entry, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render EntryWrongHash with SHA1")
	void shouldRenderEntryWrongHashWithSHA1() {
		EntryWrongHash entry = mock(EntryWrongHash.class);
		SubjectSet parent = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		Entry entryItem = mock(Entry.class);
		
		when(entry.getParent()).thenReturn(parent);
		when(parent.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(entry.getEntry()).thenReturn(entryItem);
		when(entryItem.getRelFile()).thenReturn("path/to/file.rom");
		when(entryItem.getSha1()).thenReturn("abc123");
		when(entry.getSha1()).thenReturn("def456");

		callUpdateItem(entry, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render EntryWrongName")
	void shouldRenderEntryWrongName() {
		EntryWrongName entry = mock(EntryWrongName.class);
		SubjectSet parent = mock(SubjectSet.class);
		AnywareBase ware = mock(AnywareBase.class);
		Entry entryItem = mock(Entry.class);
		Entity entity = mock(Entity.class);
		
		when(entry.getParent()).thenReturn(parent);
		when(parent.getWare()).thenReturn(ware);
		when(ware.getFullName()).thenReturn("Test Game");
		when(entry.getEntry()).thenReturn(entryItem);
		when(entryItem.getName()).thenReturn("wrong.rom");
		when(entry.getEntity()).thenReturn(entity);
		when(entity.getNormalizedName()).thenReturn("correct.rom");

		callUpdateItem(entry, false);
		
		assertThat(reportTreeCell.getGraphic()).isNotNull();
		assertThat(reportTreeCell.getText()).isNull();
	}

	@Test
	@DisplayName("Should render unknown item type with toString")
	void shouldRenderUnknownItemType() {
		Object unknownItem = new Object() {
			@Override
			public String toString() {
				return "Unknown Item";
			}
		};

		callUpdateItem(unknownItem, false);
		
		assertThat(reportTreeCell.getText()).isEqualTo("Unknown Item");
	}

	@Test
	@DisplayName("Should render plain Note with toString")
	void shouldRenderPlainNote() {
		Note note = mock(Note.class);
		when(note.toString()).thenReturn("A note");

		callUpdateItem(note, false);
		
		assertThat(reportTreeCell.getText()).isEqualTo("A note");
	}

	@Test
	@DisplayName("Should get folder icon for FOUND with no notes")
	void shouldGetFolderIconForFoundNoNotes() throws Exception {
		Method m = reportTreeCellClass.getDeclaredMethod("getFolderIcon", Object.class, boolean.class);
		m.setAccessible(true);

		SubjectSet s = mock(SubjectSet.class);
		when(s.getStatus()).thenReturn(SubjectSet.Status.FOUND);
		when(s.hasNotes()).thenReturn(false);

		String icon = (String) m.invoke(reportTreeCell, s, false);
		assertThat(icon).contains("folder_closed_green.png");
	}

	@Test
	@DisplayName("Should get folder icon for FOUND with fixable notes")
	void shouldGetFolderIconForFoundFixable() throws Exception {
		Method m = reportTreeCellClass.getDeclaredMethod("getFolderIcon", Object.class, boolean.class);
		m.setAccessible(true);

		SubjectSet s = mock(SubjectSet.class);
		when(s.getStatus()).thenReturn(SubjectSet.Status.FOUND);
		when(s.hasNotes()).thenReturn(true);
		when(s.isFixable()).thenReturn(true);

		String icon = (String) m.invoke(reportTreeCell, s, false);
		assertThat(icon).contains("folder_closed_purple.png");
	}

	@Test
	@DisplayName("Should get folder icon for FOUND with non-fixable notes")
	void shouldGetFolderIconForFoundNonFixable() throws Exception {
		Method m = reportTreeCellClass.getDeclaredMethod("getFolderIcon", Object.class, boolean.class);
		m.setAccessible(true);

		SubjectSet s = mock(SubjectSet.class);
		when(s.getStatus()).thenReturn(SubjectSet.Status.FOUND);
		when(s.hasNotes()).thenReturn(true);
		when(s.isFixable()).thenReturn(false);

		String icon = (String) m.invoke(reportTreeCell, s, false);
		assertThat(icon).contains("folder_closed_orange.png");
	}

	@Test
	@DisplayName("Should get folder icon for MISSING")
	void shouldGetFolderIconForMissing() throws Exception {
		Method m = reportTreeCellClass.getDeclaredMethod("getFolderIcon", Object.class, boolean.class);
		m.setAccessible(true);

		SubjectSet s = mock(SubjectSet.class);
		when(s.getStatus()).thenReturn(SubjectSet.Status.MISSING);

		String icon = (String) m.invoke(reportTreeCell, s, false);
		assertThat(icon).contains("folder_closed_red.png");
	}

	@Test
	@DisplayName("Should get folder icon for CREATE fixable")
	void shouldGetFolderIconForCreateFixable() throws Exception {
		Method m = reportTreeCellClass.getDeclaredMethod("getFolderIcon", Object.class, boolean.class);
		m.setAccessible(true);

		SubjectSet s = mock(SubjectSet.class);
		when(s.getStatus()).thenReturn(SubjectSet.Status.CREATE);
		when(s.isFixable()).thenReturn(true);

		String icon = (String) m.invoke(reportTreeCell, s, false);
		assertThat(icon).contains("folder_closed_blue.png");
	}

	@Test
	@DisplayName("Should get folder icon for CREATE non-fixable")
	void shouldGetFolderIconForCreateNonFixable() throws Exception {
		Method m = reportTreeCellClass.getDeclaredMethod("getFolderIcon", Object.class, boolean.class);
		m.setAccessible(true);

		SubjectSet s = mock(SubjectSet.class);
		when(s.getStatus()).thenReturn(SubjectSet.Status.CREATE);
		when(s.isFixable()).thenReturn(false);

		String icon = (String) m.invoke(reportTreeCell, s, false);
		assertThat(icon).contains("folder_closed_orange.png");
	}

	@Test
	@DisplayName("Should get folder icon for UNNEEDED")
	void shouldGetFolderIconForUnneeded() throws Exception {
		Method m = reportTreeCellClass.getDeclaredMethod("getFolderIcon", Object.class, boolean.class);
		m.setAccessible(true);

		SubjectSet s = mock(SubjectSet.class);
		when(s.getStatus()).thenReturn(SubjectSet.Status.UNNEEDED);

		String icon = (String) m.invoke(reportTreeCell, s, false);
		assertThat(icon).contains("folder_closed_gray.png");
	}

	@Test
	@DisplayName("Should get folder icon for expanded state")
	void shouldGetFolderIconExpanded() throws Exception {
		Method m = reportTreeCellClass.getDeclaredMethod("getFolderIcon", Object.class, boolean.class);
		m.setAccessible(true);

		SubjectSet s = mock(SubjectSet.class);
		when(s.getStatus()).thenReturn(SubjectSet.Status.MISSING);

		String icon = (String) m.invoke(reportTreeCell, s, true);
		assertThat(icon).contains("folder_open_red.png");
	}

	@Test
	@DisplayName("Should get folder icon for non-SubjectSet value")
	void shouldGetFolderIconForNonSubjectSet() throws Exception {
		Method m = reportTreeCellClass.getDeclaredMethod("getFolderIcon", Object.class, boolean.class);
		m.setAccessible(true);

		String icon = (String) m.invoke(reportTreeCell, "plain", false);
		assertThat(icon).contains("folder_closed.png");
	}
}
