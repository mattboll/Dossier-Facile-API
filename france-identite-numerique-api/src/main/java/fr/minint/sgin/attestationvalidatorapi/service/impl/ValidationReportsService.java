package fr.minint.sgin.attestationvalidatorapi.service.impl;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.pades.validation.PDFDocumentValidator;
import eu.europa.esig.dss.pdf.IPdfObjFactory;
import eu.europa.esig.dss.pdf.modifications.DefaultPdfDifferencesFinder;
import eu.europa.esig.dss.pdf.modifications.DefaultPdfObjectModificationsFinder;
import eu.europa.esig.dss.validation.reports.Reports;
import fr.minint.sgin.attestationvalidatorapi.config.DSSBeanConfig;
import fr.minint.sgin.attestationvalidatorapi.service.IValidationReportService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ValidationReportsService implements IValidationReportService {

    private final DSSBeanConfig dssConfig;

    public ValidationReportsService(DSSBeanConfig dssConfig) {
        this.dssConfig = dssConfig;
    }

    /**
     * Get the DSS validation reports from the attestation
     * @param file
     * @return
     * @throws IOException
     */
    public Reports getValidationReportsFromAttestation(MultipartFile file) throws IOException {
        DSSDocument document = new InMemoryDocument(file.getBytes());
        PDFDocumentValidator documentValidator = new PDFDocumentValidator(document);
        documentValidator.setSignaturePolicyProvider(dssConfig.signaturePolicyProvider());
        documentValidator.setCertificateVerifier(dssConfig.certificateVerifier());

        // Initialize IPdfObjFactory
        // Note : example uses ServiceLoaderPdfObjFactory loading the available implementation in runtime.
        //        A custom implementation of IPdfObjFactory may be also provided, when applicable (e.g. PdfBoxNativeObjectFactory).
        IPdfObjFactory pdfObjFactory = dssConfig.pdfObjFactory();

        // tag::visual-change-finder[]
        // import eu.europa.esig.dss.pdf.modifications.DefaultPdfDifferencesFinder;

        DefaultPdfDifferencesFinder pdfDifferencesFinder = dssConfig.pdfDifferencesFinder();
        // The variable defines number of pages in a document to run the validation for
        // NOTE: setting '0' as MaximalPagesAmountForVisualComparison will skip the visual changes detection
        pdfDifferencesFinder.setMaximalPagesAmountForVisualComparison(1);
        // Provide a customized PdfDifferencesFinder within IPdfObjFactory
        pdfObjFactory.setPdfDifferencesFinder(pdfDifferencesFinder);
        // end::visual-change-finder[]

        // tag::object-modifications[]
        // import eu.europa.esig.dss.pdf.modifications.DefaultPdfObjectModificationsFinder;

        DefaultPdfObjectModificationsFinder pdfObjectModificationsFinder = dssConfig.pdfObjectModificationsFinder();
        // The variable defines a limit of the nested objects to be verified (in case of too big PDFs)
        // NOTE: setting '0' as MaximumObjectVerificationDeepness will skip the object modification detection
        pdfObjectModificationsFinder.setMaximumObjectVerificationDeepness(100);

        // Provide a customized PdfObjectModificationsFinder within IPdfObjFactory
        pdfObjFactory.setPdfObjectModificationsFinder(pdfObjectModificationsFinder);
        // end::object-modifications[]

        // Set the factory to the DocumentValidator
        documentValidator.setPdfObjFactory(pdfObjFactory);

        return documentValidator.validateDocument(new ClassPathResource("constraint.xml").getInputStream());
    }
}
