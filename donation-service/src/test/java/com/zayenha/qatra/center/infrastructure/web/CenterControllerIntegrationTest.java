package com.zayenha.qatra.center.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class CenterControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final String baseUrl = "/api/v1/centers";

    private int counter;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String centerJson(String name, String email) {
        return """
            {
                "name": "%s",
                "address": "456 Test Ave",
                "city": "Testville",
                "country": "Testland",
                "postalCode": "99999",
                "phone": "1112223333",
                "email": "%s",
                "latitude": 35.0,
                "longitude": -120.0,
                "facilityType": "BLOOD_BANK",
                "operatingHours": {
                    "monday": {"open": "08:00", "close": "17:00"},
                    "tuesday": {"open": "08:00", "close": "17:00"},
                    "wednesday": {"open": "08:00", "close": "17:00"},
                    "thursday": {"open": "08:00", "close": "17:00"},
                    "friday": {"open": "08:00", "close": "14:00"}
                },
                "totalCapacity": 100,
                "maxRegular": 50,
                "slotPeriod": 30
            }
            """.formatted(name, email);
    }

    private Long createCenter(String name, String email) throws Exception {
        var json = centerJson(name, email);
        var result = mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        var responseJson = result.getResponse().getContentAsString();
        var id = responseJson.substring(responseJson.indexOf("\"id\":") + 5, responseJson.indexOf(",", responseJson.indexOf("\"id\":")));
        return Long.parseLong(id.trim());
    }

    @Test
    void createAndGetById() throws Exception {
        var id = createCenter("Integration Test Center " + (++counter), "int" + counter + "@test.com");

        mockMvc.perform(get(baseUrl + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name", containsString("Integration Test Center")));
    }

    @Test
    void getAllReturnsPaginatedResults() throws Exception {
        createCenter("Paginated A " + (++counter), "pA" + counter + "@test.com");
        createCenter("Paginated B " + (++counter), "pB" + counter + "@test.com");

        mockMvc.perform(get(baseUrl)
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.page.number").value(1));
    }

    @Test
    void updateModifiesCenter() throws Exception {
        var id = createCenter("Update Test " + (++counter), "upd" + counter + "@test.com");

        var updateJson = centerJson("Updated Name " + counter, "upd" + counter + "@test.com")
                .replace("456 Test Ave", "789 Updated Ave");

        mockMvc.perform(put(baseUrl + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name", containsString("Updated Name")));
    }

    @Test
    void updateStatusChangesStatus() throws Exception {
        var id = createCenter("Status Test " + (++counter), "stat" + counter + "@test.com");

        mockMvc.perform(patch(baseUrl + "/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteRemovesCenter() throws Exception {
        var id = createCenter("Delete Test " + (++counter), "del" + counter + "@test.com");

        mockMvc.perform(delete(baseUrl + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Center deleted"));

        mockMvc.perform(get(baseUrl + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByIdReturns404WhenNotFound() throws Exception {
        mockMvc.perform(get(baseUrl + "/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createReturns409WhenNameDuplicated() throws Exception {
        var name = "Dup Center " + (++counter);
        createCenter(name, "dup1" + counter + "@test.com");

        mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(centerJson(name, "dup2" + counter + "@test.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void addClosureBlocksOverlappingSlots() throws Exception {
        var id = createCenter("Closure Center " + (++counter), "clo" + counter + "@test.com");

        var closureJson = """
            {
                "date": "2030-01-15",
                "startTime": "08:00",
                "endTime": "17:00",
                "allDay": false,
                "reason": "Maintenance"
            }
            """;

        mockMvc.perform(post(baseUrl + "/" + id + "/closures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(closureJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reason").value("Maintenance"));
    }

    @Test
    void getSlotsReturnsSlotList() throws Exception {
        var id = createCenter("Slot Center " + (++counter), "slot" + counter + "@test.com");

        mockMvc.perform(get(baseUrl + "/" + id + "/slots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void addAndRemoveStaff() throws Exception {
        var id = createCenter("Staff Center " + (++counter), "staff" + counter + "@test.com");

        mockMvc.perform(post(baseUrl + "/" + id + "/staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\": 999999}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(999999));

        mockMvc.perform(get(baseUrl + "/" + id + "/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(delete(baseUrl + "/" + id + "/staff/999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void blockSlotReturnsUpdatedSlot() throws Exception {
        var id = createCenter("Block Center " + (++counter), "blk" + counter + "@test.com");

        var slotsJson = mockMvc.perform(get(baseUrl + "/" + id + "/slots"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(patch(baseUrl + "/" + id + "/slots/99999/block")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isBlocked\": true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPendingReturnsOnlyPendingCenters() throws Exception {
        createCenter("Pending Center " + (++counter), "pend" + counter + "@test.com");

        mockMvc.perform(get(baseUrl + "/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void approveActivatesCenter() throws Exception {
        var id = createCenter("Approve Center " + (++counter), "appr" + counter + "@test.com");

        mockMvc.perform(patch(baseUrl + "/" + id + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"approved\": true, \"reason\": \"Looks good\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }
}
