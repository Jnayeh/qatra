package com.zayenha.qatra.user.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    private static int counter;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    private final String baseUrl = "/api/v1/admin/users";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("DELETE FROM audit_logs");
        jdbcTemplate.execute("DELETE FROM center_admin_profiles");
        jdbcTemplate.execute("DELETE FROM center_staff_profiles");
        jdbcTemplate.execute("DELETE FROM user_roles");
        jdbcTemplate.execute("DELETE FROM donor_profiles");
        jdbcTemplate.execute("DELETE FROM sessions");
        jdbcTemplate.execute("DELETE FROM verification_tokens");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("ALTER SEQUENCE user_seq RESTART WITH 1");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        counter = 0;
    }

    private UsernamePasswordAuthenticationToken adminAuth(long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
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
                .with(authentication(adminAuth(1L)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email))
                .andReturn();

        var responseBody = createResult.getResponse().getContentAsString();
        var id = responseBody.substring(responseBody.indexOf("\"id\":") + 5, responseBody.indexOf(",", responseBody.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(get(baseUrl + "/" + id)
                .with(authentication(adminAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.displayName").value("Integration User"));
    }

    @Test
    void getAllReturnsPaginatedResults() throws Exception {
        mockMvc.perform(post(baseUrl)
                .with(authentication(adminAuth(1L)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(uniqueEmail(), "2000000001")));
        mockMvc.perform(post(baseUrl)
                .with(authentication(adminAuth(1L)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(uniqueEmail(), "2000000002")));

        mockMvc.perform(get(baseUrl)
                .with(authentication(adminAuth(1L)))
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
                .with(authentication(adminAuth(1L)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(email, "3000000001")))
                .andExpect(status().isCreated())
                .andReturn();

        var json = createResult.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(put(baseUrl + "/" + id)
                .with(authentication(adminAuth(1L)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "updated@example.com", "phone": "5556667777", "displayName": "Updated User", "firstName": "Updated", "familyName": "User"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.displayName").value("Updated User"));
    }

    @Test
    void updateStatusChangesStatus() throws Exception {
        var email = uniqueEmail();
        var createResult = mockMvc.perform(post(baseUrl)
                .with(authentication(adminAuth(1L)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(email, "4000000001")))
                .andExpect(status().isCreated())
                .andReturn();

        var json = createResult.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(patch(baseUrl + "/" + id + "/status")
                .with(authentication(adminAuth(1L)))
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
                .with(authentication(adminAuth(1L)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(email, "5000000001")))
                .andExpect(status().isCreated())
                .andReturn();

        var json = createResult.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(patch(baseUrl + "/" + id + "/status")
                .with(authentication(adminAuth(1L)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "INACTIVE"}
                    """))
                .andExpect(status().isOk());

        mockMvc.perform(delete(baseUrl + "/" + id)
                .with(authentication(adminAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("User deleted"));

        mockMvc.perform(get(baseUrl + "/" + id)
                .with(authentication(adminAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELETED"));
    }

    @Test
    void getByIdReturns404WhenNotFound() throws Exception {
        mockMvc.perform(get(baseUrl + "/99999")
                .with(authentication(adminAuth(1L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createReturns409WhenEmailDuplicated() throws Exception {
        var email = uniqueEmail();
        mockMvc.perform(post(baseUrl)
                .with(authentication(adminAuth(1L)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(email, "6000000001")))
                .andExpect(status().isCreated());

        mockMvc.perform(post(baseUrl)
                .with(authentication(adminAuth(1L)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserJson(email, "6000000002")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString(email)));
    }
}
