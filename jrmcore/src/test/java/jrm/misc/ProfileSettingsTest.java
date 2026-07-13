package jrm.misc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
