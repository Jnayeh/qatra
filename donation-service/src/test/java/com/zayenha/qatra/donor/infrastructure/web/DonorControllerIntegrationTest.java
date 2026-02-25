package com.zayenha.qatra.donor.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class DonorControllerIntegrationTest {

    private static int counter;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private void createProfile(long userId) throws Exception {
        mockMvc.perform(put("/api/v1/donors/me")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyProfileReturnsExistingProfile() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(get("/api/v1/donors/me")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.bloodType").value("UNKNOWN"))
                .andExpect(jsonPath("$.data.availabilityStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.profileComplete").value(false));
    }

    @Test
    void updateBloodTypeSetsBloodType() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(put("/api/v1/donors/me/blood-type")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"bloodType": "A_POSITIVE"}
                    """))
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
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"bloodType": "A_POSITIVE"}
                    """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/donors/me/blood-type")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"bloodType": "B_POSITIVE"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bloodType").value("B_POSITIVE"));
    }

    @Test
    void updateLocationSetsLocation() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(put("/api/v1/donors/me/location")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"latitude": 40.71, "longitude": -74.00, "city": "NYC", "country": "USA"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.latitude").value(40.71))
                .andExpect(jsonPath("$.data.longitude").value(-74.00))
                .andExpect(jsonPath("$.data.city").value("NYC"))
                .andExpect(jsonPath("$.data.country").value("USA"));
    }

    @Test
    void updateAvailabilityChangesStatus() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(put("/api/v1/donors/me/availability")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "TEMPORARILY_UNAVAILABLE"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.availabilityStatus").value("TEMPORARILY_UNAVAILABLE"));
    }

    @Test
    void healthQuestionnaireFullFlow() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(put("/api/v1/donors/me/health-questionnaire")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"hasChronicIllness": false, "onMedication": false,
                     "recentSurgery": false, "recentTravel": true,
                     "recentTattooOrPiercing": false}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recentTravel").value(true));

        mockMvc.perform(get("/api/v1/donors/me/health-questionnaire")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recentTravel").value(true));
    }

    @Test
    void healthQuestionnaireChronicIllnessSetsRestrictionReason() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(put("/api/v1/donors/me/health-questionnaire")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"hasChronicIllness": true, "onMedication": false,
                     "recentSurgery": false, "recentTravel": false,
                     "recentTattooOrPiercing": false}
                    """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/donors/me")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.restrictionReason", org.hamcrest.Matchers.containsString("Chronic illness")));
    }

    @Test
    void getEligibilityReturnsEligibleForNewProfile() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(get("/api/v1/donors/me/eligibility")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.eligible").value(true));
    }

    @Test
    void requestDeletionSetsInactive() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        mockMvc.perform(delete("/api/v1/donors/me")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Deletion requested"));
    }

    @Test
    void updateRestrictionModifiesDonor() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        var getProfile = mockMvc.perform(get("/api/v1/donors/me")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn();
        var json = getProfile.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(patch("/api/v1/donors/" + id + "/restriction")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"permanentlyRestricted": true, "restrictionReason": "Admin override"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.permanentlyRestricted").value(true));
    }

    @Test
    void updateFlagModifiesDonor() throws Exception {
        var userId = ++counter;
        createProfile(userId);

        var getProfile = mockMvc.perform(get("/api/v1/donors/me")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn();
        var json = getProfile.getResponse().getContentAsString();
        var id = json.substring(json.indexOf("\"id\":") + 5, json.indexOf(",", json.indexOf("\"id\":")));
        id = id.trim();

        mockMvc.perform(patch("/api/v1/donors/" + id + "/flag")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"flaggedForManualReview": true}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flaggedForManualReview").value(true));
    }
}
