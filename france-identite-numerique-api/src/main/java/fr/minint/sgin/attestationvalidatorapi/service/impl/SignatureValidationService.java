package fr.minint.sgin.attestationvalidatorapi.service.impl;

import eu.europa.esig.dss.detailedreport.DetailedReport;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.validation.reports.Reports;
import fr.minint.sgin.attestationvalidatorapi.config.SignatureConfig;
import fr.minint.sgin.attestationvalidatorapi.entity.SignatureIdValidation;
import fr.minint.sgin.attestationvalidatorapi.entity.TimestampIdValidation;
import fr.minint.sgin.attestationvalidatorapi.service.ISignatureValidationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignatureValidationService implements ISignatureValidationService {

    private final SignatureConfig signatureConfig;

    public SignatureValidationService(SignatureConfig signatureConfig) {
        this.signatureConfig = signatureConfig;
    }

    /**
     * Check if attestation signature and timestamp are valid
     * @param reports
     * @return
     */
    public boolean isAttestationSignatureTimestampValid(Reports reports) {
        return isAttestationSignatureValid(reports) && isAttestationTimestampValid(reports);
    }

    /**
     * Check if attestation signature is valid
     * @param reports
     * @return
     */
    public boolean isAttestationSignatureValid(Reports reports) {
        DetailedReport detailedReport = reports.getDetailedReport();
        DiagnosticData diagnosticData = reports.getDiagnosticData();
        List<String> signatureIds = detailedReport.getSignatureIds();
        return !signatureIds.isEmpty() && signatureIds.stream().allMatch(signatureId -> isSignatureValid(signatureId, detailedReport, diagnosticData));
    }

    /**
     * Check if attestation timestamp is valid
     * @param reports
     * @return
     */
    public boolean isAttestationTimestampValid(Reports reports) {
        DetailedReport detailedReport = reports.getDetailedReport();
        List<String> timestampIds = detailedReport.getTimestampIds();
        return !timestampIds.isEmpty() && timestampIds.stream().allMatch(timestampId -> isTimestampValid(timestampId, detailedReport));
    }

    /**
     * Check the validity of the signature
     * @param signatureId
     * @param detailedReport
     * @param diagnosticData
     * @return
     */
    private boolean isSignatureValid(String signatureId, DetailedReport detailedReport, DiagnosticData diagnosticData) {
        SignatureIdValidation signatureIdValidation = new SignatureIdValidation(signatureId, detailedReport, diagnosticData, signatureConfig.isVerifyHashSignature());
        return signatureIdValidation.isValid(signatureConfig.getSignaturePolicyId(), signatureConfig.getOrganizationalUnit(), signatureConfig.getOrganizationIdentifier());
    }

    /**
     * Check the validity of the timestamp
     * @param timestampId
     * @param detailedReport
     * @return
     */
    private boolean isTimestampValid(String timestampId, DetailedReport detailedReport) {
        TimestampIdValidation timestampIdValidation = new TimestampIdValidation(timestampId, detailedReport);
        return timestampIdValidation.isValid();
    }
}
