package fr.minint.sgin.attestationvalidatorapi.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class OpenApiTest {

    @Autowired
    protected MockMvc mockMvc;

    @Test
    @DisplayName("Test Open API Output")
    void testOpenApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs.yaml")).andExpect(status().is2xxSuccessful()).andReturn();
        File openApi = new File(getClass().getClassLoader().getResource("").getFile(), "open-api.yaml");
        Files.writeString(Path.of(openApi.getPath()), result.getResponse().getContentAsString());
    }

}

