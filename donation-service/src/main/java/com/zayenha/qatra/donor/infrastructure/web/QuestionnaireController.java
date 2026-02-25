package com.zayenha.qatra.donor.infrastructure.web;

import com.zayenha.qatra.donor.domain.port.in.QuestionnaireCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireQueryUseCases;
import com.zayenha.qatra.donor.infrastructure.web.dto.request.HealthQuestionnaireRequest;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.DonorHealthResponse;
import com.zayenha.qatra.donor.infrastructure.web.mapper.DonorMapper;
import com.zayenha.qatra.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class QuestionnaireController {

    private final QuestionnaireCommandUseCases commandUseCases;
    private final QuestionnaireQueryUseCases queryUseCases;

    @GetMapping("/api/v1/donors/me/health-questionnaire")
    public ResponseEntity<ApiResponse<DonorHealthResponse>> getHealthQuestionnaire(
            @RequestHeader("x-user-id") Long userId) {
        var questionnaire = queryUseCases.getHealthQuestionnaire(userId);
        return ResponseEntity.ok(ApiResponse.success(DonorMapper.toHealthResponse(questionnaire)));
    }

    @PutMapping("/api/v1/donors/me/health-questionnaire")
    public ResponseEntity<ApiResponse<DonorHealthResponse>> updateHealthQuestionnaire(
            @RequestHeader("x-user-id") Long userId,
            @Valid @RequestBody HealthQuestionnaireRequest request) {
        var command = new QuestionnaireCommandUseCases.HealthQuestionnaireCommand(
            request.hasChronicIllness(), request.medicalConditionsDetails(),
            request.onMedication(), request.medicationDetails(),
            request.recentSurgery(), request.recentTravel(),
            request.recentTattooOrPiercing()
        );
        var questionnaire = commandUseCases.updateHealthQuestionnaire(userId, command);
        return ResponseEntity.ok(ApiResponse.success(DonorMapper.toHealthResponse(questionnaire)));
    }
}
