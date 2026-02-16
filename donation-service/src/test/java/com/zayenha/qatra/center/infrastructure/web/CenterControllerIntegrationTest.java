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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String validCenterJson() {
        return """
            {
                "name": "Integration Test Center",
                "address": "456 Test Ave",
                "city": "Testville",
                "country": "Testland",
                "postalCode": "99999",
                "phone": "1112223333",
                "email": "integration@test.com",
                "latitude": 35.0,
                "longitude": -120.0,
                "facilityType": "BLOOD_BANK",
                "operatingHours": {
                    "monday": {"open": "08:00:00", "close": "17:00:00"}
                },
                "totalCapacity": 100,
                "maxRegular": 50,
                "slotPeriod": 30
            }
            """;
    }

    @Test
    void createAndGetById() throws Exception {
        var createJson = validCenterJson();

        var createResult = mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Integration Test Center"))
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"))
                .andReturn();

        var json = createResult.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(get(baseUrl + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Integration Test Center"));
    }

    @Test
    void getAllReturnsPaginatedResults() throws Exception {
        mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCenterJson().replace("Integration Test Center", "Paginated Center 1")));
        mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCenterJson().replace("integration@test.com", "paginated2@test.com")
                        .replace("Integration Test Center", "Paginated Center 2")));

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
        var createJson = validCenterJson().replace("Integration Test Center", "Update Test Center");
        var createResult = mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        var json = createResult.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

        var updateJson = createJson
                .replace("Update Test Center", "Updated Name")
                .replace("456 Test Ave", "789 Updated Ave")
                .replace("integration@test.com", "updated@test.com");

        mockMvc.perform(put(baseUrl + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    @Test
    void updateStatusChangesStatus() throws Exception {
        var createResult = mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCenterJson().replace("Integration Test Center", "Status Test Center")
                        .replace("integration@test.com", "status@test.com")))
                .andExpect(status().isCreated())
                .andReturn();

        var json = createResult.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(patch(baseUrl + "/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "ACTIVE"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteRemovesCenter() throws Exception {
        var createResult = mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCenterJson().replace("Integration Test Center", "Delete Test Center")
                        .replace("integration@test.com", "delete@test.com")))
                .andExpect(status().isCreated())
                .andReturn();

        var json = createResult.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

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
        var json = validCenterJson().replace("Integration Test Center", "Duplicate Center")
                .replace("integration@test.com", "dup1@test.com");

        mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        var dupJson = validCenterJson().replace("Integration Test Center", "Duplicate Center")
                .replace("integration@test.com", "dup2@test.com");

        mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dupJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Duplicate Center")));
    }
}
