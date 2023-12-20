package fr.minint.sgin.attestationvalidatorapi.service;

import eu.europa.esig.dss.validation.reports.Reports;
import fr.minint.sgin.attestationvalidatorapi.config.SignatureConfig;
import fr.minint.sgin.attestationvalidatorapi.service.impl.SignatureValidationService;
import fr.minint.sgin.attestationvalidatorapi.utils.ReportsUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Signatures validation service tests")
class SignatureValidationServiceTests {

    private SignatureValidationService signatureValidationService;

    private SignatureConfig signatureConfig;

    private static Reports mockReportsValid;
    private static Reports mockReportsNotValid;
    private static Reports mockReportsNotValidSigned;

    @BeforeAll
    static void setUp() throws Exception {
        mockReportsValid = ReportsUtils.getMockReportsValid();
        mockReportsNotValid = ReportsUtils.getMockReportsNotValid();
        mockReportsNotValidSigned = ReportsUtils.getMockReportsNotValidSigned();
    }

    @BeforeEach
    void setUpEach() {
        signatureConfig = new SignatureConfig();
        signatureConfig.setSignaturePolicyId("1.2.250.1.152.202.1.1.1");
        signatureConfig.setOrganizationalUnit("0002110014016");
        signatureConfig.setOrganizationIdentifier("NTRFR-110014016");
        signatureConfig.setVerifyHashSignature(true);
        signatureValidationService = new SignatureValidationService(signatureConfig);
    }

    /**
     * Signature + timestamp
     */


    @Test
    @DisplayName("Signature and timestamp OK with valid signed pdf")
    void testIsAttestationSignatureTimestampValidWithSigned() {
        boolean result  = signatureValidationService.isAttestationSignatureTimestampValid(mockReportsValid);
        Assertions.assertTrue(result, "The signature validation result is not correct");
    }

    @Test
    @DisplayName("Signature and timestamp KO with valid but non signed pdf")
    void testIsAttestationSignatureTimestampNotValidWithSigned() {
        boolean result  = signatureValidationService.isAttestationSignatureTimestampValid(mockReportsNotValidSigned);
        Assertions.assertFalse(result, "The signature validation result is not correct");
    }

    @Test
    @DisplayName("Signature and timestamp KO with not valid and non signed pdf")
    void testIsAttestationSignatureTimestampValidWithNonSigned() {
        boolean result  = signatureValidationService.isAttestationSignatureTimestampValid(mockReportsNotValid);
        Assertions.assertFalse(result, "The signature validation result is not correct");
    }

    @Test
    @DisplayName("Signature valid with signed pdf and match with organization identifier and not with organizational unit")
    void testIsAttestationSignatureValidWithSignedAndOrganizationIdentifierMatchOnly() {
        signatureConfig.setOrganizationalUnit("123456");
        boolean result  = signatureValidationService.isAttestationSignatureTimestampValid(mockReportsValid);
        Assertions.assertTrue(result, "The signature validation result is not correct");
    }

    /**
     * Signature
     */

    @Test
    @DisplayName("Signature not valid with non signed pdf")
    void testIsAttestationSignatureValidWithNonSigned() {
        boolean result  = signatureValidationService.isAttestationSignatureValid(mockReportsNotValid);
        Assertions.assertFalse(result, "The signature validation result is not correct");
    }

    @Test
    @DisplayName("Signature valid with signed pdf")
    void testIsAttestationSignatureValidWithSigned() {
        boolean result  = signatureValidationService.isAttestationSignatureValid(mockReportsValid);
        Assertions.assertTrue(result, "The signature validation result is not correct");
    }

    /**
     * Timestamp
     */

    @Test
    @DisplayName("Timestamp not valid with non signed pdf")
    void testIsAttestationTimestampValidWithNonSigned() {
        boolean result  = signatureValidationService.isAttestationTimestampValid(mockReportsNotValid);
        Assertions.assertFalse(result, "The timestamp validation result is not correct");
    }

    @Test
    @DisplayName("Timestamp valid with signed pdf")
    void testIsAttestationTimestampValidWithSigned() {
        boolean result  = signatureValidationService.isAttestationTimestampValid(mockReportsValid);
        Assertions.assertTrue(result, "The timestamp validation result is not correct");
    }
}
