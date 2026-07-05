# Testing

## Unit Tests

Run all unit tests:

```bash
./gradlew test
```

Run tests for a specific module:

```bash
./gradlew :feature:preferences:test
```

## Screenshot Tests (Roborazzi)

Screenshot tests use [Roborazzi](https://github.com/takahirom/roborazzi) with Robolectric. Golden PNG files live in `src/test/screenshots/` inside each module and are committed to the repository.

### Run and verify against goldens

```bash
./gradlew :feature:preferences:verifyRoborazziDebug
```

Fails if the current render differs from the committed golden.

### Record new goldens

Run this after adding a new screenshot test or intentionally changing the UI:

```bash
./gradlew :feature:preferences:recordRoborazziDebug
```

Overwrites existing goldens and creates new ones. Commit the resulting PNGs.

To record a single test:

```bash
./gradlew :feature:preferences:recordRoborazziDebug --tests "*.PreferencesScreenTest.preferencesScreen_standardCurrency"
```

### Writing a screenshot test

```kotlin
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-normal-notlong-notround-port-notnight-mdpi-finger")
class MyScreenTest {

    @Test
    fun myScreen_someState() {
        captureRoboImage("src/test/screenshots/debts.my.package.MyScreenTest.myScreen_someState.png") {
            AppTheme {
                MyScreen(uiState = MyUiState(...), ...)
            }
        }
    }
}
```

> **Note:** Always pass the file path explicitly to `captureRoboImage`. The no-argument overload has a known issue with Roborazzi 1.7.0 where it captures too early and may miss text rendered from composable parameters.
