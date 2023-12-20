package fr.minint.sgin.attestationvalidatorapi.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.minint.sgin.attestationvalidatorapi.dto.AttributesDTO;
import fr.minint.sgin.attestationvalidatorapi.dto.DocValidationDTO;
import fr.minint.sgin.attestationvalidatorapi.dto.DocsValidationDTO;
import fr.minint.sgin.attestationvalidatorapi.enums.DocStatus;
import fr.minint.sgin.attestationvalidatorapi.exception.technical.TechnicalException;
import fr.minint.sgin.attestationvalidatorapi.service.impl.ContentValidationService;
import fr.minint.sgin.attestationvalidatorapi.service.impl.SignatureValidationService;
import fr.minint.sgin.attestationvalidatorapi.service.impl.ValidationReportsService;
import fr.minint.sgin.attestationvalidatorapi.utils.Messages;
import fr.minint.sgin.attestationvalidatorapi.utils.ReportsUtils;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import eu.europa.esig.dss.validation.reports.Reports;

@DisplayName("Attestation validation controller tests")
@WebMvcTest(controllers = AttestationValidationController.class)
class AttestationValidationControllerTests extends AbstractControllerTests {

    @MockBean
    private SignatureValidationService signatureValidationService;

    @MockBean
    private ValidationReportsService validationReportsService;

    @MockBean
    private ContentValidationService contentValidationService;

    private static Reports mockReports;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String DATE_MIN_5 = LocalDate.now().minusDays(5).format(DATE_TIME_FORMATTER);

    private static final String DATE_PLUS_5 = LocalDate.now().plusDays(5).format(DATE_TIME_FORMATTER);

    @BeforeAll
    static void setUp() throws Exception {
	mockReports = ReportsUtils.getMockReportsValid();
    }

    @Test
    @DisplayName("Check doc validity with valid attestation")
    void testCheckDocValidityStatusValid() throws Exception {
	Mockito.when(validationReportsService.getValidationReportsFromAttestation(Mockito.any()))
		.thenReturn(mockReports);
	Mockito.when(signatureValidationService.isAttestationSignatureTimestampValid(Mockito.any())).thenReturn(true);
	Mockito.when(contentValidationService.getAttributesFromPDDocument(Mockito.any(), Mockito.any()))
		.thenReturn(AttributesDTO.builder().validityDate(DATE_PLUS_5).build());
	Mockito.when(contentValidationService.isDateValidNotExpire(Mockito.any())).thenReturn(true);

	MockMultipartFile mockFile = new MockMultipartFile("file", "justificatif_identite_non_signee.pdf",
		MediaType.APPLICATION_PDF_VALUE,
		new FileInputStream("src/test/resources/file/justificatif_identite_non_signee.pdf"));
	ResultActions resultActions = mockMvc.perform(multipart("/validation/v1/check-doc-valid").file(mockFile))
		.andExpect(status().is2xxSuccessful());

	MvcResult result = resultActions.andReturn();
	String contentAsString = result.getResponse().getContentAsString();

	DocValidationDTO response = objectMapper.readValue(contentAsString, DocValidationDTO.class);

	Assertions.assertAll("Some results are not correct",
		() -> Assertions.assertEquals(DocStatus.VALID, response.getStatus(), "The status is not correct"),
		() -> Assertions.assertEquals(DATE_PLUS_5, response.getValidDate(), "The valid date is not correct"),
		() -> Assertions.assertEquals(mockFile.getOriginalFilename(), response.getFileName(),
			"The file name is not correct"),
		() -> Assertions.assertEquals(
			Base64.getEncoder()
				.encodeToString(mockReports.getXmlDetailedReport().getBytes(StandardCharsets.UTF_8)),
			response.getXmlDetailedReport(), "The xml detailed report is not correct"));
    }

    @Test
    @DisplayName("Check docs validity with valid attestations")
    void testCheckDocsValidityStatusValid() throws Exception {
	Mockito.when(validationReportsService.getValidationReportsFromAttestation(Mockito.any()))
		.thenReturn(mockReports);
	Mockito.when(signatureValidationService.isAttestationSignatureTimestampValid(Mockito.any())).thenReturn(true);
	Mockito.when(contentValidationService.getAttributesFromPDDocument(Mockito.any(), Mockito.any()))
		.thenReturn(AttributesDTO.builder().validityDate(DATE_PLUS_5).build());
	Mockito.when(contentValidationService.isDateValidNotExpire(Mockito.any())).thenReturn(true);

	MockMultipartFile mockFile = new MockMultipartFile("file", "justificatif_identite_non_signee.pdf",
		MediaType.APPLICATION_PDF_VALUE,
		new FileInputStream("src/test/resources/file/justificatif_identite_non_signee.pdf"));
	ResultActions resultActions = mockMvc
		.perform(multipart("/validation/v1/check-docs-valid").file(mockFile).file(mockFile))
		.andExpect(status().is2xxSuccessful());

	MvcResult result = resultActions.andReturn();
	String contentAsString = result.getResponse().getContentAsString();

	DocsValidationDTO response = objectMapper.readValue(contentAsString, DocsValidationDTO.class);

	Assertions.assertAll("Some results are not correct",
		() -> Assertions.assertNotNull(response.getDocValidationDTOList(),
			"The documents validation reponse should not be null"),
		() -> Assertions.assertEquals(2, response.getDocValidationDTOList().size(),
			"Number of documents validation is not correct"),
		() -> Assertions.assertTrue(response.getDocValidationDTOList().stream()
			.allMatch(res -> res.getStatus().equals(DocStatus.VALID))));
    }

    @Test
    @DisplayName("Check docs validity with non signed attestations")
    void testCheckDocsValidityStatusNotValid() throws Exception {
	Mockito.when(validationReportsService.getValidationReportsFromAttestation(Mockito.any()))
		.thenReturn(mockReports);
	Mockito.when(signatureValidationService.isAttestationSignatureTimestampValid(Mockito.any())).thenReturn(false);
	Mockito.when(contentValidationService.getAttributesFromPDDocument(Mockito.any(), Mockito.any()))
		.thenReturn(AttributesDTO.builder().validityDate(DATE_PLUS_5).build());
	Mockito.when(contentValidationService.isDateValidNotExpire(Mockito.any())).thenReturn(true);

	MockMultipartFile mockFile = new MockMultipartFile("file", "justificatif_identite_non_signee.pdf",
		MediaType.APPLICATION_PDF_VALUE,
		new FileInputStream("src/test/resources/file/justificatif_identite_non_signee.pdf"));
	ResultActions resultActions = mockMvc
		.perform(multipart("/validation/v1/check-docs-valid").file(mockFile).file(mockFile))
		.andExpect(status().is2xxSuccessful());

	MvcResult result = resultActions.andReturn();
	String contentAsString = result.getResponse().getContentAsString();

	DocsValidationDTO response = objectMapper.readValue(contentAsString, DocsValidationDTO.class);

	Assertions.assertAll("Some results are not correct",
		() -> Assertions.assertNotNull(response.getDocValidationDTOList(),
			"The documents validation reponse should not be null"),
		() -> Assertions.assertEquals(2, response.getDocValidationDTOList().size(),
			"Number of documents validation is not correct"),
		() -> Assertions.assertTrue(response.getDocValidationDTOList().stream()
			.allMatch(res -> res.getStatus().equals(DocStatus.NOT_VALID))));
    }

    @Test
    @DisplayName("Check docs validity with date valid expired")
    void testCheckDocsValidityExpired() throws Exception {
	Mockito.when(validationReportsService.getValidationReportsFromAttestation(Mockito.any()))
		.thenReturn(mockReports);
	Mockito.when(signatureValidationService.isAttestationSignatureTimestampValid(Mockito.any())).thenReturn(true);
	Mockito.when(contentValidationService.getAttributesFromPDDocument(Mockito.any(), Mockito.any()))
		.thenReturn(AttributesDTO.builder().validityDate(DATE_MIN_5).build());
	Mockito.when(contentValidationService.isDateValidNotExpire(Mockito.any())).thenReturn(false);

	MockMultipartFile mockFile = new MockMultipartFile("file", "justificatif_identite_non_signee.pdf",
		MediaType.APPLICATION_PDF_VALUE,
		new FileInputStream("src/test/resources/file/justificatif_identite_non_signee.pdf"));
	ResultActions resultActions = mockMvc
		.perform(multipart("/validation/v1/check-docs-valid").file(mockFile).file(mockFile))
		.andExpect(status().is2xxSuccessful());

	MvcResult result = resultActions.andReturn();
	String contentAsString = result.getResponse().getContentAsString();

	DocsValidationDTO response = objectMapper.readValue(contentAsString, DocsValidationDTO.class);

	Assertions.assertAll("Some results are not correct",
		() -> Assertions.assertNotNull(response.getDocValidationDTOList(),
			"The documents validation reponse should not be null"),
		() -> Assertions.assertEquals(2, response.getDocValidationDTOList().size(),
			"Number of documents validation is not correct"),
		() -> Assertions.assertTrue(response.getDocValidationDTOList().stream()
			.allMatch(res -> res.getStatus().equals(DocStatus.EXPIRED))));
    }

    @Test
    @DisplayName("Check doc validity with not valid attestation")
    void testCheckDocValidityStatusNotValid() throws Exception {
	Mockito.when(validationReportsService.getValidationReportsFromAttestation(Mockito.any()))
		.thenReturn(mockReports);
	Mockito.when(signatureValidationService.isAttestationSignatureTimestampValid(Mockito.any())).thenReturn(false);
	Mockito.when(contentValidationService.getAttributesFromPDDocument(Mockito.any(), Mockito.any()))
		.thenReturn(AttributesDTO.builder().validityDate(DATE_PLUS_5).build());
	Mockito.when(contentValidationService.isDateValidNotExpire(Mockito.any())).thenReturn(true);

	MockMultipartFile mockFile = new MockMultipartFile("file", "justificatif_identite_non_signee.pdf",
		MediaType.APPLICATION_PDF_VALUE,
		new FileInputStream("src/test/resources/file/justificatif_identite_non_signee.pdf"));
	ResultActions resultActions = mockMvc.perform(multipart("/validation/v1/check-doc-valid").file(mockFile))
		.andExpect(status().is2xxSuccessful());

	MvcResult result = resultActions.andReturn();
	String contentAsString = result.getResponse().getContentAsString();

	DocValidationDTO response = objectMapper.readValue(contentAsString, DocValidationDTO.class);

	Assertions.assertAll("Some results are not correct",
		() -> Assertions.assertEquals(DocStatus.NOT_VALID, response.getStatus(), "The status is not correct"),
		() -> Assertions.assertNull(response.getValidDate(), "The valid date is not correct"),
		() -> Assertions.assertEquals(mockFile.getOriginalFilename(), response.getFileName(),
			"The file name is not correct"),
		() -> Assertions.assertEquals(
			Base64.getEncoder()
				.encodeToString(mockReports.getXmlDetailedReport().getBytes(StandardCharsets.UTF_8)),
			response.getXmlDetailedReport(), "The xml detailed report is not correct"));
    }

    @Test
    @DisplayName("Check doc validity with expired valid date")
    void testCheckDocValidityStatusExpired() throws Exception {
	Mockito.when(validationReportsService.getValidationReportsFromAttestation(Mockito.any()))
		.thenReturn(mockReports);
	Mockito.when(signatureValidationService.isAttestationSignatureTimestampValid(Mockito.any())).thenReturn(true);
	Mockito.when(contentValidationService.getAttributesFromPDDocument(Mockito.any(), Mockito.any()))
		.thenReturn(AttributesDTO.builder().validityDate(DATE_MIN_5).build());
	Mockito.when(contentValidationService.isDateValidNotExpire(Mockito.any())).thenReturn(false);

	MockMultipartFile mockFile = new MockMultipartFile("file", "justificatif_identite_non_signee.pdf",
		MediaType.APPLICATION_PDF_VALUE,
		new FileInputStream("src/test/resources/file/justificatif_identite_non_signee.pdf"));
	ResultActions resultActions = mockMvc.perform(multipart("/validation/v1/check-doc-valid").file(mockFile))
		.andExpect(status().is2xxSuccessful());

	MvcResult result = resultActions.andReturn();
	String contentAsString = result.getResponse().getContentAsString();

	DocValidationDTO response = objectMapper.readValue(contentAsString, DocValidationDTO.class);

	Assertions.assertAll("Some results are not correct",
		() -> Assertions.assertEquals(DocStatus.EXPIRED, response.getStatus(), "The status is not correct"),
		() -> Assertions.assertEquals(DATE_MIN_5, response.getValidDate(), "The valid date is not correct"),
		() -> Assertions.assertEquals(mockFile.getOriginalFilename(), response.getFileName(),
			"The file name is not correct"),
		() -> Assertions.assertEquals(
			Base64.getEncoder()
				.encodeToString(mockReports.getXmlDetailedReport().getBytes(StandardCharsets.UTF_8)),
			response.getXmlDetailedReport(), "The xml detailed report is not correct"));
    }

    @Test
    @DisplayName("Check docs validity with not pdf files")
    void testCheckDocsValidityNotPDF() throws Exception {
	MockMultipartFile notPDFFileMock = new MockMultipartFile("file", "not_pdf.csv",
		MediaType.MULTIPART_FORM_DATA_VALUE, "value1, value2".getBytes());
	ResultActions resultActions = mockMvc
		.perform(multipart("/validation/v1/check-docs-valid").file(notPDFFileMock).file(notPDFFileMock))
		.andExpect(status().is2xxSuccessful());

	MvcResult result = resultActions.andReturn();
	String contentAsString = result.getResponse().getContentAsString();

	DocsValidationDTO response = objectMapper.readValue(contentAsString, DocsValidationDTO.class);

	Assertions.assertAll("Some results are not correct",
		() -> Assertions.assertTrue(response.getDocValidationDTOList().stream()
			.allMatch(res -> res.getStatus().equals(DocStatus.ERROR_FILE)), "The status is not correct"),
		() -> Assertions
			.assertTrue(response.getDocValidationDTOList().stream().allMatch(
				res -> res.getValidDate() == null), "The valid date is not correct"),
		() -> Assertions.assertTrue(
			response.getDocValidationDTOList().stream()
				.allMatch(res -> res.getFileName().equals(notPDFFileMock.getOriginalFilename())),
			"The file name is not correct"),
		() -> Assertions.assertTrue(
			response.getDocValidationDTOList().stream().allMatch(res -> res.getXmlDetailedReport() == null),
			"The xml detailed report is not correct"),
		() -> Assertions.assertTrue(
			response.getDocValidationDTOList().stream()
				.allMatch(res -> res.getMessage().equals(Messages.SGIN_NOT_PDF)),
			"The error message is not correct"));
    }

    @Test
    @DisplayName("Check doc validity with no file")
    void testCheckDocValidityNoFile() throws Exception {
	MockMultipartFile mockFile = new MockMultipartFile("file", "justificatif_identite_non_signee.pdf",
		MediaType.APPLICATION_PDF_VALUE, "".getBytes());
	ResultActions resultActions = mockMvc.perform(multipart("/validation/v1/check-doc-valid").file(mockFile))
		.andExpect(status().is2xxSuccessful());

	MvcResult result = resultActions.andReturn();
	String contentAsString = result.getResponse().getContentAsString();

	DocValidationDTO response = objectMapper.readValue(contentAsString, DocValidationDTO.class);

	Assertions.assertAll("Some results are not correct",
		() -> Assertions.assertEquals(DocStatus.ERROR_FILE, response.getStatus(), "The status is not correct"),
		() -> Assertions.assertNull(response.getValidDate(), "The valid date is not correct"),
		() -> Assertions.assertNull(response.getFileName(), "The file name is not correct"),
		() -> Assertions.assertNull(response.getXmlDetailedReport(), "The xml detailed report is not correct"),
		() -> Assertions.assertEquals(Messages.SGIN_NO_FILE, response.getMessage(),
			"The error message is not correct"));
    }

    @Test
    @DisplayName("Check doc validity with not pdf file")
    void testCheckDocValidityNotPDF() throws Exception {
	MockMultipartFile notPDFFileMock = new MockMultipartFile("file", "not_pdf.csv",
		MediaType.MULTIPART_FORM_DATA_VALUE, "value1, value2".getBytes());
	ResultActions resultActions = mockMvc.perform(multipart("/validation/v1/check-doc-valid").file(notPDFFileMock))
		.andExpect(status().is2xxSuccessful());

	MvcResult result = resultActions.andReturn();
	String contentAsString = result.getResponse().getContentAsString();

	DocValidationDTO response = objectMapper.readValue(contentAsString, DocValidationDTO.class);

	Assertions.assertAll("Some results are not correct",
		() -> Assertions.assertEquals(DocStatus.ERROR_FILE, response.getStatus(), "The status is not correct"),
		() -> Assertions.assertNull(response.getValidDate(), "The valid date is not correct"),
		() -> Assertions.assertEquals(notPDFFileMock.getOriginalFilename(), response.getFileName(),
			"The file name is not correct"),
		() -> Assertions.assertNull(response.getXmlDetailedReport(), "The xml detailed report is not correct"),
		() -> Assertions.assertEquals(Messages.SGIN_NOT_PDF, response.getMessage(),
			"The error message is not correct"));
    }

    @Test
    @DisplayName("Check doc validity with file too large")
    void testCheckDocValidityFileTooLarge() throws Exception {
	MockMultipartFile fileTooLarge = new MockMultipartFile("file", "to_large.pdf", MediaType.APPLICATION_PDF_VALUE,
		new FileInputStream("src/test/resources/file/justificatif_identite_fake.pdf"));
	ResultActions resultActions = mockMvc.perform(multipart("/validation/v1/check-doc-valid").file(fileTooLarge))
		.andExpect(status().is2xxSuccessful());

	MvcResult result = resultActions.andReturn();
	String contentAsString = result.getResponse().getContentAsString();

	DocValidationDTO response = objectMapper.readValue(contentAsString, DocValidationDTO.class);

	Assertions.assertAll("Some results are not correct",
		() -> Assertions.assertEquals(DocStatus.ERROR_FILE, response.getStatus(), "The status is not correct"),
		() -> Assertions.assertNull(response.getValidDate(), "The valid date is not correct"),
		() -> Assertions.assertEquals(fileTooLarge.getOriginalFilename(), response.getFileName(),
			"The file name is not correct"),
		() -> Assertions.assertNull(response.getXmlDetailedReport(), "The xml detailed report is not correct"),
		() -> Assertions.assertEquals(Messages.SGIN_FILE_TOO_LARGE, response.getMessage(),
			"The error message is not correct"));
    }

    @Test
    @DisplayName("Check doc validity with no valid date")
    void testCheckDocValidityValidDateNotPresent() throws Exception {
	Mockito.when(validationReportsService.getValidationReportsFromAttestation(Mockito.any()))
		.thenReturn(mockReports);
	Mockito.when(signatureValidationService.isAttestationSignatureTimestampValid(Mockito.any())).thenReturn(true);
	Mockito.when(contentValidationService.getAttributesFromPDDocument(Mockito.any(), Mockito.any()))
		.thenReturn(new AttributesDTO());
	Mockito.when(contentValidationService.isDateValidNotExpire(Mockito.any()))
		.thenThrow(new TechnicalException(Messages.SGIN_NO_VALID_DATE));

	MockMultipartFile mockFile = new MockMultipartFile("file", "justificatif_identite_non_signee.pdf",
		MediaType.APPLICATION_PDF_VALUE,
		new FileInputStream("src/test/resources/file/justificatif_identite_non_signee.pdf"));
	ResultActions resultActions = mockMvc.perform(multipart("/validation/v1/check-doc-valid").file(mockFile))
		.andExpect(status().is2xxSuccessful());

	MvcResult result = resultActions.andReturn();
	String contentAsString = result.getResponse().getContentAsString();

	DocValidationDTO response = objectMapper.readValue(contentAsString, DocValidationDTO.class);

	Assertions.assertAll("Some results are not correct",
		() -> Assertions.assertEquals(DocStatus.ERROR_CONTENT, response.getStatus(),
			"The status is not correct"),
		() -> Assertions.assertNull(response.getValidDate(), "The valid date is not correct"),
		() -> Assertions.assertEquals(mockFile.getOriginalFilename(), response.getFileName(),
			"The file name is not correct"),
		() -> Assertions.assertEquals(
			Base64.getEncoder()
				.encodeToString(mockReports.getXmlDetailedReport().getBytes(StandardCharsets.UTF_8)),
			response.getXmlDetailedReport(), "The xml detailed report is not correct"),
		() -> Assertions.assertEquals(Messages.SGIN_ERROR_CONTENT_VALIDATION, response.getMessage(),
			"The error message is not correct"));
    }

    @Test
    @DisplayName("Check if validity date is expired only if signature is OK")
    void testCheckValidityDateExpireOnlyWhenSignatureOK() throws Exception {
	Mockito.when(validationReportsService.getValidationReportsFromAttestation(Mockito.any()))
		.thenReturn(mockReports);
	Mockito.when(signatureValidationService.isAttestationSignatureTimestampValid(Mockito.any())).thenReturn(false);

	MockMultipartFile mockFile = new MockMultipartFile("file", "justificatif_identite_non_signee.pdf",
		MediaType.APPLICATION_PDF_VALUE,
		new FileInputStream("src/test/resources/file/justificatif_identite_non_signee.pdf"));
	ResultActions resultActions = mockMvc.perform(multipart("/validation/v1/check-doc-valid").file(mockFile))
		.andExpect(status().is2xxSuccessful());

	MvcResult result = resultActions.andReturn();
	String contentAsString = result.getResponse().getContentAsString();

	DocValidationDTO response = objectMapper.readValue(contentAsString, DocValidationDTO.class);

	Mockito.verify(contentValidationService, Mockito.never()).isDateValidNotExpire(Mockito.any());
	Assertions.assertAll("Some results are not correct",
		() -> Assertions.assertEquals(DocStatus.NOT_VALID, response.getStatus(), "The status is not correct"),
		() -> Assertions.assertNull(response.getValidDate(), "The valid date is not correct"),
		() -> Assertions.assertEquals(mockFile.getOriginalFilename(), response.getFileName(),
			"The file name is not correct"),
		() -> Assertions.assertEquals(
			Base64.getEncoder()
				.encodeToString(mockReports.getXmlDetailedReport().getBytes(StandardCharsets.UTF_8)),
			response.getXmlDetailedReport(), "The xml detailed report is not correct"),
		() -> Assertions.assertNull(response.getMessage(), "The error message is not correct"));
    }

    @Test
    @DisplayName("Extract attributes only if signature is OK")
    void testExtractAttributesOnlyWhenSignatureOK() throws Exception {
	Mockito.when(validationReportsService.getValidationReportsFromAttestation(Mockito.any()))
		.thenReturn(mockReports);
	Mockito.when(signatureValidationService.isAttestationSignatureTimestampValid(Mockito.any())).thenReturn(false);

	MockMultipartFile mockFile = new MockMultipartFile("file", "justificatif_identite_non_signee.pdf",
		MediaType.APPLICATION_PDF_VALUE,
		new FileInputStream("src/test/resources/file/justificatif_identite_non_signee.pdf"));
	ResultActions resultActions = mockMvc.perform(multipart("/validation/v1/check-doc-valid").file(mockFile))
		.andExpect(status().is2xxSuccessful());

	MvcResult result = resultActions.andReturn();
	String contentAsString = result.getResponse().getContentAsString();

	DocValidationDTO response = objectMapper.readValue(contentAsString, DocValidationDTO.class);

	Mockito.verify(contentValidationService, Mockito.never()).getAttributesFromPDDocument(Mockito.any());
	Mockito.verify(contentValidationService, Mockito.never()).getAttributesFromPDDocument(Mockito.any(),
		Mockito.any());
	Assertions.assertAll("Some results are not correct",
		() -> Assertions.assertEquals(DocStatus.NOT_VALID, response.getStatus(), "The status is not correct"),
		() -> Assertions.assertNull(response.getValidDate(), "The valid date is not correct"),
		() -> Assertions.assertEquals(mockFile.getOriginalFilename(), response.getFileName(),
			"The file name is not correct"),
		() -> Assertions.assertEquals(
			Base64.getEncoder()
				.encodeToString(mockReports.getXmlDetailedReport().getBytes(StandardCharsets.UTF_8)),
			response.getXmlDetailedReport(), "The xml detailed report is not correct"),
		() -> Assertions.assertNull(response.getMessage(), "The error message is not correct"),
		() -> Assertions.assertNull(response.getAttributes(), "The error message is not correct"));
    }
}
