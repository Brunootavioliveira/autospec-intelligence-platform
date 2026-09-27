package br.com.autospec.backend.modules.vehicle.controller;

import br.com.autospec.backend.config.SecurityMocksConfig;
import br.com.autospec.backend.modules.vehicle.dto.VehicleRequestDTO;
import br.com.autospec.backend.modules.vehicle.dto.VehicleResponseDTO;
import br.com.autospec.backend.modules.vehicle.service.VehicleComparisonService;
import br.com.autospec.backend.modules.vehicle.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
@Import(SecurityMocksConfig.class)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private VehicleComparisonService vehicleComparisonService;

    private final VehicleRequestDTO validRequest =
            new VehicleRequestDTO("Ford", "Ranger", "Raptor", 2026);

    @Test
    void generateSpec_semAutenticacao_deveRetornar403() throws Exception {
        mockMvc.perform(post("/api/v1/vehicles/spec")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void generateSpec_comRoleViewer_deveRetornar403() throws Exception {
        mockMvc.perform(post("/api/v1/vehicles/spec")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void generateSpec_comRoleAnalyst_deveRetornar201() throws Exception {
        VehicleResponseDTO response = new VehicleResponseDTO(
                1L, "Ford", "Ranger", "Raptor", 2026,
                null, null, null, null, null, null, null, null, null, null, null, null
        );

        when(vehicleService.generateVehicleSpec(any(VehicleRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/vehicles/spec")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void delete_comRoleAnalyst_deveRetornar403() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/spec/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_comRoleAdmin_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/spec/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void findById_naoEncontrado_deveRetornar404() throws Exception {
        when(vehicleService.findById(99L))
                .thenThrow(new br.com.autospec.backend.core.exception.ResourceNotFoundException("Vehicle not found"));

        mockMvc.perform(get("/api/v1/vehicles/spec/99"))
                .andExpect(status().isNotFound());
    }
}