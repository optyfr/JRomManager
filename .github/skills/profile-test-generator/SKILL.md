---
description: "Multi-step workflow to analyze Profile parsing code, identify test scenarios from MAME DAT data, generate comprehensive JUnit 5 tests with coverage reporting, and validate test execution."
name: "Profile Test Generator"
argument-hint: "[Profile class/method to analyze; e.g., Profile.java, MachineList]"
---

# Profile Test Generator

A comprehensive workflow for generating production-quality tests for Profile parsing code using real MAME DAT fixtures.

## When to Use
- You need to add test coverage for Profile XML parsing logic
- You want to validate data model construction from DAT files
- You need to verify edge cases in machine/software/ROM relationships
- You're refactoring Profile code and need regression tests
- You want to ensure parsing correctness before deployment

## Inputs
- **Target code**: Profile classes or methods in `jrm.profile` package (e.g., `Profile.java`, `MachineList`, `SoftwareList`)
- **Test data**: MAME DAT XML files in `jrmcore/src/test/resources/dats/`
- **Context**: Existing test patterns in `jrmcore/src/test/java/jrm/`

## Workflow Steps

### Step 1: Analyze Target Code
**Goal**: Understand parsing logic, state management, and object relationships.

1. Read the target Java class(es) to identify:
   - Entry points (public methods like `loadDat()`, constructors)
   - XML parsing callbacks (`startElement()`, `endElement()`)
   - State transitions and flags
   - Object graph construction (parent-child relationships)
   - Edge cases handled (optional fields, null checks, validation)

2. Document key behaviors:
   - What XML elements are parsed?
   - What attributes are extracted?
   - How are cross-references resolved (cloneof, device_ref)?
   - What validation or error handling exists?

**Output**: Summary of parsing logic and testable behaviors.

### Step 2: Examine DAT Fixtures
**Goal**: Identify concrete test scenarios from real data.

1. List available DAT files:
   ```bash
   find jrmcore/src/test/resources/dats -name "*.xml" | head -20
   ```

2. For each DAT file, extract sample data:
   - Machine count and structure
   - Software list definitions
   - ROM/disk attributes (checksums, sizes, merge flags)
   - Clone relationships
   - Device definitions and slots
   - Optional/conditional fields

3. Identify edge cases in the data:
   - Machines with no ROMs
   - Software with multiple parts
   - CHD disk images
   - BIOS sets
   - Mechanical machines (`ismechanical="yes"`)

**Output**: List of test scenarios with concrete examples from DAT data.

### Step 3: Design Test Cases
**Goal**: Map behaviors and scenarios to JUnit 5 tests.

For each testable behavior from Step 1, create test cases:

**Happy Path Tests**
- Parse valid DAT → verify object graph construction
- Load machine with ROMs → validate checksums, sizes, names
- Load software list → verify parent/clone relationships
- Parse device definitions → check slot configurations

**Edge Case Tests**
- Machine with optional ROMs (`optional="yes"`)
- ROM with merge attribute (`merge="..."`)
- Software with multiple parts and data areas
- Missing attributes (year, manufacturer)
- Clone relationships (`cloneof="..."`)

**Error Condition Tests**
- Invalid XML structure
- Missing required attributes (name, size, crc)
- Malformed checksums (wrong length, invalid hex)
- Circular clone references

**Filtering Tests**
- Machine status filters (AnywareStatus)
- System boundaries (Systms)
- Year range filtering

**Test Structure**:
```java
@Nested
@DisplayName("Machine Parsing")
class MachineParsingTests {
    @Test
    @DisplayName("Should parse machine with ROMs and verify checksums")
    void shouldParseMachineWithRomsAndVerifyChecksums() { ... }
    
    @Test
    @DisplayName("Should handle machine with optional ROMs")
    void shouldHandleMachineWithOptionalRoms() { ... }
}
```

**Output**: Test case specification with method signatures and assertions.

### Step 4: Generate Test Code
**Goal**: Write production-quality JUnit 5 tests.

1. Create test class in `jrmcore/src/test/java/jrm/profile/`:
   ```java
   package jrm.profile;
   
   import static org.assertj.core.api.Assertions.assertThat;
   import org.junit.jupiter.api.*;
   
   class ProfileParserTest {
       // Test implementation
   }
   ```

2. Follow test-patterns.instructions.md conventions:
   - Arrange → Act → Assert structure
   - Fluent AssertJ assertions
   - Descriptive `@DisplayName` annotations
   - `@Nested` classes for grouping

3. Load DAT fixtures:
   ```java
   private Profile loadDat(String filename) throws Exception {
       Path datFile = Path.of("src/test/resources/dats", filename);
       return Profile.loadDat(datFile);
   }
   ```

4. Write comprehensive assertions:
   ```java
   assertThat(machine.getRoms())
       .isNotEmpty()
       .allSatisfy(rom -> {
           assertThat(rom.getName()).isNotBlank();
           assertThat(rom.getSize()).isGreaterThan(0);
           assertThat(rom.getCrc()).matches("^[0-9a-f]{8}$");
       });
   ```

**Output**: Complete test class with 10-20 test methods covering key scenarios.

### Step 5: Execute & Validate Tests
**Goal**: Ensure tests pass and provide meaningful coverage.

1. Compile and run tests:
   ```bash
   cd jrmcore
   ../gradlew test --tests "jrm.profile.ProfileParserTest"
   ```

2. Review test output:
   - All tests pass ✓
   - No false positives (tests that pass but don't validate anything)
   - Clear failure messages when assertions fail

3. Check coverage (optional):
   ```bash
   ../gradlew test jacocoTestReport
   open build/reports/jacoco/test/html/index.html
   ```

4. Refactor if needed:
   - Extract common setup into `@BeforeEach` or helper methods
   - Split large test methods into focused tests
   - Add `@ParameterizedTest` for repetitive scenarios

**Output**: Passing test suite with coverage report.

## Example Invocation

```
/profile-test-generator Profile.java
```

Or for a specific method:

```
/profile-test-generator Profile.loadDat()
```

Or for a data model class:

```
/profile-test-generator MachineList
```

## Deliverables

After running this skill, you will have:
1. **Test class** in `jrmcore/src/test/java/jrm/profile/` with 10-20 test methods
2. **Coverage** of happy paths, edge cases, and error conditions
3. **Assertions** validating object graph construction and relationships
4. **Documentation** via `@DisplayName` explaining test intent
5. **Passing tests** validated against real MAME DAT fixtures

## Constraints
- Do not modify production code in `jrm.profile` package
- Use only existing DAT fixtures or create minimal synthetic XML
- Follow existing test patterns in `jrmcore/src/test/java/jrm/`
- Avoid testing UI components or progress handlers (mock if needed)
- Keep tests independent and deterministic

## Related Files
- **Instructions**: `.github/instructions/test-patterns.instructions.md`
- **Prompt**: `.github/prompts/write-profile-tests.prompt.md`
- **DAT Fixtures**: `jrmcore/src/test/resources/dats/`
- **Existing Tests**: `jrmcore/src/test/java/jrm/io/torrent/TorrentParserTest.java`

## Next Steps
After generating tests:
1. Review generated tests for completeness
2. Add additional edge cases specific to your Profile changes
3. Run full test suite: `./gradlew test`
4. Check coverage: `./gradlew jacocoTestReport`
5. Commit tests with descriptive message
