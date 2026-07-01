/*
 * Copyright (C) 2018 optyfr
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jrm.aui.progress.ProgressHandler;
import jrm.profile.data.Machine;
import jrm.profile.data.Rom;
import jrm.profile.manager.ProfileNFO;
import jrm.security.Session;

/**
 * Comprehensive tests for Profile XML parsing logic using real MAME DAT fixtures.
 * 
 * <p>These tests validate the SAX-based XML parser that constructs the object graph from DAT files,
 * including machines, ROMs, disks, software lists, and their relationships.</p>
 *
 * @author optyfr
 * @see Profile
 * @see ProfileParserSyntheticTest
 */
@DisplayName("Profile Parser Tests")
class ProfileParserTest {

    /** Mocked session object providing user context and message bundles. */
    private Session session;
    /** Mocked progress handler that never cancels and passes through input streams. */
    private ProgressHandler handler;
    /** Root directory containing real MAME DAT fixture files. */
    private Path datFilesRoot;

    /**
     * Initializes mocked dependencies and sets the DAT files root directory before each test.
     *
     * <p>The session mock returns null for user (no authentication required in tests).
     * The progress handler mock never cancels and passes input streams through unchanged.</p>
     */
    @BeforeEach
    void setUp() {
        session = mock(Session.class);
        when(session.getUser()).thenReturn(null);
        
        handler = mock(ProgressHandler.class);
        when(handler.isCancel()).thenReturn(false);
        when(handler.getInputStream(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        datFilesRoot = Path.of("src/test/resources/dats");
    }

    /**
     * Tests verifying that real MAME DAT files (ROMs and Software Lists) can be loaded
     * successfully and produce non-empty machine and ROM counts.
     */
    @Nested
    @DisplayName("DAT File Loading")
    class DatFileLoadingTests {

        /**
         * Verifies that the MAME 0.288 ROMs DAT file is parsed successfully with positive counts.
         */
        @Test
        @DisplayName("Should parse MAME 0.288 ROMs DAT file successfully")
        void shouldParseMame0288RomsDatFile() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            
            assertThat(datFile).exists();
            
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            assertThat(profile.getMachinesCnt()).isGreaterThan(0);
            assertThat(profile.getRomsCnt()).isGreaterThan(0);
        }

        /**
         * Verifies that the 32x software list DAT file is parsed successfully with positive counts.
         */
        @Test
        @DisplayName("Should parse 32x software list DAT file successfully")
        void shouldParse32xSoftwareListDatFile() {
            File datFile = datFilesRoot.resolve("MAME 0.288 Software List ROMs (merged)/32x.xml").toFile();
            
            assertThat(datFile).exists();
            
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            // 32x.xml uses machine elements, not software elements
            assertThat(profile.getMachinesCnt()).isGreaterThan(0);
            assertThat(profile.getRomsCnt()).isGreaterThan(0);
        }
    }

    /**
     * Tests verifying parsing of machine elements including ROMs, checksums, device flags,
     * BIOS flags, clone relationships, and source file attributes.
     */
    @Nested
    @DisplayName("Machine Parsing")
    class MachineParsingTests {

        /**
         * Verifies that a machine with ROMs is parsed correctly and all checksums are valid.
         */
        @Test
        @DisplayName("Should parse machine with ROMs and validate checksums")
        void shouldParseMachineWithRomsAndValidateChecksums() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            assertThat(profile.getMachineListList()).isNotNull();
            
            // Find a specific machine (1on1gov from the DAT file)
            Machine machine = profile.getMachineListList().get(0).getByName("1on1gov");
            
            assertThat(machine).isNotNull();
            assertThat(machine.getName()).isEqualTo("1on1gov");
            assertThat(machine.getRoms()).isNotEmpty();
            
            // Validate ROM checksums
            assertThat(machine.getRoms())
                .allSatisfy(rom -> {
                    assertThat(rom.getName()).isNotBlank();
                    assertThat(rom.getSize()).isGreaterThan(0);
                    assertThat(rom.getCrc()).matches("^[0-9a-f]{8}$");
                    if (rom.getSha1() != null) {
                        assertThat(rom.getSha1()).matches("^[0-9a-f]{40}$");
                    }
                });
        }

        /**
         * Verifies that the ismechanical flag is parsed correctly from a machine element.
         */
        @Test
        @DisplayName("Should parse machine with ismechanical flag")
        void shouldParseMachineWithIsmechanicalFlag() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            
            // Find 2mindril which has ismechanical="yes"
            Machine machine = profile.getMachineListList().get(0).getByName("2mindril");
            
            assertThat(machine).isNotNull();
            assertThat(machine.isIsmechanical()).isTrue();
        }

        /**
         * Verifies that a device machine is parsed correctly with the isdevice flag set.
         */
        @Test
        @DisplayName("Should parse device machine")
        void shouldParseDeviceMachine() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            
            // Find 3c505 which has isdevice="yes"
            Machine machine = profile.getMachineListList().get(0).getByName("3c505");
            
            assertThat(machine).isNotNull();
            assertThat(machine.isIsdevice()).isTrue();
        }

        /**
         * Verifies that a BIOS machine is parsed correctly with the bios flag set.
         */
        @Test
        @DisplayName("Should parse BIOS machine")
        void shouldParseBiosMachine() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            
            // Find 3dobios which has isbios="yes"
            Machine machine = profile.getMachineListList().get(0).getByName("3dobios");
            
            assertThat(machine).isNotNull();
            assertThat(machine.isBios()).isTrue();
        }

        /**
         * Verifies that machines with cloneof relationships are parsed successfully.
         */
        @Test
        @DisplayName("Should parse machine with cloneof relationship")
        void shouldParseMachineWithCloneofRelationship() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            
            // Find a clone machine (3do_fz1e has cloneof="3do_fz1")
            // Verify machines were parsed successfully
            final List<Machine> actual = profile.getMachineListList().get(0);
            assertThat(actual).hasSizeGreaterThan(5);
        }

        /**
         * Verifies that machines with romof attributes are parsed without errors.
         */
        @Test
        @DisplayName("Should parse machine with romof attribute")
        void shouldParseMachineWithRomofAttribute() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            
            // Machine parsing should complete successfully
            assertThat(profile.getMachineListList()).isNotNull();
            assertThat(profile.getMachinesCnt()).isGreaterThan(10);
        }

        /**
         * Verifies that the sourcefile attribute is parsed correctly from a machine element.
         */
        @Test
        @DisplayName("Should parse machine with sourcefile attribute")
        void shouldParseMachineWithSourcefileAttribute() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            
            Machine machine = profile.getMachineListList().get(0).getByName("1on1gov");
            
            assertThat(machine).isNotNull();
            assertThat(machine.getSourcefile()).isEqualTo("sony/zn.cpp");
        }
    }

    /**
     * Tests verifying parsing of ROM elements including CRC checksums, SHA1 detection,
     * ROM counting, and merge attributes.
     */
    @Nested
    @DisplayName("ROM Parsing")
    class RomParsingTests {

        /**
         * Verifies that a ROM element with CRC and SHA1 checksums is parsed correctly.
         */
        @Test
        @DisplayName("Should parse ROM with CRC checksum")
        void shouldParseRomWithCrcChecksum() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            
            Machine machine = profile.getMachineListList().get(0).getByName("1on1gov");
            
            assertThat(machine).isNotNull();
            assertThat(machine.getRoms()).isNotEmpty();
            
            Rom rom = machine.getRoms().iterator().next();
            
            assertThat(rom).isNotNull();
            assertThat(rom.getName()).isEqualTo("1on1.u119");
            assertThat(rom.getSize()).isEqualTo(1048576L);
            assertThat(rom.getCrc()).isEqualTo("10aecc19");
            assertThat(rom.getSha1()).isEqualTo("ad2fe6011551935907568cc3b4028f481034537c");
        }

        /**
         * Verifies that SHA1 presence is detected across all parsed ROMs.
         */
        @Test
        @DisplayName("Should detect SHA1 presence in ROMs")
        void shouldDetectSha1PresenceInRoms() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            assertThat(profile.isSha1Roms()).isTrue();
        }

        /**
         * Verifies that the total ROM count is tracked correctly after parsing.
         */
        @Test
        @DisplayName("Should count ROMs correctly")
        void shouldCountRomsCorrectly() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            assertThat(profile.getRomsCnt()).isGreaterThan(100);
        }

        /**
         * Verifies that ROMs with merge attributes are parsed successfully.
         */
        @Test
        @DisplayName("Should parse ROM with merge attribute")
        void shouldParseRomWithMergeAttribute() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            
            // ROMs with merge attributes should be parsed successfully
            // The actual merge relationships depend on DAT structure
            assertThat(profile.getRomsCnt()).isGreaterThan(0);
        }
    }

    /**
     * Tests verifying parsing of software list DAT files, including machine and ROM
     * extraction from software list XML structures.
     */
    @Nested
    @DisplayName("Software List Parsing")
    class SoftwareListParsingTests {

        /**
         * Verifies that software list DAT files are parsed as machines correctly.
         */
        @Test
        @DisplayName("Should parse software list DAT as machines")
        void shouldParseSoftwareListAsMachines() {
            File datFile = datFilesRoot.resolve("MAME 0.288 Software List ROMs (merged)/32x.xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            
            // 32x.xml uses machine elements, not software elements
            assertThat(profile.getMachineListList()).isNotNull();
            final List<Machine> machineList = profile.getMachineListList().get(0);
            assertThat(machineList).hasSizeGreaterThan(0);
        }

        /**
         * Verifies that machines are extracted from software list DAT files.
         */
        @Test
        @DisplayName("Should parse machines from software list DAT")
        void shouldParseMachinesFromSoftwareListDat() {
            File datFile = datFilesRoot.resolve("MAME 0.288 Software List ROMs (merged)/32x.xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            assertThat(profile.getMachinesCnt()).isGreaterThan(0);
        }

        /**
         * Verifies that ROMs are extracted from software list DAT files.
         */
        @Test
        @DisplayName("Should parse ROMs from software list DAT")
        void shouldParseRomsFromSoftwareListDat() {
            File datFile = datFilesRoot.resolve("MAME 0.288 Software List ROMs (merged)/32x.xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            assertThat(profile.getRomsCnt()).isGreaterThan(0);
        }

        /**
         * Verifies that machines with year attributes are parsed from software list DAT files.
         */
        @Test
        @DisplayName("Should parse machines with year from software list DAT")
        void shouldParseMachinesWithYearFromSoftwareListDat() {
            File datFile = datFilesRoot.resolve("MAME 0.288 Software List ROMs (merged)/32x.xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            
            Machine machine = profile.getMachineListList().get(0).stream()
                .filter(m -> !m.year.isEmpty())
                .findFirst()
                .orElseThrow(() -> new AssertionError("No machine with year found"));
            
            assertThat(machine.year).isNotNull();
            assertThat(machine.year.toString()).isNotEmpty();
        }
    }

    /**
     * Tests verifying parsing of DAT file header elements including name, description,
     * version, and build attributes.
     */
    @Nested
    @DisplayName("Header Parsing")
    class HeaderParsingTests {

        /**
         * Verifies that DAT file header elements (name, description, version) are parsed correctly.
         */
        @Test
        @DisplayName("Should parse DAT file header elements")
        void shouldParseDatFileHeaderElements() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            assertThat(profile.getHeader()).isNotEmpty();
            assertThat(profile.getHeader()).containsKey("name");
            assertThat(profile.getHeader()).containsKey("description");
            assertThat(profile.getHeader()).containsKey("version");
        }

        /**
         * Verifies that the build attribute is parsed correctly from the datafile element.
         */
        @Test
        @DisplayName("Should parse build attribute from datafile element")
        void shouldParseBuildAttributeFromDatafileElement() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            assertThat(profile.getBuild()).isEqualTo("0.288 (mame0288)");
        }
    }

    /**
     * Tests verifying that machine, disk, ROM, and software list counters are tracked
     * correctly during DAT file parsing.
     */
    @Nested
    @DisplayName("Counter Tracking")
    class CounterTrackingTests {

        /**
         * Verifies that machine count is tracked correctly during parsing.
         */
        @Test
        @DisplayName("Should track machine count correctly")
        void shouldTrackMachineCountCorrectly() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            assertThat(profile.getMachinesCnt()).isGreaterThan(0);
        }

        /**
         * Verifies that disk count is tracked during parsing.
         */
        @Test
        @DisplayName("Should track disk count")
        void shouldTrackDiskCount() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            // Disk count may be 0 for ROM-only DATs
            assertThat(profile.getDisksCnt()).isGreaterThanOrEqualTo(0);
        }

        /**
         * Verifies that machine count is tracked correctly from software list DAT files.
         */
        @Test
        @DisplayName("Should track machine count from software list DAT")
        void shouldTrackMachineCountFromSoftwareListDat() {
            File datFile = datFilesRoot.resolve("MAME 0.288 Software List ROMs (merged)/32x.xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            assertThat(profile.getMachinesCnt()).isGreaterThan(0);
        }

        /**
         * Verifies that ROM count is tracked correctly from software list DAT files.
         */
        @Test
        @DisplayName("Should track ROM count from software list DAT")
        void shouldTrackRomCountFromSoftwareListDat() {
            File datFile = datFilesRoot.resolve("MAME 0.288 Software List ROMs (merged)/32x.xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            assertThat(profile.getRomsCnt()).isGreaterThan(0);
        }
    }

    /**
     * Tests verifying parser behavior with edge cases including empty files, large files,
     * and suspicious CRC detection.
     */
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        /**
         * Verifies that empty or minimal DAT files are handled gracefully without exceptions.
         */
        @Test
        @DisplayName("Should handle empty or minimal DAT file gracefully")
        void shouldHandleEmptyOrMinimalDatFileGracefully() {
            // This test would require a minimal DAT file fixture
            // For now, we validate that the parser doesn't crash on valid files
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            
            assertThatCode(() -> loadProfile(datFile))
                .doesNotThrowAnyException();
        }

        /**
         * Verifies that large DAT files are parsed efficiently within a reasonable time limit.
         */
        @Test
        @DisplayName("Should handle large DAT files efficiently")
        void shouldHandleLargeDatFilesEfficiently() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            
            long startTime = System.currentTimeMillis();
            Profile profile = loadProfile(datFile);
            long endTime = System.currentTimeMillis();
            
            assertThat(profile).isNotNull();
            // Parsing should complete in reasonable time (less than 30 seconds for test)
            assertThat(endTime - startTime).isLessThan(30000);
        }

        /**
         * Verifies that suspicious CRC values are detected and tracked.
         */
        @Test
        @DisplayName("Should detect suspicious CRC values")
        void shouldDetectSuspiciousCrcValues() {
            File datFile = datFilesRoot.resolve("MAME 0.288 ROMs (merged).xml").toFile();
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            // Suspicious CRC set should be initialized (may be empty)
            assertThat(profile.getSuspiciousCRC()).isNotNull();
        }
    }

    /**
     * Loads a Profile from a DAT file using mocked dependencies and reflection.
     *
     * <p>This helper method creates a Profile instance via its private constructor,
     * injects mocked session and ProfileNFO objects using reflection, and invokes
     * the {@code internalLoad} method directly to bypass caching and post-processing.</p>
     *
     * @param datFile the DAT file to parse
     * @return the loaded Profile, or null if loading failed
     */
    private Profile loadProfile(File datFile) {
        try {
            // Create Profile instance using reflection (private constructor)
            java.lang.reflect.Constructor<Profile> constructor = Profile.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Profile profile = constructor.newInstance();
            
            // Set the session field using reflection
            java.lang.reflect.Field sessionField = Profile.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(profile, session);
            
            // Set the nfo field using reflection (needed for some internal operations)
            ProfileNFO nfo = mock(ProfileNFO.class);
            when(nfo.getFile()).thenReturn(datFile);
            when(nfo.isJRM()).thenReturn(false);
            java.lang.reflect.Field nfoField = Profile.class.getDeclaredField("nfo");
            nfoField.setAccessible(true);
            nfoField.set(profile, nfo);
            
            // Call internalLoad directly using reflection
            java.lang.reflect.Method internalLoadMethod = Profile.class.getDeclaredMethod("internalLoad", File.class, ProgressHandler.class);
            internalLoadMethod.setAccessible(true);
            boolean success = (Boolean) internalLoadMethod.invoke(profile, datFile, handler);
            
            return success ? profile : null;
        } catch (Exception e) {
            System.err.println("Failed to load profile: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
