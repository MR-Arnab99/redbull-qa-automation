# Red Bull QA Automation

Three focused tests for the Digital Poster Dashboard case study.

## Tools

- Java 21
- Maven
- Playwright Java
- JUnit 5
- Google Chrome installed locally

## Automated coverage

| Test | Coverage | Observed result                                                |
|---|---|----------------------------------------------------------------|
| Combined filters | Online + Up-To-Date + Landscape; checks 8 rows and their values | Passed before switching the browser tests to the demo account. |
| Empty metadata selection | Selects an IMEI, then (empty), and checks that the selection stays empty | Failed as expected, reproducing bug #12                        |
| Online device command | Signs in, verifies Online/Outdated, sends update, and verifies Up-To-Date through GET | Passed                                                         |

The empty-metadata test asserts the expected correct behavior. It remains failing while the application resets the selection to All.

## Test environment and credentials

Tests target:
https://qa-sample-arnab-mondal.up.railway.app/

They use the demo account displayed on the application's sign-in page:
qa.tester@example.com / Password123

User can sign up with personal account as well.

## Before running

The API test changes device 75997 from Outdated to Up-To-Date.

Before each API test run, reset the assessment environment using Postman:

POST https://qa-sample-arnab-mondal.up.railway.app/api/dev/reset

Use No Auth and no request body. Confirm 200 OK.

Resetting restores sample data and may remove created accounts or invalidate existing tokens. Save any evidence first. The API test signs in again automatically.

Do not reset while tests are running.

## Run in IntelliJ

1. Open this project and load its Maven dependencies.
2. Select JDK 21.
3. Run individual test methods using the green arrow beside them.

Classes:
- src/test/java/Arnab/DeviceFiltersTest.java
- src/test/java/Arnab/RedBullQaTest.java

## Run with Maven

Run one test:

```shell
mvn "-Dtest=DeviceFiltersTest#combinedFiltersReturnMatchingDevices" test
mvn "-Dtest=DeviceFiltersTest#emptyMetadataSelectionDoesNotResetToAll" test
mvn "-Dtest=RedBullQaTest#onlineDeviceChangesFromOutdatedToUpToDate" test
```

After resetting, run all three:

```shell
mvn test
```

While bug #12 exists, the full run is expected to report a failure and return a non-zero exit code.

Maven reports are written to target/surefire-reports.

## Limitations

- The empty-metadata test checks selection persistence, not complete metadata matching.
- The API test verifies status changes, not physical software installation.
- The reported software-version mismatch is documented in the manual findings.
- Browser tests use visible Chrome, slower actions, and short pauses for demonstration.
- These three tests are selected coverage, not a complete regression suite.
- Running requires access to the assessment environment.