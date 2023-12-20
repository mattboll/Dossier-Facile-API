package fr.minint.sgin.attestationvalidatorapi.entity;

import eu.europa.esig.dss.detailedreport.jaxb.XmlConstraintsConclusion;
import eu.europa.esig.dss.enumerations.Indication;

import java.util.List;

abstract class AbstractValidation {
    protected List<XmlConstraintsConclusion> xmlConstraintsConclusionList;

    protected boolean areXmlConstraintsConclusionsValid() {
        return xmlConstraintsConclusionList.stream().allMatch(indication -> indication.getConclusion().getIndication().equals(Indication.PASSED));
    }
}
