package com.nexaworks.rafiq.test.doctor.integration;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.nexaworks.rafiq.test.BaseIntegrationTest;

@DisplayName("Specialization Controller Integration Testing")
class SpecializationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Get Specialization List")
    void getSpecializationList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/specialization")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$[0].id").exists()).andExpect(jsonPath("$[0].name").exists());
    }
}

