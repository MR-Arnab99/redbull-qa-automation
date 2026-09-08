package Arnab;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class DeviceFiltersTest {

    @Test
    void combinedFiltersReturnMatchingDevices() {

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions()
                             .setChannel("chrome")
                             .setHeadless(false)
                             .setSlowMo(800))){

            Page page = browser.newPage();

            // Open the website.
            page.navigate(
                    "https://qa-sample-arnab-mondal.up.railway.app/"
            );

            // Enter the test account details.
            page.getByTestId("signin-email-input")
                    .fill("qa.tester@example.com");

            page.getByTestId("signin-password-input")
                    .fill("Password123");


            page.getByTestId("signin-submit-button").click();

            // Check that the dashboard opened.
            assertThat(page.getByRole(
                    AriaRole.HEADING,
                    new Page.GetByRoleOptions()
                            .setName("Digital Poster Dashboard")
                            .setExact(true)
            )).isVisible();

            // Keep metadata filtering off.
            page.getByTestId("filter-metadata-key").selectOption("");

// Select the three filters.
            page.getByTestId("filter-presence-status").selectOption("online");

            page.getByTestId("filter-core-services-status")
                    .selectOption("up-to-date");

            page.getByTestId("filter-orientation").selectOption("landscape");

// Confirm the dropdown selections.
            assertThat(page.getByTestId("filter-presence-status"))
                    .hasValue("online");

            assertThat(page.getByTestId("filter-core-services-status"))
                    .hasValue("up-to-date");

            assertThat(page.getByTestId("filter-orientation"))
                    .hasValue("landscape");

// Wait until the table contains exactly 8 device rows.
            Locator rows = page.locator("table tbody tr");
            assertThat(rows).hasCount(8);

// Check that every device matches all three filters.
            for (int i = 0; i < 8; i++) {
                Locator cells = rows.nth(i).locator("td");

                assertThat(cells.nth(0)).hasText(
                        java.util.regex.Pattern.compile(
                                "^online$", java.util.regex.Pattern.CASE_INSENSITIVE)
                );

                assertThat(cells.nth(2)).hasText(
                        java.util.regex.Pattern.compile(
                                "^up-to-date$", java.util.regex.Pattern.CASE_INSENSITIVE)
                );

                assertThat(cells.nth(5)).hasText(
                        java.util.regex.Pattern.compile(
                                "^landscape$", java.util.regex.Pattern.CASE_INSENSITIVE)
                );
            }
            page.waitForTimeout(3000);
        }
    }
    @Test
    void emptyMetadataSelectionDoesNotResetToAll() {

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions()
                             .setChannel("chrome")
                             .setHeadless(false)
                             .setSlowMo(800))) {

            Page page = browser.newPage();

            page.navigate(
                    "https://qa-sample-arnab-mondal.up.railway.app/"
            );

            // Sign in.
            page.getByTestId("signin-email-input")
                    .fill("qa.tester@example.com");

            page.getByTestId("signin-password-input")
                    .fill("Password123");

            page.getByTestId("signin-submit-button").click();

            assertThat(page.getByRole(
                    AriaRole.HEADING,
                    new Page.GetByRoleOptions()
                            .setName("Digital Poster Dashboard")
                            .setExact(true)
            )).isVisible();

            // Choose the metadata field.
            page.getByTestId("filter-metadata-key")
                    .selectOption("IMEI LTE Device");

            Locator metadataValue =
                    page.getByTestId("filter-metadata-value");

            assertThat(metadataValue).isEnabled();
            metadataValue.scrollIntoViewIfNeeded();
            page.waitForTimeout(2000);

            // Select a specific IMEI first.
            metadataValue.selectOption("352811044505810");
            assertThat(metadataValue).hasValue("352811044505810");
            page.waitForTimeout(2000);

            // Then select the empty option.
            metadataValue.selectOption("");

            // Demonstration pause.
            page.waitForTimeout(3000);

            // Empty should remain selected instead of becoming All.
            assertThat(metadataValue).hasValue("");
        }
    }
}