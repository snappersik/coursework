package com.almetpt.coursework.bookclub.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "USER")
    void getAllProductCategories_ShouldReturnAllCategories() throws Exception {
        mockMvc.perform(get("/api/rest/products/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0]").value("E_BOOK"))
                .andExpect(jsonPath("$[1]").value("AUDIO_BOOK"))
                .andExpect(jsonPath("$[2]").value("LECTURE"))
                .andExpect(jsonPath("$[3]").value("MEETING_RECORDING"));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void getAllProductCategoriesWithDescriptions_ShouldReturnAllCategoriesWithDescriptions() throws Exception {
        mockMvc.perform(get("/api/rest/products/categories/with-descriptions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].name").value("E_BOOK"))
                .andExpect(jsonPath("$[0].description").value("Электронная книга"))
                .andExpect(jsonPath("$[1].name").value("AUDIO_BOOK"))
                .andExpect(jsonPath("$[1].description").value("Аудиокнига"))
                .andExpect(jsonPath("$[2].name").value("LECTURE"))
                .andExpect(jsonPath("$[2].description").value("Лекция"))
                .andExpect(jsonPath("$[3].name").value("MEETING_RECORDING"))
                .andExpect(jsonPath("$[3].description").value("Записи прошлых встреч"));
    }
}
