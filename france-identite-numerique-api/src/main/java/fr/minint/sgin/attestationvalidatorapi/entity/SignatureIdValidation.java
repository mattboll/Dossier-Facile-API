package fr.minint.sgin.attestationvalidatorapi.entity;

import eu.europa.esig.dss.detailedreport.DetailedReport;
import eu.europa.esig.dss.detailedreport.jaxb.*;
import eu.europa.esig.dss.diagnostic.CertificateWrapper;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import fr.minint.sgin.attestationvalidatorapi.utils.TextUtils;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

@Getter
@Setter
public class SignatureIdValidation extends AbstractValidation {
    private SignatureWrapper signatureWrapper;
    private CertificateWrapper certificateWrapper;
    private boolean isVerifyHashSignature;

    public SignatureIdValidation(String signatureId, DetailedReport detailedReport, DiagnosticData diagnosticData, boolean isVerifyHashSignatureParam) {
        XmlBasicBuildingBlocks basicBuildingBlocks = detailedReport.getBasicBuildingBlockById(signatureId);

        xmlConstraintsConclusionList = new ArrayList<>();
        isVerifyHashSignature = isVerifyHashSignatureParam;
        // Format checking
        xmlConstraintsConclusionList.add(basicBuildingBlocks.getFC());
        // Identification of the Signing Certificate
        xmlConstraintsConclusionList.add(basicBuildingBlocks.getISC());
        if (isVerifyHashSignature) {
            // Validation Context Initialization
            xmlConstraintsConclusionList.add(basicBuildingBlocks.getVCI());
        }
        // X509 Certificate Validation
        xmlConstraintsConclusionList.add(basicBuildingBlocks.getXCV());
        // Cryptographic Verification
        xmlConstraintsConclusionList.add(basicBuildingBlocks.getCV());
        // Signature Acceptance Validation
        xmlConstraintsConclusionList.add(basicBuildingBlocks.getSAV());
        // Signature policy ID wrapper
        this.signatureWrapper = diagnosticData.getSignatureById(signatureId);
        // Certificate signature wrapper
        this.certificateWrapper = diagnosticData.getSignatureById(signatureId).getSigningCertificate();
    }

    public boolean isValid(String signaturePolicyId, String organizationalUnit, String organizationIdentifier) {
        return areXmlConstraintsConclusionsValid() && isSignaturePolicyIdValid(signaturePolicyId) && isCertificateSignatureValid(organizationalUnit, organizationIdentifier);
    }

    private boolean isSignaturePolicyIdValid(String signaturePolicyId) {
        return isVerifyHashSignature ? signatureWrapper.getPolicyId().equals(signaturePolicyId) : true;
    }

    private boolean isCertificateSignatureValid(String organizationalUnit, String organizationIdentifier) {
        boolean isOrganizationalUnitMatch = StringUtils.isNotBlank(certificateWrapper.getOrganizationalUnit())
                && TextUtils.removeWhiteSpaces(certificateWrapper.getOrganizationalUnit()).startsWith(TextUtils.removeWhiteSpaces(organizationalUnit));
        boolean isOrganizationIdentifierMatch = StringUtils.isNotBlank(certificateWrapper.getOrganizationIdentifier())
                && TextUtils.removeWhiteSpaces(certificateWrapper.getOrganizationIdentifier()).startsWith(TextUtils.removeWhiteSpaces(organizationIdentifier));
        return isOrganizationalUnitMatch || isOrganizationIdentifierMatch;
    }
}
