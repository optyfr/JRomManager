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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
 */
@DisplayName("Profile Parser Synthetic Tests")
class ProfileParserSyntheticTest {

    private Session session;
    private ProgressHandler handler;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        session = mock(Session.class);
        when(session.getUser()).thenReturn(null);
        
        handler = mock(ProgressHandler.class);
        when(handler.isCancel()).thenReturn(false);
        when(handler.getInputStream(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("Disk Parsing")
    class DiskParsingTests {

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

    @Nested
    @DisplayName("ROM Extended Attributes")
    class RomExtendedAttributesTests {

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

    @Nested
    @DisplayName("Slot Parsing")
    class SlotParsingTests {

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

    @Nested
    @DisplayName("Clone and Rom Relationships")
    class CloneAndRomRelationshipsTests {

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

    @Nested
    @DisplayName("Device References")
    class DeviceReferencesTests {

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

    @Nested
    @DisplayName("Input Configuration")
    class InputConfigurationTests {

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

    @Nested
    @DisplayName("Driver Configuration")
    class DriverConfigurationTests {

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

    @Nested
    @DisplayName("Display Configuration")
    class DisplayConfigurationTests {

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

    @Nested
    @DisplayName("Cabinet Type")
    class CabinetTypeTests {

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
     * Helper method to create a temporary DAT file with the given XML content.
     * 
     * @param xml the XML content to write
     * @return the created File
     * @throws IOException if an I/O error occurs
     */
    private File createTempDatFile(String xml) throws IOException {
        Path datPath = tempDir.resolve("test_dat.xml");
        Files.writeString(datPath, xml);
        return datPath.toFile();
    }

    /**
     * Helper method to get an int field from Input class using reflection.
     * 
     * @param input the Input object
     * @param fieldName the field name
     * @return the field value
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
     * Helper method to get a boolean field from Input class using reflection.
     * 
     * @param input the Input object
     * @param fieldName the field name
     * @return the field value
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
     * Helper method to load a Profile from a DAT file with mocked dependencies.
     * Uses reflection to call internalLoad directly, bypassing cache and post-processing.
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
            throw new RuntimeException("Failed to load profile", e);
        }
    }
}
