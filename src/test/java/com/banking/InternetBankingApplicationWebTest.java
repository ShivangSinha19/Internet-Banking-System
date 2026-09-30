package com.banking;

import com.banking.controller.AuthController;
import com.banking.repository.UserRepository;
import com.banking.service.RestBankingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class InternetBankingApplicationWebTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestBankingService service;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void applicationWebContextLoadsAndValidatesRegistration() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"firstName\":\"A\",\"lastName\":\"B\",\"email\":\"bad\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }
}