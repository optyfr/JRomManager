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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jrm.aui.progress.ProgressHandler;
import jrm.profile.data.Disk;
import jrm.profile.data.Machine;
import jrm.profile.data.Rom;
import jrm.profile.data.Slot;
import jrm.profile.data.SlotOption;
import jrm.profile.manager.ProfileNFO;
import jrm.security.Session;

/**
 * Comprehensive tests for Profile XML parsing logic using synthetic XML fixtures.
 * 
 * <p>These tests validate parser paths not covered by real MAME DAT fixtures, including
 * disk elements, slot configurations, sample definitions, and various ROM attributes.</p>
 *
 * @author optyfr
 * @see Profile
 * @see ProfileParserTest
 */
@DisplayName("Profile Parser Synthetic Tests")
class ProfileParserSyntheticTest {

    /** Mocked session object providing user context and message bundles. */
    private Session session;
    /** Mocked progress handler that never cancels and passes through input streams. */
    private ProgressHandler handler;
    /** Temporary directory for synthetic XML test files, automatically cleaned up after each test. */
    @TempDir
    Path tempDir;

    /**
     * Initializes mocked dependencies before each test.
     *
     * <p>The session mock returns null for user (no authentication required in tests).
     * The progress handler mock never cancels and passes input streams through unchanged.</p>
     */
    @BeforeEach
    void setUp() {
        session = mock(Session.class, withSettings().stubOnly());
        when(session.getUser()).thenReturn(null);
        
        handler = mock(ProgressHandler.class, withSettings().stubOnly());
        when(handler.isCancel()).thenReturn(false);
        when(handler.getInputStream(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * Tests verifying parsing of disk elements including SHA1 and MD5 checksums,
     * optional and writeable flags, merge attributes, index attributes, and filename preservation.
     */
    @Nested
    @DisplayName("Disk Parsing")
    class DiskParsingTests {

        /**
         * Verifies that a disk element with a SHA1 checksum is parsed correctly.
         */
        @Test
        @DisplayName("Should parse disk with SHA1 checksum")
        void shouldParseDiskWithSha1Checksum() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                        <description>Test DAT with disks</description>
                        <version>1.0</version>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <year>2024</year>
                        <manufacturer>Test Manufacturer</manufacturer>
                        <disk name="test_disk" sha1="0123456789abcdef0123456789abcdef01234567" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            assertThat(profile.getDisksCnt()).isEqualTo(1);
            assertThat(profile.isSha1Disks()).isTrue();
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            assertThat(machine).isNotNull();
            assertThat(machine.getDisks()).hasSize(1);
            
            Disk disk = machine.getDisks().iterator().next();
            assertThat(disk.getBaseName()).isEqualTo("test_disk");
            assertThat(disk.getSha1()).isEqualTo("0123456789abcdef0123456789abcdef01234567");
        }

        /**
         * Verifies that a disk element with an MD5 checksum is parsed correctly.
         */
        @Test
        @DisplayName("Should parse disk with MD5 checksum")
        void shouldParseDiskWithMd5Checksum() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <disk name="test_disk" md5="0123456789abcdef0123456789abcdef" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            assertThat(profile).isNotNull();
            assertThat(profile.getDisksCnt()).isEqualTo(1);
            assertThat(profile.isMd5Disks()).isTrue();
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            Disk disk = machine.getDisks().iterator().next();
            assertThat(disk.getMd5()).isEqualTo("0123456789abcdef0123456789abcdef");
        }

        /**
         * Verifies that a disk element with the optional flag set is parsed correctly.
         */
        @Test
        @DisplayName("Should parse disk with optional flag")
        void shouldParseDiskWithOptionalFlag() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <disk name="optional_disk" sha1="0123456789abcdef0123456789abcdef01234567" optional="yes" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            Disk disk = machine.getDisks().iterator().next();
            assertThat(disk.isOptional()).isTrue();
        }

        /**
         * Verifies that a disk element with the writeable flag set is parsed correctly.
         */
        @Test
        @DisplayName("Should parse disk with writeable flag")
        void shouldParseDiskWithWriteableFlag() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <disk name="writable_disk" sha1="0123456789abcdef0123456789abcdef01234567" writeable="yes" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            Disk disk = machine.getDisks().iterator().next();
            assertThat(disk.isWriteable()).isTrue();
        }

        /**
         * Verifies that a disk element with a merge attribute is parsed correctly.
         */
        @Test
        @DisplayName("Should parse disk with merge attribute")
        void shouldParseDiskWithMergeAttribute() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="clone_machine" cloneof="parent_machine">
                        <description>Clone Machine</description>
                        <disk name="merged_disk" merge="parent_disk" sha1="0123456789abcdef0123456789abcdef01234567" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("clone_machine");
            Disk disk = machine.getDisks().iterator().next();
            assertThat(disk.getMerge()).isEqualTo("parent_disk");
        }

        /**
         * Verifies that a disk element with an index attribute is parsed correctly.
         */
        @Test
        @DisplayName("Should parse disk with index attribute")
        void shouldParseDiskWithIndexAttribute() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <disk name="indexed_disk" sha1="0123456789abcdef0123456789abcdef01234567" index="1" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            Disk disk = machine.getDisks().iterator().next();
            assertThat(disk.getIndex()).isEqualTo(1);
        }

        /**
         * Verifies that the .chd extension is preserved in the disk name attribute.
         */
        @Test
        @DisplayName("Should preserve .chd extension in disk name")
        void shouldPreserveChdExtensionInDiskName() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <disk name="test_disk.chd" sha1="0123456789abcdef0123456789abcdef01234567" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            Disk disk = machine.getDisks().iterator().next();
            // Disk name should be preserved as-is from XML
            assertThat(disk.getName()).isEqualTo("test_disk.chd");
        }
    }

    /**
     * Tests verifying parsing of extended ROM attributes including optional flag, merge attribute,
     * MD5 checksum, bios attribute, region attribute, date attribute, and dump status.
     */
    @Nested
    @DisplayName("ROM Extended Attributes")
    class RomExtendedAttributesTests {

        /**
         * Verifies that a ROM element with the optional flag set is parsed correctly.
         */
        @Test
        @DisplayName("Should parse ROM with optional flag")
        void shouldParseRomWithOptionalFlag() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <rom name="optional_rom" size="1024" crc="12345678" optional="yes" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            Rom rom = machine.getRoms().iterator().next();
            assertThat(rom.isOptional()).isTrue();
        }

        /**
         * Verifies that a ROM element with a merge attribute is parsed correctly.
         */
        @Test
        @DisplayName("Should parse ROM with merge attribute")
        void shouldParseRomWithMergeAttribute() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="clone_machine" cloneof="parent_machine">
                        <description>Clone Machine</description>
                        <rom name="merged_rom" size="1024" crc="12345678" merge="parent_rom" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("clone_machine");
            Rom rom = machine.getRoms().iterator().next();
            assertThat(rom.getMerge()).isEqualTo("parent_rom");
        }

        /**
         * Verifies that a ROM element with an MD5 checksum is parsed correctly.
         */
        @Test
        @DisplayName("Should parse ROM with MD5 checksum")
        void shouldParseRomWithMd5Checksum() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <rom name="md5_rom" size="1024" crc="12345678" md5="0123456789abcdef0123456789abcdef" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            assertThat(profile.isMd5Roms()).isTrue();
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            Rom rom = machine.getRoms().iterator().next();
            assertThat(rom.getMd5()).isEqualTo("0123456789abcdef0123456789abcdef");
        }

        /**
         * Verifies that a ROM element with a bios attribute is parsed correctly.
         */
        @Test
        @DisplayName("Should parse ROM with bios attribute")
        void shouldParseRomWithBiosAttribute() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <rom name="bios_rom" size="1024" crc="12345678" bios="default" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            Rom rom = machine.getRoms().iterator().next();
            assertThat(rom.getBios()).isEqualTo("default");
        }

        /**
         * Verifies that a ROM element with a region attribute is parsed correctly.
         */
        @Test
        @DisplayName("Should parse ROM with region attribute")
        void shouldParseRomWithRegionAttribute() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <rom name="region_rom" size="1024" crc="12345678" region="maincpu" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            Rom rom = machine.getRoms().iterator().next();
            assertThat(rom.getRegion()).isEqualTo("maincpu");
        }

        /**
         * Verifies that a ROM element with a date attribute is parsed correctly.
         */
        @Test
        @DisplayName("Should parse ROM with date attribute")
        void shouldParseRomWithDateAttribute() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <rom name="dated_rom" size="1024" crc="12345678" date="20240101" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            Rom rom = machine.getRoms().iterator().next();
            assertThat(rom.getDate()).isEqualTo("20240101");
        }

        /**
         * Verifies that a ROM element with a status attribute is parsed correctly.
         */
        @Test
        @DisplayName("Should parse ROM with status attribute")
        void shouldParseRomWithStatusAttribute() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <rom name="baddump_rom" size="1024" crc="12345678" status="baddump" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            Rom rom = machine.getRoms().iterator().next();
            assertThat(rom.getDumpStatus()).isEqualTo(Rom.Status.baddump);
        }
    }

    /**
     * Tests verifying parsing of slot elements and slot options including default flags,
     * device names, and multiple slots per machine.
     */
    @Nested
    @DisplayName("Slot Parsing")
    class SlotParsingTests {

        /**
         * Verifies that slot elements with multiple slot options are parsed correctly.
         */
        @Test
        @DisplayName("Should parse slot with slot options")
        void shouldParseSlotWithSlotOptions() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <slot name="cartslot">
                            <slotoption name="cart1" devname="a2600_cart" default="yes" />
                            <slotoption name="cart2" devname="a2600_cart" />
                        </slot>
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            assertThat(machine.getSlots()).containsKey("cartslot");
            
            Slot slot = machine.getSlots().get("cartslot");
            assertThat(slot).isNotNull();
            assertThat(slot.getName()).isEqualTo("cartslot");
            assertThat(slot).hasSize(2);
            
            SlotOption option1 = slot.get(0);
            assertThat(option1.getName()).isEqualTo("cart1");
            assertThat(option1.getDevName()).isEqualTo("a2600_cart");
            assertThat(option1.isDef()).isTrue();
            
            SlotOption option2 = slot.get(1);
            assertThat(option2.getName()).isEqualTo("cart2");
            assertThat(option2.isDef()).isFalse();
        }

        /**
         * Verifies that machines with multiple slot elements are parsed correctly.
         */
        @Test
        @DisplayName("Should parse multiple slots")
        void shouldParseMultipleSlots() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <slot name="slot1">
                            <slotoption name="opt1" devname="device1" />
                        </slot>
                        <slot name="slot2">
                            <slotoption name="opt2" devname="device2" />
                        </slot>
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            assertThat(machine.getSlots()).hasSize(2);
            assertThat(machine.getSlots()).containsKeys("slot1", "slot2");
        }
    }

    /**
     * Tests verifying parsing of clone relationships including cloneof, romof, and sampleof
     * attributes, as well as suspicious CRC detection when SHA1 values differ.
     */
    @Nested
    @DisplayName("Clone and Rom Relationships")
    class CloneAndRomRelationshipsTests {

        /**
         * Verifies that clone relationships via the cloneof attribute are parsed correctly.
         */
        @Test
        @DisplayName("Should parse machine with cloneof attribute")
        void shouldParseMachineWithCloneofAttribute() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="parent_machine">
                        <description>Parent Machine</description>
                        <rom name="rom1" size="1024" crc="12345678" />
                    </machine>
                    <machine name="clone_machine" cloneof="parent_machine">
                        <description>Clone Machine</description>
                        <rom name="rom2" size="2048" crc="abcdef12" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine parent = profile.getMachineListList().get(0).getByName("parent_machine");
            assertThat(parent).isNotNull();
            
            Machine clone = profile.getMachineListList().get(0).getByName("clone_machine");
            assertThat(clone).isNotNull();
            assertThat(clone.getCloneof()).isEqualTo("parent_machine");
        }

        /**
         * Verifies that ROM parent references via the romof attribute are parsed correctly.
         */
        @Test
        @DisplayName("Should parse machine with romof attribute")
        void shouldParseMachineWithRomofAttribute() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="parent_machine">
                        <description>Parent Machine</description>
                        <rom name="rom1" size="1024" crc="12345678" />
                    </machine>
                    <machine name="clone_machine" cloneof="parent_machine" romof="parent_machine">
                        <description>Clone Machine</description>
                        <rom name="rom2" size="2048" crc="abcdef12" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine clone = profile.getMachineListList().get(0).getByName("clone_machine");
            assertThat(clone.getRomof()).isEqualTo("parent_machine");
        }

        /**
         * Verifies that sample set references via the sampleof attribute are parsed correctly.
         */
        @Test
        @DisplayName("Should parse machine with sampleof attribute")
        void shouldParseMachineWithSampleofAttribute() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine" sampleof="test_samples">
                        <description>Test Machine</description>
                        <rom name="rom1" size="1024" crc="12345678" />
                        <sample name="sample1" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            assertThat(machine.getSampleof()).isEqualTo("test_samples");
            assertThat(profile.getSamplesCnt()).isEqualTo(1);
        }

        /**
         * Verifies that suspicious CRC detection flags ROMs with matching CRC but different SHA1.
         */
        @Test
        @DisplayName("Should detect suspicious CRC with different SHA1 values")
        void shouldDetectSuspiciousCrcWithDifferentSha1Values() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="machine1">
                        <description>Machine 1</description>
                        <rom name="rom1" size="1024" crc="12345678" sha1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" />
                    </machine>
                    <machine name="machine2">
                        <description>Machine 2</description>
                        <rom name="rom2" size="2048" crc="12345678" sha1="bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            assertThat(profile.getSuspiciousCRC()).contains("12345678");
        }
    }

    /**
     * Tests verifying parsing of device_ref elements that reference external device definitions.
     */
    @Nested
    @DisplayName("Device References")
    class DeviceReferencesTests {

        /**
         * Verifies that device_ref elements are parsed and stored correctly.
         */
        @Test
        @DisplayName("Should parse device_ref elements")
        void shouldParseDeviceRefElements() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <device_ref name="device1" />
                        <device_ref name="device2" />
                        <rom name="rom1" size="1024" crc="12345678" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            assertThat(machine.getDeviceRef()).containsExactly("device1", "device2");
        }
    }

    /**
     * Tests verifying parsing of input configuration elements including player count,
     * coin count, service mode, and tilt switch attributes.
     */
    @Nested
    @DisplayName("Input Configuration")
    class InputConfigurationTests {

        /**
         * Verifies that input configuration elements with players, coins, service, and tilt are parsed correctly.
         */
        @Test
        @DisplayName("Should parse input element with players and coins")
        void shouldParseInputElementWithPlayersAndCoins() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <input players="2" coins="2" service="yes" tilt="yes" />
                        <rom name="rom1" size="1024" crc="12345678" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            // Input class has protected fields with no getters, use reflection
            assertThat(getInputField(machine.input, "players")).isEqualTo(2);
            assertThat(getInputField(machine.input, "coins")).isEqualTo(2);
            assertThat(getInputBooleanField(machine.input, "service")).isTrue();
            assertThat(getInputBooleanField(machine.input, "tilt")).isTrue();
        }
    }

    /**
     * Tests verifying parsing of driver configuration elements including status, emulation,
     * cocktail, and savestate attributes.
     */
    @Nested
    @DisplayName("Driver Configuration")
    class DriverConfigurationTests {

        /**
         * Verifies that driver configuration elements with status, emulation, cocktail, and savestate are parsed correctly.
         */
        @Test
        @DisplayName("Should parse driver element with status and emulation")
        void shouldParseDriverElementWithStatusAndEmulation() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="test_machine">
                        <description>Test Machine</description>
                        <driver status="good" emulation="good" cocktail="preliminary" savestate="supported" />
                        <rom name="rom1" size="1024" crc="12345678" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine machine = profile.getMachineListList().get(0).getByName("test_machine");
            // Driver getters return enum types (StatusType/SaveStateType)
            assertThat(machine.driver.getStatus().name()).isEqualTo("good");
            assertThat(machine.driver.getEmulation().name()).isEqualTo("good");
            assertThat(machine.driver.getCocktail().name()).isEqualTo("preliminary");
            assertThat(machine.driver.getSaveState().name()).isEqualTo("supported");
        }
    }

    /**
     * Tests verifying parsing of display configuration elements including rotation attributes
     * and derived orientation (horizontal vs. vertical).
     */
    @Nested
    @DisplayName("Display Configuration")
    class DisplayConfigurationTests {

        /**
         * Verifies that display elements with rotation attributes are parsed and orientation is derived correctly.
         */
        @Test
        @DisplayName("Should parse display element with rotate attribute")
        void shouldParseDisplayElementWithRotateAttribute() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="horizontal_machine">
                        <description>Horizontal Machine</description>
                        <display rotate="0" />
                        <rom name="rom1" size="1024" crc="12345678" />
                    </machine>
                    <machine name="vertical_machine">
                        <description>Vertical Machine</description>
                        <display rotate="90" />
                        <rom name="rom2" size="2048" crc="abcdef12" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine horizontal = profile.getMachineListList().get(0).getByName("horizontal_machine");
            assertThat(horizontal.getOrientation()).isEqualTo(Machine.DisplayOrientation.horizontal);
            
            Machine vertical = profile.getMachineListList().get(0).getByName("vertical_machine");
            assertThat(vertical.getOrientation()).isEqualTo(Machine.DisplayOrientation.vertical);
        }
    }

    /**
     * Tests verifying parsing of cabinet type from dipswitch configurations, including
     * detection of "any" cabinet (upright + cocktail) vs. "cocktail" only.
     */
    @Nested
    @DisplayName("Cabinet Type")
    class CabinetTypeTests {

        /**
         * Verifies that cabinet types are derived from dipswitch configurations with upright and cocktail values.
         */
        @Test
        @DisplayName("Should parse cabinet dipswitch with upright and cocktail")
        void shouldParseCabinetDipswitchWithUprightAndCocktail() throws IOException {
            String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <datafile>
                    <header>
                        <name>Test DAT</name>
                    </header>
                    <machine name="any_cabinet">
                        <description>Any Cabinet</description>
                        <dipswitch name="cabinet">
                            <dipvalue name="upright" />
                            <dipvalue name="cocktail" />
                        </dipswitch>
                        <rom name="rom1" size="1024" crc="12345678" />
                    </machine>
                    <machine name="cocktail_only">
                        <description>Cocktail Only</description>
                        <dipswitch name="cabinet">
                            <dipvalue name="cocktail" />
                        </dipswitch>
                        <rom name="rom2" size="2048" crc="abcdef12" />
                    </machine>
                </datafile>
                """;
            
            File datFile = createTempDatFile(xml);
            Profile profile = loadProfile(datFile);
            
            Machine anyCabinet = profile.getMachineListList().get(0).getByName("any_cabinet");
            assertThat(anyCabinet.getCabinetType()).isEqualTo(Machine.CabinetType.any);
            
            Machine cocktailOnly = profile.getMachineListList().get(0).getByName("cocktail_only");
            assertThat(cocktailOnly.getCabinetType()).isEqualTo(Machine.CabinetType.cocktail);
        }
    }

    /**
     * Creates a temporary DAT file with the specified XML content.
     *
     * @param xml the XML content to write to the file
     * @return the created temporary File
     * @throws IOException if the file cannot be written
     */
    private File createTempDatFile(String xml) throws IOException {
        Path datPath = tempDir.resolve("test_dat.xml");
        Files.writeString(datPath, xml);
        return datPath.toFile();
    }

    /**
     * Retrieves an integer field value from an Input object using reflection.
     *
     * <p>The Input class has protected fields with no public getters, so reflection
     * is used to access field values for testing.</p>
     *
     * @param input     the Input object to inspect
     * @param fieldName the name of the field to retrieve
     * @return the integer value of the field
     * @throws RuntimeException if the field cannot be accessed
     */
    private int getInputField(jrm.profile.data.Input input, String fieldName) {
        try {
            java.lang.reflect.Field field = jrm.profile.data.Input.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Input field: " + fieldName, e);
        }
    }

    /**
     * Retrieves a boolean field value from an Input object using reflection.
     *
     * <p>The Input class has protected fields with no public getters, so reflection
     * is used to access field values for testing.</p>
     *
     * @param input     the Input object to inspect
     * @param fieldName the name of the field to retrieve
     * @return the boolean value of the field
     * @throws RuntimeException if the field cannot be accessed
     */
    private boolean getInputBooleanField(jrm.profile.data.Input input, String fieldName) {
        try {
            java.lang.reflect.Field field = jrm.profile.data.Input.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getBoolean(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Input field: " + fieldName, e);
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
     * @return the loaded Profile, or null if the internal load returned false
     * @throws RuntimeException if reflective access fails
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
            throw new RuntimeException("Failed to load profile", e);
        }
    }
}
