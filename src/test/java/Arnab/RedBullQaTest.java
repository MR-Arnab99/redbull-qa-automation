package Arnab;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RedBullQaTest {

    private static final String BASE_URL =
            "https://qa-sample-arnab-mondal.up.railway.app";

    private static final String DEVICE_ID =
            "79101X02X002015003CAJ2000";

    private static final String DEVICE_PATH = "/api/devices/75997";

    @Test
    void onlineDeviceChangesFromOutdatedToUpToDate() {
        String email = "qa.tester@example.com";
        String password = "Password123";

        try (Playwright playwright = Playwright.create()) {
            APIRequestContext api = playwright.request().newContext(
                    new APIRequest.NewContextOptions()
                            .setBaseURL(BASE_URL)
            );

            try {
                // 1. Sign in and obtain an API token.
                APIResponse login = api.post(
                        "/api/auth/signin",
                        RequestOptions.create().setData(Map.of(
                                "email", email,
                                "password", password
                        ))
                );

                assertEquals(200, login.status(), "Sign-in must succeed");

                String token = json(login).get("token").getAsString();


                // 2. Read the device before updating.
                APIResponse beforeResponse = api.get(
                        DEVICE_PATH,
                        RequestOptions.create()
                                .setHeader(
                                        "Authorization",
                                        "Bearer " + token
                                )
                );

                assertEquals(
                        200, beforeResponse.status(),
                        "Reading the device must succeed"
                );

                JsonObject before = json(beforeResponse);

                assertEquals(
                        DEVICE_ID,
                        before.get("device_id").getAsString()
                );

                assertEquals(
                        "online",
                        before.get("presence_status").getAsString(),
                        "The device must be online"
                );

                assertEquals(
                        "outdated",
                        before.get("core_services_status").getAsString(),
                        "The device must start Outdated."
                );

                // 3. Send the update command.
                APIResponse update = api.post(
                        "/api/devices/command",
                        RequestOptions.create()
                                .setHeader(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .setData(Map.of(
                                        "devices", List.of(DEVICE_ID),
                                        "command_name", "update_core_services",
                                        "params", Map.of(
                                                "command_version", "6.4.10"
                                        )
                                ))
                );

                assertEquals(
                        200, update.status(),
                        "The online command must succeed"
                );

                var updatedDevices = json(update)
                        .getAsJsonArray("updated");

                assertNotNull(
                        updatedDevices,
                        "Response must contain an updated list"
                );

                assertEquals(1, updatedDevices.size());

                assertEquals(
                        DEVICE_ID,
                        updatedDevices.get(0).getAsString(),
                        "Response must identify the requested device"
                );

                // 4. Read the device again.
                APIResponse afterResponse = api.get(
                        DEVICE_PATH,
                        RequestOptions.create()
                                .setHeader(
                                        "Authorization",
                                        "Bearer " + token
                                )
                );

                assertEquals(
                        200, afterResponse.status(),
                        "Reading the updated device must succeed"
                );

                JsonObject after = json(afterResponse);

                assertEquals(
                        DEVICE_ID,
                        after.get("device_id").getAsString()
                );

                assertEquals(
                        "up-to-date",
                        after.get("core_services_status").getAsString(),
                        "The saved status must change to Up-To-Date"
                );

                System.out.println(
                        "PASS: online device changed from "
                                + "Outdated to Up-To-Date."
                );

            } finally {
                api.dispose();
            }
        }
    }

    private static JsonObject json(APIResponse response) {
        return JsonParser.parseString(response.text())
                .getAsJsonObject();
    }
}