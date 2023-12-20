package fr.minint.sgin.attestationvalidatorapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.minint.sgin.attestationvalidatorapi.config.FileConfig;
import fr.minint.sgin.attestationvalidatorapi.config.SecurityConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@Import({SecurityConfiguration.class, FileConfig.class})
public abstract class AbstractControllerTests {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

}
