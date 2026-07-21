package jrm.misc;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.eclipsesource.json.JsonObject;

import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;

/**
 * Tests for {@link ProfileSettings} configuration management.
 */
@DisplayName("ProfileSettings tests")
class ProfileSettingsTest {

	private ProfileSettings settings;

	@BeforeEach
	void setUp() {
		settings = new ProfileSettings();
	}

	@Test
	@DisplayName("should create new instance with null fields")
	void shouldCreateNewInstanceWithNullFields() {
		assertThat(settings.getMergeMode()).isNull();
		assertThat(settings.getImplicitMerge()).isNull();
		assertThat(settings.getHashCollisionMode()).isNull();
	}

	@Test
	@DisplayName("should set and get merge mode")
	void shouldSetAndGetMergeMode() {
		settings.setMergeMode(MergeOptions.SPLIT);
		assertThat(settings.getMergeMode()).isEqualTo(MergeOptions.SPLIT);

		settings.setMergeMode(MergeOptions.MERGE);
		assertThat(settings.getMergeMode()).isEqualTo(MergeOptions.MERGE);

		settings.setMergeMode(MergeOptions.NOMERGE);
		assertThat(settings.getMergeMode()).isEqualTo(MergeOptions.NOMERGE);
	}

	@Test
	@DisplayName("should set and get implicit merge flag")
	void shouldSetAndGetImplicitMergeFlag() {
		settings.setImplicitMerge(true);
		assertThat(settings.getImplicitMerge()).isTrue();

		settings.setImplicitMerge(false);
		assertThat(settings.getImplicitMerge()).isFalse();
	}

	@Test
	@DisplayName("should set and get hash collision mode")
	void shouldSetAndGetHashCollisionMode() {
		settings.setHashCollisionMode(HashCollisionOptions.SINGLEFILE);
		assertThat(settings.getHashCollisionMode()).isEqualTo(HashCollisionOptions.SINGLEFILE);

		settings.setHashCollisionMode(HashCollisionOptions.SINGLECLONE);
		assertThat(settings.getHashCollisionMode()).isEqualTo(HashCollisionOptions.SINGLECLONE);

		settings.setHashCollisionMode(HashCollisionOptions.ALLCLONES);
		assertThat(settings.getHashCollisionMode()).isEqualTo(HashCollisionOptions.ALLCLONES);
	}

	@Test
	@DisplayName("should propagate merge_mode property to mergeMode field")
	void shouldPropagateMergeModeProperty() {
		settings.setProperty(ProfileSettingsEnum.merge_mode, MergeOptions.SPLIT.toString());
		assertThat(settings.getMergeMode()).isEqualTo(MergeOptions.SPLIT);

		settings.setProperty(ProfileSettingsEnum.merge_mode, MergeOptions.MERGE.toString());
		assertThat(settings.getMergeMode()).isEqualTo(MergeOptions.MERGE);
	}

	@Test
	@DisplayName("should propagate implicit_merge property to implicitMerge field")
	void shouldPropagateImplicitMergeProperty() {
		settings.setProperty(ProfileSettingsEnum.implicit_merge, "true");
		assertThat(settings.getImplicitMerge()).isTrue();

		settings.setProperty(ProfileSettingsEnum.implicit_merge, "false");
		assertThat(settings.getImplicitMerge()).isFalse();
	}

	@Test
	@DisplayName("should propagate hash_collision_mode property to hashCollisionMode field")
	void shouldPropagateHashCollisionModeProperty() {
		settings.setProperty(ProfileSettingsEnum.hash_collision_mode, HashCollisionOptions.SINGLEFILE.toString());
		assertThat(settings.getHashCollisionMode()).isEqualTo(HashCollisionOptions.SINGLEFILE);

		settings.setProperty(ProfileSettingsEnum.hash_collision_mode, HashCollisionOptions.SINGLECLONE.toString());
		assertThat(settings.getHashCollisionMode()).isEqualTo(HashCollisionOptions.SINGLECLONE);
	}

	@Test
	@DisplayName("should handle null values in propagate")
	void shouldHandleNullValuesInPropagate() {
		settings.setMergeMode(MergeOptions.SPLIT);
		settings.setProperty(ProfileSettingsEnum.merge_mode, null);
		assertThat(settings.getMergeMode()).isNull();

		settings.setImplicitMerge(true);
		settings.setProperty(ProfileSettingsEnum.implicit_merge, null);
		assertThat(settings.getImplicitMerge()).isNull();

		settings.setHashCollisionMode(HashCollisionOptions.SINGLEFILE);
		settings.setProperty(ProfileSettingsEnum.hash_collision_mode, null);
		assertThat(settings.getHashCollisionMode()).isNull();
	}

	@Test
	@DisplayName("should set and get boolean properties")
	void shouldSetAndGetBooleanProperties() {
		settings.setProperty(ProfileSettingsEnum.need_sha1_or_md5, true);
		assertThat(settings.getProperty(ProfileSettingsEnum.need_sha1_or_md5, Boolean.class)).isTrue();

		settings.setProperty(ProfileSettingsEnum.use_parallelism, false);
		assertThat(settings.getProperty(ProfileSettingsEnum.use_parallelism, Boolean.class)).isFalse();
	}

	@Test
	@DisplayName("should set and get string properties")
	void shouldSetAndGetStringProperties() {
		settings.setProperty(ProfileSettingsEnum.format, FormatOptions.ZIP.toString());
		assertThat(settings.getProperty(ProfileSettingsEnum.format)).isEqualTo(FormatOptions.ZIP.toString());

		settings.setProperty(ProfileSettingsEnum.format, FormatOptions.DIR.toString());
		assertThat(settings.getProperty(ProfileSettingsEnum.format)).isEqualTo(FormatOptions.DIR.toString());
	}

	@Test
	@DisplayName("should handle multiple property changes")
	void shouldHandleMultiplePropertyChanges() {
		settings.setMergeMode(MergeOptions.SPLIT);
		settings.setImplicitMerge(true);
		settings.setHashCollisionMode(HashCollisionOptions.SINGLEFILE);

		assertThat(settings.getMergeMode()).isEqualTo(MergeOptions.SPLIT);
		assertThat(settings.getImplicitMerge()).isTrue();
		assertThat(settings.getHashCollisionMode()).isEqualTo(HashCollisionOptions.SINGLEFILE);

		settings.setMergeMode(MergeOptions.MERGE);
		settings.setImplicitMerge(false);
		settings.setHashCollisionMode(HashCollisionOptions.SINGLECLONE);

		assertThat(settings.getMergeMode()).isEqualTo(MergeOptions.MERGE);
		assertThat(settings.getImplicitMerge()).isFalse();
		assertThat(settings.getHashCollisionMode()).isEqualTo(HashCollisionOptions.SINGLECLONE);
	}

	@Test
	@DisplayName("should preserve properties independently")
	void shouldPreservePropertiesIndependently() {
		settings.setMergeMode(MergeOptions.SPLIT);
		settings.setImplicitMerge(true);

		settings.setMergeMode(null);
		assertThat(settings.getMergeMode()).isNull();
		assertThat(settings.getImplicitMerge()).isTrue();

		settings.setImplicitMerge(null);
		assertThat(settings.getMergeMode()).isNull();
		assertThat(settings.getImplicitMerge()).isNull();
	}

	@Test
	@DisplayName("should get properties with default values")
	void shouldGetPropertiesWithDefaultValues() {
		// Test boolean property with default
		Boolean boolValue = settings.getProperty(ProfileSettingsEnum.need_sha1_or_md5, Boolean.class);
		assertThat(boolValue).isFalse(); // default is false

		// Test string property with default
		String stringValue = settings.getProperty(ProfileSettingsEnum.format, String.class);
		assertThat(stringValue).isNotNull();
		
		// Test another boolean property with default
		Boolean parallelism = settings.getProperty(ProfileSettingsEnum.use_parallelism, Boolean.class);
		assertThat(parallelism).isTrue(); // default is true
	}

	@Test
	@DisplayName("should get enum property")
	void shouldGetEnumProperty() {
		settings.setProperty(ProfileSettingsEnum.format, FormatOptions.SEVENZIP.toString());
		
		FormatOptions format = settings.getEnumProperty(ProfileSettingsEnum.format, FormatOptions.class);
		assertThat(format).isEqualTo(FormatOptions.SEVENZIP);
	}

	@Test
	@DisplayName("should set enum property")
	void shouldSetEnumProperty() {
		settings.setEnumProperty(ProfileSettingsEnum.merge_mode, MergeOptions.FULLMERGE);
		
		assertThat(settings.getMergeMode()).isEqualTo(MergeOptions.FULLMERGE);
		assertThat(settings.getProperty(ProfileSettingsEnum.merge_mode)).isEqualTo(MergeOptions.FULLMERGE.toString());
	}

	@Test
	@DisplayName("should check if property exists")
	void shouldCheckIfPropertyExists() {
		assertThat(settings.hasProperty(ProfileSettingsEnum.format)).isFalse();
		
		settings.setProperty(ProfileSettingsEnum.format, FormatOptions.ZIP.toString());
		assertThat(settings.hasProperty(ProfileSettingsEnum.format)).isTrue();
	}

	@Test
	@DisplayName("should remove property when setting null")
	void shouldRemovePropertyWhenSettingNull() {
		settings.setProperty(ProfileSettingsEnum.format, FormatOptions.ZIP.toString());
		assertThat(settings.hasProperty(ProfileSettingsEnum.format)).isTrue();
		
		settings.setProperty(ProfileSettingsEnum.format, (String) null);
		assertThat(settings.hasProperty(ProfileSettingsEnum.format)).isFalse();
	}

	@Test
	@DisplayName("should save and load settings from file")
	void shouldSaveAndLoadSettingsFromFile(@TempDir File tempDir) {
		// Arrange
		File settingsFile = new File(tempDir, "settings.xml");
		settings.setProperty(ProfileSettingsEnum.format, FormatOptions.SEVENZIP.toString());
		settings.setProperty(ProfileSettingsEnum.merge_mode, MergeOptions.FULLMERGE.toString());
		settings.setProperty(ProfileSettingsEnum.need_sha1_or_md5, true);
		settings.setProperty(ProfileSettingsEnum.implicit_merge, true);
		
		// Act - Save settings
		settings.saveSettings(settingsFile);
		assertThat(settingsFile).exists();
		
		// Create new settings instance and load
		ProfileSettings loadedSettings = new ProfileSettings();
		loadedSettings.loadSettings(settingsFile);
		
		// Assert - verify raw properties are loaded correctly
		assertThat(loadedSettings.getProperty(ProfileSettingsEnum.format)).isEqualTo(FormatOptions.SEVENZIP.toString());
		assertThat(loadedSettings.getProperty(ProfileSettingsEnum.merge_mode)).isEqualTo(MergeOptions.FULLMERGE.toString());
		assertThat(loadedSettings.getProperty(ProfileSettingsEnum.need_sha1_or_md5, Boolean.class)).isTrue();
		assertThat(loadedSettings.getProperty(ProfileSettingsEnum.implicit_merge, Boolean.class)).isTrue();
	}

	@Test
	@DisplayName("should handle loading from non-existent file")
	void shouldHandleLoadingFromNonExistentFile(@TempDir File tempDir) {
		File nonExistentFile = new File(tempDir, "nonexistent.xml");
		
		// Should not throw exception
		settings.loadSettings(nonExistentFile);
		
		// Settings should remain empty
		assertThat(settings.getProperties()).isEmpty();
	}

	@Test
	@DisplayName("should export settings to JSON")
	void shouldExportSettingsToJson() {
		// Arrange
		settings.setProperty(ProfileSettingsEnum.format, FormatOptions.ZIP.toString());
		settings.setProperty(ProfileSettingsEnum.merge_mode, MergeOptions.NOMERGE.toString());
		settings.setProperty(ProfileSettingsEnum.need_sha1_or_md5, true);
		settings.setProperty(ProfileSettingsEnum.create_mode, false);
		
		// Act
		JsonObject json = settings.asJSO();
		
		// Assert
		assertThat(json).isNotNull();
		assertThat(json.get("format").asString()).isEqualTo(FormatOptions.ZIP.toString());
		assertThat(json.get("merge_mode").asString()).isEqualTo(MergeOptions.NOMERGE.toString());
		assertThat(json.get("need_sha1_or_md5").asBoolean()).isTrue();
		assertThat(json.get("create_mode").asBoolean()).isFalse();
	}

	@Test
	@DisplayName("should export JSON with various property types")
	void shouldExportJsonWithVariousPropertyTypes() {
		// Arrange
		settings.setProperty(ProfileSettingsEnum.format, FormatOptions.ZIP.toString());
		settings.setProperty(ProfileSettingsEnum.merge_mode, MergeOptions.NOMERGE.toString());
		settings.setProperty(ProfileSettingsEnum.need_sha1_or_md5, true);
		settings.setProperty(ProfileSettingsEnum.create_mode, false);
		settings.setProperty(ProfileSettingsEnum.roms_dest_dir, "/path/to/roms");
		
		// Act
		JsonObject json = settings.asJSO();
		
		// Assert
		assertThat(json).isNotNull();
		assertThat(json.get("format")).isNotNull();
		assertThat(json.get("merge_mode")).isNotNull();
		assertThat(json.get("need_sha1_or_md5")).isNotNull();
		assertThat(json.get("roms_dest_dir")).isNotNull();
	}

	@Test
	@DisplayName("should get properties backing store")
	void shouldGetPropertiesBackingStore() {
		assertThat(settings.getProperties()).isNotNull();
		assertThat(settings.getProperties()).isEmpty();
		
		settings.setProperty(ProfileSettingsEnum.format, FormatOptions.ZIP.toString());
		assertThat(settings.getProperties()).hasSize(1);
		assertThat(settings.getProperties().getProperty("format")).isEqualTo(FormatOptions.ZIP.toString());
	}

	@Test
	@DisplayName("should handle all format options")
	void shouldHandleAllFormatOptions() {
		for (FormatOptions format : FormatOptions.values()) {
			settings.setProperty(ProfileSettingsEnum.format, format.toString());
			assertThat(settings.getProperty(ProfileSettingsEnum.format)).isEqualTo(format.toString());
		}
	}

	@Test
	@DisplayName("should handle all merge options")
	void shouldHandleAllMergeOptions() {
		for (MergeOptions mergeMode : MergeOptions.values()) {
			settings.setMergeMode(mergeMode);
			assertThat(settings.getMergeMode()).isEqualTo(mergeMode);
		}
	}

	@Test
	@DisplayName("should handle all hash collision options")
	void shouldHandleAllHashCollisionOptions() {
		for (HashCollisionOptions mode : HashCollisionOptions.values()) {
			settings.setHashCollisionMode(mode);
			assertThat(settings.getHashCollisionMode()).isEqualTo(mode);
		}
	}

	@Test
	@DisplayName("should set and get directory properties")
	void shouldSetAndGetDirectoryProperties() {
		String romsDir = "/path/to/roms";
		String swromsDir = "/path/to/swroms";
		String disksDir = "/path/to/disks";
		
		settings.setProperty(ProfileSettingsEnum.roms_dest_dir, romsDir);
		settings.setProperty(ProfileSettingsEnum.swroms_dest_dir, swromsDir);
		settings.setProperty(ProfileSettingsEnum.disks_dest_dir, disksDir);
		
		assertThat(settings.getProperty(ProfileSettingsEnum.roms_dest_dir)).isEqualTo(romsDir);
		assertThat(settings.getProperty(ProfileSettingsEnum.swroms_dest_dir)).isEqualTo(swromsDir);
		assertThat(settings.getProperty(ProfileSettingsEnum.disks_dest_dir)).isEqualTo(disksDir);
	}

	@Test
	@DisplayName("should set and get directory enabled flags")
	void shouldSetAndGetDirectoryEnabledFlags() {
		settings.setProperty(ProfileSettingsEnum.swroms_dest_dir_enabled, true);
		settings.setProperty(ProfileSettingsEnum.disks_dest_dir_enabled, true);
		settings.setProperty(ProfileSettingsEnum.samples_dest_dir_enabled, true);
		settings.setProperty(ProfileSettingsEnum.backup_dest_dir_enabled, true);
		
		assertThat(settings.getProperty(ProfileSettingsEnum.swroms_dest_dir_enabled, Boolean.class)).isTrue();
		assertThat(settings.getProperty(ProfileSettingsEnum.disks_dest_dir_enabled, Boolean.class)).isTrue();
		assertThat(settings.getProperty(ProfileSettingsEnum.samples_dest_dir_enabled, Boolean.class)).isTrue();
		assertThat(settings.getProperty(ProfileSettingsEnum.backup_dest_dir_enabled, Boolean.class)).isTrue();
	}

	@Test
	@DisplayName("should handle source directory with multiple paths")
	void shouldHandleSourceDirectoryWithMultiplePaths() {
		String srcDir = "/path/one|/path/two|/path/three";
		settings.setProperty(ProfileSettingsEnum.src_dir, srcDir);
		
		assertThat(settings.getProperty(ProfileSettingsEnum.src_dir)).isEqualTo(srcDir);
	}

	@Test
	@DisplayName("should handle boolean property defaults")
	void shouldHandleBooleanPropertyDefaults() {
		// Test various boolean properties with their defaults
		assertThat(settings.getProperty(ProfileSettingsEnum.use_parallelism, Boolean.class)).isTrue();
		assertThat(settings.getProperty(ProfileSettingsEnum.create_mode, Boolean.class)).isTrue();
		assertThat(settings.getProperty(ProfileSettingsEnum.createfull_mode, Boolean.class)).isFalse();
		assertThat(settings.getProperty(ProfileSettingsEnum.ignore_unneeded_containers, Boolean.class)).isFalse();
		assertThat(settings.getProperty(ProfileSettingsEnum.ignore_unneeded_entries, Boolean.class)).isFalse();
		assertThat(settings.getProperty(ProfileSettingsEnum.ignore_unknown_containers, Boolean.class)).isFalse();
		assertThat(settings.getProperty(ProfileSettingsEnum.backup, Boolean.class)).isFalse();
	}
}
