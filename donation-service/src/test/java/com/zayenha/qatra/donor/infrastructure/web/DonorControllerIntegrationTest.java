package com.zayenha.qatra.donor.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
@ActiveProfiles("test")
class DonorControllerIntegrationTest {

    private static int counter;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("DELETE FROM audit_logs");
        jdbcTemplate.execute("DELETE FROM donation_certificates");
        jdbcTemplate.execute("DELETE FROM appointments");
        jdbcTemplate.execute("DELETE FROM health_questionnaires");
        jdbcTemplate.execute("DELETE FROM donor_profiles");
        jdbcTemplate.execute("DELETE FROM center_admin_profiles");
        jdbcTemplate.execute("DELETE FROM center_staff_profiles");
        jdbcTemplate.execute("DELETE FROM user_roles");
        jdbcTemplate.execute("DELETE FROM sessions");
        jdbcTemplate.execute("DELETE FROM verification_tokens");
        jdbcTemplate.execute("DELETE FROM donation_centers");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("ALTER SEQUENCE user_seq RESTART WITH 1");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        counter = 0;
    }

    private UsernamePasswordAuthenticationToken auth(long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"), new SimpleGrantedAuthority("ROLE_DONOR")));
    }

    private void createProfile(long userId) throws Exception {
        mockMvc.perform(put("/api/v1/donors/me")
                .with(authentication(auth(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyProfileReturnsExistingProfile() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(get("/api/v1/donors/me")
                .with(authentication(auth(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.bloodType").value("UNKNOWN"))
                .andExpect(jsonPath("$.data.availability").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.profileComplete").value(false));
    }

    @Test
    void updateBloodTypeSetsBloodType() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(put("/api/v1/donors/me/blood-type")
                .with(authentication(auth(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bloodType\": \"A_POSITIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bloodType").value("A_POSITIVE"))
                .andExpect(jsonPath("$.data.bloodTypeVerified").value(false));
    }

    @Test
    void updateBloodTypeCanChangeAgainWhenNotVerified() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(put("/api/v1/donors/me/blood-type")
                .with(authentication(auth(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bloodType\": \"A_POSITIVE\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/donors/me/blood-type")
                .with(authentication(auth(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bloodType\": \"B_POSITIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bloodType").value("B_POSITIVE"));
    }

    @Test
    void updateLocationSetsLocation() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(put("/api/v1/donors/me/location")
                .with(authentication(auth(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\": 40.71, \"longitude\": -74.00, \"city\": \"NYC\", \"country\": \"USA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.latitude").value(40.71))
                .andExpect(jsonPath("$.data.longitude").value(-74.00))
                .andExpect(jsonPath("$.data.city").value("NYC"));
    }

    @Test
    void updateAvailabilityChangesStatus() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(put("/api/v1/donors/me/availability")
                .with(authentication(auth(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"TEMPORARILY_UNAVAILABLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.availability").value("TEMPORARILY_UNAVAILABLE"));
    }

    @Test
    void healthQuestionnaireFullFlow() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(put("/api/v1/donors/me/health-questionnaire")
                .with(authentication(auth(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"hasChronicIllness\": false, \"onMedication\": false, \"lastSurgeryAt\": null, \"lastTravelAt\": \"2025-06-01T00:00:00Z\", \"lastTattooOrPiercingAt\": null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lastTravelAt").isNotEmpty());

        mockMvc.perform(get("/api/v1/donors/me/health-questionnaire")
                .with(authentication(auth(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lastTravelAt").isNotEmpty());
    }

    @Test
    void healthQuestionnaireChronicIllnessSetsRestrictionReason() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(put("/api/v1/donors/me/health-questionnaire")
                .with(authentication(auth(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"hasChronicIllness\": true, \"onMedication\": false, \"lastSurgeryAt\": null, \"lastTravelAt\": null, \"lastTattooOrPiercingAt\": null}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/donors/me")
                .with(authentication(auth(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.restrictionReason", org.hamcrest.Matchers.containsString("Chronic illness")));
    }

    @Test
    void getEligibilityReturnsEligibleForNewProfile() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(get("/api/v1/donors/me/eligibility")
                .with(authentication(auth(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.eligible").value(true));
    }

    @Test
    void getImpactReturnsImpactResponse() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(get("/api/v1/donors/me/impact")
                .with(authentication(auth(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDonations").value(0))
                .andExpect(jsonPath("$.data.milestones").isArray());
    }

    @Test
    void getDonorEligibilityReturnsEligibility() throws Exception {
        var donorUserId = ++counter;
        createProfile(donorUserId);

        var profileJson = mockMvc.perform(get("/api/v1/donors/me")
                .with(authentication(auth(donorUserId))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var donorId = profileJson.substring(profileJson.indexOf("\"id\":") + 5, profileJson.indexOf(",", profileJson.indexOf("\"id\":")));
        donorId = donorId.trim();

        mockMvc.perform(get("/api/v1/donors/" + donorId + "/eligibility")
                .with(authentication(auth(99L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.eligible").value(true));
    }

    @Test
    void updateRestrictionModifiesDonor() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        var getProfile = mockMvc.perform(get("/api/v1/donors/me")
                .with(authentication(auth(userId))))
                .andExpect(status().isOk())
                .andReturn();
        var json = getProfile.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(patch("/api/v1/donors/" + id + "/restriction")
                .with(authentication(auth(99L)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"permanentlyRestricted\": true, \"restrictionReason\": \"Admin override\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.permanentlyRestricted").value(true));
    }

    @Test
    void updateFlagModifiesDonor() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        var getProfile = mockMvc.perform(get("/api/v1/donors/me")
                .with(authentication(auth(userId))))
                .andExpect(status().isOk())
                .andReturn();
        var json = getProfile.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(patch("/api/v1/donors/" + id + "/flag")
                .with(authentication(auth(99L)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"flaggedForManualReview\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flaggedForManualReview").value(true));
    }
}
