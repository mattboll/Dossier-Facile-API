package fr.minint.sgin.attestationvalidatorapi.service;

import fr.minint.sgin.attestationvalidatorapi.dto.AttributesDTO;
import fr.minint.sgin.attestationvalidatorapi.enums.FieldAttribute;
import fr.minint.sgin.attestationvalidatorapi.exception.technical.TechnicalException;
import fr.minint.sgin.attestationvalidatorapi.service.impl.ContentValidationService;
import fr.minint.sgin.attestationvalidatorapi.utils.Messages;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;


@ExtendWith(MockitoExtension.class)
@DisplayName("Content validation service tests")
class ContentValidationServiceTests {

    @InjectMocks
    private ContentValidationService contentValidationService;

    @Test
    @DisplayName("Get date valid from document")
    void testGetDateValid() throws IOException, TechnicalException {
        File file = new File("src/test/resources/file/justificatif_identite_content.pdf");
        PDDocument document = PDDocument.load(file);
        String dateValid = contentValidationService.getDateValid(document);
        Assertions.assertEquals("10/11/2022", dateValid, "Date valid is not correct");
    }

    @Test
    @DisplayName("Get all attributes from document")
    void testGetAllAttributes() throws IOException {
        AttributesDTO expected = AttributesDTO.builder()
                .id("L0LIQNX2WW1OKTI")
                .recipient("Guillaume")
                .reason("test motif")
                .generatedDate("11/10/2022")
                .validityDate("10/11/2022")
                .familyName("Jean-Baptiste")
                .givenName("Sabine")
                .usageName(null)
                .gender("M")
                .nationality("FRA")
                .birthDate("15/09/1999")
                .birthPlace("BEAUPONT")
                .build();

        File file = new File("src/test/resources/file/justificatif_identite_content.pdf");
        PDDocument document = PDDocument.load(file);
        AttributesDTO attributesDTO = contentValidationService.getAttributesFromPDDocument(document);
        Assertions.assertEquals(expected, attributesDTO);
    }

    @Test
    @DisplayName("Get some attributes from document")
    void testGetSomeAttributes() throws IOException {
        AttributesDTO expected = AttributesDTO.builder()
                .id("L0LIQNX2WW1OKTI")
                .recipient("Guillaume")
                .reason("test motif")
                .gender("M")
                .birthPlace("BEAUPONT")
                .build();

        File file = new File("src/test/resources/file/justificatif_identite_content.pdf");
        PDDocument document = PDDocument.load(file);
        AttributesDTO attributesDTO = contentValidationService
                .getAttributesFromPDDocument(document,
                        Set.of(
                                FieldAttribute.ID,
                                FieldAttribute.RECIPIENT,
                                FieldAttribute.REASON,
                                FieldAttribute.GENDER,
                                FieldAttribute.BIRTH_PLACE
                        ));
        Assertions.assertEquals(expected, attributesDTO);
    }

    @Test
    @DisplayName("Get not existing date valid from document")
    void testGetDateValidNotExist() {
        TechnicalException exception = Assertions.assertThrows(TechnicalException.class, () -> contentValidationService.isDateValidNotExpire(""));
        Assertions.assertEquals(Messages.SGIN_NO_VALID_DATE, exception.getMessage(), "The error message is not correct");
    }

    @Test
    @DisplayName("Date valid is expired")
    void testDateValidIsExpired() throws TechnicalException {
        LocalDate dateMinus5Days = LocalDate.now().minusDays(5);
        boolean isNotExpired = contentValidationService.isDateValidNotExpire(dateMinus5Days.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        Assertions.assertFalse(isNotExpired, "Date valid should be expired");
    }

    @Test
    @DisplayName("Date valid is not expire")
    void testDateValidIsNotExpired() throws TechnicalException {
        LocalDate dateNow = LocalDate.now();
        LocalDate datePlus5Days = dateNow.plusDays(5);
        boolean isNotExpiredNow = contentValidationService.isDateValidNotExpire(dateNow.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        boolean isNotExpiredPlus5Days = contentValidationService.isDateValidNotExpire(datePlus5Days.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        Assertions.assertAll("Some results are not correct",
                () -> Assertions.assertTrue(isNotExpiredNow, "Date valid should not be expired"),
                () -> Assertions.assertTrue(isNotExpiredPlus5Days, "Date valid should not be expired"));
    }
}
