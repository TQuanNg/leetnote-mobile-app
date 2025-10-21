package com.example.leetnote_backend.controller;

import com.example.leetnote_backend.config.FirebaseAuthenticationFilter;
import com.example.leetnote_backend.config.UserPrincipal;
import com.example.leetnote_backend.model.DTO.EvaluationDTO;
import com.example.leetnote_backend.model.DTO.EvaluationDetailDTO;
import com.example.leetnote_backend.model.DTO.EvaluationListItemDTO;
import com.example.leetnote_backend.service.EvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EvaluationController.class)
@AutoConfigureMockMvc(addFilters = false)
class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvaluationService evaluationService;

    @MockitoBean
    private FirebaseAuthenticationFilter firebaseAuthenticationFilter;

    private RequestPostProcessor authenticated() {
        UserPrincipal principal = new UserPrincipal(1L, "firebase-123", "test@example.com");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        return SecurityMockMvcRequestPostProcessors.securityContext(context);
    }

    @Test
    void getAllEvaluations_returnsLatestListItem() throws Exception {
        Long pid = 42L;
        EvaluationListItemDTO item = new EvaluationListItemDTO(100L, pid, "Two Sum", LocalDateTime.now());
        when(evaluationService.getAllEvaluations(1L, pid)).thenReturn(List.of(item));

        mockMvc.perform(get("/evaluations/all").param("problemId", String.valueOf(pid)).with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].evaluationId").value(100))
                .andExpect(jsonPath("$[0].problemId").value(42))
                .andExpect(jsonPath("$[0].problemTitle").value("Two Sum"))
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    void getAllEvaluations_returnsEmptyListWhenNone() throws Exception {
        Long pid = 42L;
        when(evaluationService.getAllEvaluations(1L, pid)).thenReturn(List.of());

        mockMvc.perform(get("/evaluations/all").param("problemId", String.valueOf(pid)).with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getLastEvaluation_byEvaluationId_returnsDetail() throws Exception {
        Long evalId = 100L;
        EvaluationDTO eval = new EvaluationDTO();
        eval.setRating(4);
        EvaluationDetailDTO detail = new EvaluationDetailDTO(evalId, 42L, "Two Sum", "Easy", LocalDateTime.now(), eval, "print(\"hi\")");

        when(evaluationService.getEvaluationDetailById(1L, evalId)).thenReturn(Optional.of(detail));

        mockMvc.perform(get("/evaluations/last").param("evaluationId", String.valueOf(evalId)).with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.evaluationId").value(100))
                .andExpect(jsonPath("$.problemId").value(42))
                .andExpect(jsonPath("$.problemTitle").value("Two Sum"))
                .andExpect(jsonPath("$.difficulty").value("Easy"))
                .andExpect(jsonPath("$.evaluation.rating").value(4))
                .andExpect(jsonPath("$.solutionText").value("print(\"hi\")"));
    }

    @Test
    void getLastEvaluation_byProblemId_returnsDetail() throws Exception {
        Long pid = 42L;
        EvaluationDTO eval = new EvaluationDTO();
        eval.setRating(5);
        EvaluationDetailDTO detail = new EvaluationDetailDTO(101L, pid, "Two Sum", "Easy", LocalDateTime.now(), eval, "code");

        when(evaluationService.getLastEvaluationDetail(1L, pid)).thenReturn(Optional.of(detail));

        mockMvc.perform(get("/evaluations/last").param("problemId", String.valueOf(pid)).with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluationId").value(101))
                .andExpect(jsonPath("$.evaluation.rating").value(5));
    }

    @Test
    void getLastEvaluation_notFound_returns404() throws Exception {
        Long evalId = 999L;
        when(evaluationService.getEvaluationDetailById(1L, evalId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/evaluations/last").param("evaluationId", String.valueOf(evalId)).with(authenticated()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLastEvaluation_missingParams_returns400() throws Exception {
        mockMvc.perform(get("/evaluations/last").with(authenticated()))
                .andExpect(status().isBadRequest());
    }
}

