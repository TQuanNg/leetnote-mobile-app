package com.example.leetnote_backend.controller;

import com.example.leetnote_backend.config.FirebaseAuthenticationFilter;
import com.example.leetnote_backend.config.UserPrincipal;
import com.example.leetnote_backend.model.DTO.ProblemDetailDTO;
import com.example.leetnote_backend.model.DTO.ProblemListDTO;
import com.example.leetnote_backend.service.ProblemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(ProblemController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProblemService problemService;

    @MockitoBean
    private FirebaseAuthenticationFilter firebaseAuthenticationFilter;

    private RequestPostProcessor authenticated(){
        UserPrincipal principal = new UserPrincipal(1L, "firebase-123", "test@example.com");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        return SecurityMockMvcRequestPostProcessors.securityContext(context);
    }

    @Test
    void getAllProblems_ReturnsPagedProblems() throws Exception {
        ProblemListDTO problem = new ProblemListDTO(1L, "Two Sum", "Easy", false, false);
        Page<ProblemListDTO> page = new PageImpl<>(List.of(problem));

        when(problemService.getAllProblems(eq(1L), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/problems").with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].problemId").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Two Sum"))
                .andExpect(jsonPath("$.content[0].difficulty").value("Easy"))
                .andExpect(jsonPath("$.content[0].solved").value(false))
                .andExpect(jsonPath("$.content[0].favorite").value(false));
    }

    @Test
    void getProblemById_ReturnsProblemDetail() throws Exception {
        ProblemDetailDTO detail = new ProblemDetailDTO();
        detail.setId(1L);
        detail.setTitle("Two Sum");
        detail.setDifficulty("Easy");
        detail.setDescription("Find indices...");
        detail.setSolved(false);
        detail.setFavorite(false);

        when(problemService.getProblemDetail(1L, 1L)).thenReturn(detail);

        mockMvc.perform(get("/problems/1")
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Two Sum"))
                .andExpect(jsonPath("$.difficulty").value("Easy"))
                .andExpect(jsonPath("$.description").value("Find indices..."))
                .andExpect(jsonPath("$.solved").value(false))
                .andExpect(jsonPath("$.favorite").value(false));
    }

    @Test
    void getProblemById_ThrowsWhenNotFound() throws Exception {
        when(problemService.getProblemDetail(1L, 1L)).thenReturn(null);

        mockMvc.perform(get("/problems/1")
                        .with(authenticated()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_ReturnsUpdatedProblem() throws Exception {
        ProblemListDTO updated = new ProblemListDTO(1L, "Two Sum", "Easy", true, true);

        when(problemService.updateProblemStatus(1L, 1L, true, true)).thenReturn(updated);

        mockMvc.perform(put("/problems/1/status")
                        .param("isSolved", "true")
                        .param("isFavorite", "true")
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problemId").value(1L))
                .andExpect(jsonPath("$.solved").value(true))
                .andExpect(jsonPath("$.favorite").value(true));
    }
}
