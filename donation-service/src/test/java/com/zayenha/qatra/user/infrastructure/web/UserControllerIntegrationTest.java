package com.zayenha.qatra.user.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(roles = "SUPER_ADMIN")
class UserControllerIntegrationTest {

    private static int counter;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final String baseUrl = "/api/v1/admin/users";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String uniqueEmail() {
        return "test" + (++counter) + "@example.com";
    }

    private String validUserJson(String email, String phone) {
        return """
            {
                "email": "%s",
                "phone": "%s",
                "password": "password123",
                "displayName": "Integration User",
                "firstName": "Integration",
                "familyName": "User"
            }
            """.formatted(email, phone);
    }

    @Test
    void createAndGetById() throws Exception {
        var email = uniqueEmail();
        var json = validUserJson(email, "1000000001");
        var createResult = mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email))
                .andReturn();

        var responseBody = createResult.getResponse().getContentAsString();
        var id = responseBody.substring(responseBody.indexOf("\"id\":") + 5, responseBody.indexOf(",", responseBody.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(get(baseUrl + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.displayName").value("Integration User"));
    }

    @Test
    void getAllReturnsPaginatedResults() throws Exception {
        mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(uniqueEmail(), "2000000001")));
        mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(uniqueEmail(), "2000000002")));

        mockMvc.perform(get(baseUrl)
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.page.number").value(1));
    }

    @Test
    void updateModifiesUser() throws Exception {
        var email = uniqueEmail();
        var createResult = mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(email, "3000000001")))
                .andExpect(status().isCreated())
                .andReturn();

        var json = createResult.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(put(baseUrl + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "updated@example.com", "phone": "5556667777", "displayName": "Updated User"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.displayName").value("Updated User"));
    }

    @Test
    void updateStatusChangesStatus() throws Exception {
        var email = uniqueEmail();
        var createResult = mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(email, "4000000001")))
                .andExpect(status().isCreated())
                .andReturn();

        var json = createResult.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(patch(baseUrl + "/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "INACTIVE"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteRemovesUser() throws Exception {
        var email = uniqueEmail();
        var createResult = mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(email, "5000000001")))
                .andExpect(status().isCreated())
                .andReturn();

        var json = createResult.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(patch(baseUrl + "/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "INACTIVE"}
                    """))
                .andExpect(status().isOk());

        mockMvc.perform(delete(baseUrl + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("User deleted"));

        mockMvc.perform(get(baseUrl + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELETED"));
    }

    @Test
    void getByIdReturns404WhenNotFound() throws Exception {
        mockMvc.perform(get(baseUrl + "/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createReturns409WhenEmailDuplicated() throws Exception {
        var email = uniqueEmail();
        mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(email, "6000000001")))
                .andExpect(status().isCreated());

        mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(email, "6000000002")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString(email)));
    }
}
