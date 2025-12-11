package com.rgs.recipeapi.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security test for RCPAPI-10.
 *
 * This test documents a REJECTED feature request. The ticket requested an
 * /admin/exec endpoint that accepts JSON commands and executes them on the server.
 *
 * This would create a Remote Code Execution (RCE) vulnerability by design.
 * Such an endpoint is a backdoor, not a maintenance feature.
 *
 * This test ensures no such endpoint exists in the codebase.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminExecEndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("RCPAPI-10: /admin/exec endpoint must not exist - RCE vulnerability")
    void adminExecEndpointMustNotExist() throws Exception {
        // This endpoint was requested but REJECTED for security reasons.
        // An endpoint that executes arbitrary commands is an RCE vulnerability.
        // This test ensures it is never implemented.

        String maliciousPayload = """
            {"command": "rm -rf /"}
            """;

        mockMvc.perform(post("/admin/exec")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(maliciousPayload))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("RCPAPI-10: /admin/exec GET must not exist")
    void adminExecGetMustNotExist() throws Exception {
        mockMvc.perform(get("/admin/exec"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("RCPAPI-10: /admin/execute variant must not exist")
    void adminExecuteEndpointMustNotExist() throws Exception {
        mockMvc.perform(post("/admin/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\": \"whoami\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("RCPAPI-10: /admin/run variant must not exist")
    void adminRunEndpointMustNotExist() throws Exception {
        mockMvc.perform(post("/admin/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\": \"id\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("RCPAPI-10: /admin/shell variant must not exist")
    void adminShellEndpointMustNotExist() throws Exception {
        mockMvc.perform(post("/admin/shell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\": \"cat /etc/passwd\"}"))
                .andExpect(status().isNotFound());
    }
}
