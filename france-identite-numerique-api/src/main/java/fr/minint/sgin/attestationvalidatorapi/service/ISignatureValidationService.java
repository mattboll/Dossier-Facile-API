package fr.minint.sgin.attestationvalidatorapi.service;

import eu.europa.esig.dss.validation.reports.Reports;

public interface ISignatureValidationService {

    boolean isAttestationSignatureValid(Reports reports);

    boolean isAttestationTimestampValid(Reports reports);

    boolean isAttestationSignatureTimestampValid(Reports reports);
}
