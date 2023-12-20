package fr.minint.sgin.attestationvalidatorapi.entity;

import eu.europa.esig.dss.detailedreport.DetailedReport;
import eu.europa.esig.dss.detailedreport.jaxb.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class TimestampIdValidation extends AbstractValidation {

    public TimestampIdValidation(String timestampId, DetailedReport detailedReport) {
        XmlBasicBuildingBlocks basicBuildingBlocks = detailedReport.getBasicBuildingBlockById(timestampId);
        xmlConstraintsConclusionList = new ArrayList<>();
        // Identification of the Signing Certificate
        xmlConstraintsConclusionList.add(basicBuildingBlocks.getISC());
        // X509 Certificate Validation
        xmlConstraintsConclusionList.add(basicBuildingBlocks.getXCV());
        // Cryptographic Verification
        xmlConstraintsConclusionList.add(basicBuildingBlocks.getCV());
        // Signature Acceptance Validation
        xmlConstraintsConclusionList.add(basicBuildingBlocks.getSAV());
    }

    public boolean isValid() {
        return areXmlConstraintsConclusionsValid();
    }
}
