package fr.minint.sgin.attestationvalidatorapi.service;

import eu.europa.esig.dss.validation.reports.Reports;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IValidationReportService {

    Reports getValidationReportsFromAttestation(MultipartFile file) throws IOException;
}
