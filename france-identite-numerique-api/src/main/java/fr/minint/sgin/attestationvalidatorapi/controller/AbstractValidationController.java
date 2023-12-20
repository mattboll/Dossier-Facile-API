package fr.minint.sgin.attestationvalidatorapi.controller;

import eu.europa.esig.dss.validation.reports.Reports;
import fr.minint.sgin.attestationvalidatorapi.config.FileConfig;
import fr.minint.sgin.attestationvalidatorapi.dto.AttributesDTO;
import fr.minint.sgin.attestationvalidatorapi.dto.DocValidationDTO;
import fr.minint.sgin.attestationvalidatorapi.enums.DocStatus;
import fr.minint.sgin.attestationvalidatorapi.enums.FieldAttribute;
import fr.minint.sgin.attestationvalidatorapi.exception.functional.ContentValidationException;
import fr.minint.sgin.attestationvalidatorapi.exception.functional.SignatureValidationException;
import fr.minint.sgin.attestationvalidatorapi.exception.functional.UnsupportedMediaException;
import fr.minint.sgin.attestationvalidatorapi.exception.technical.TechnicalException;
import fr.minint.sgin.attestationvalidatorapi.service.IContentValidationService;
import fr.minint.sgin.attestationvalidatorapi.service.ISignatureValidationService;
import fr.minint.sgin.attestationvalidatorapi.service.IValidationReportService;
import fr.minint.sgin.attestationvalidatorapi.utils.FileUtils;
import fr.minint.sgin.attestationvalidatorapi.utils.Messages;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

abstract class AbstractValidationController {

    private final ISignatureValidationService signatureValidationService;

    private final IValidationReportService validationReportsService;

    private final IContentValidationService contentValidationService;

    private final FileConfig fileConfig;

    protected AbstractValidationController(ISignatureValidationService signatureValidationService, IValidationReportService validationReportsService, IContentValidationService contentValidationService, FileConfig fileConfig) {
        this.signatureValidationService = signatureValidationService;
        this.validationReportsService = validationReportsService;
        this.contentValidationService = contentValidationService;
        this.fileConfig = fileConfig;
    }

    /**
     * Process document validation (signatures, timestamps and date validity not expired).
     *
     * @param file          The input file to validate
     * @param docResponse   Result of doc validation with the detailed report in xml
     * @param attributes    attributes to extract from pdf
     * @param allAttributes if extract all attributes
     * @throws SignatureValidationException
     * @throws UnsupportedMediaException
     * @throws IOException
     * @throws ContentValidationException
     */
    protected void checkDocValidity(MultipartFile file, DocValidationDTO docResponse, Set<FieldAttribute> attributes,
                                  boolean allAttributes)
            throws SignatureValidationException, ContentValidationException, UnsupportedMediaException, IOException {
        // Check file
        this.checkFile(file, docResponse);
        // Check signatures + timestamps
        boolean areSignatureTimestampValid;
        try {
            areSignatureTimestampValid = areSignaturesAndTimestampsValid(file, docResponse);
        } catch (Exception e) {
            throw new SignatureValidationException(Messages.SGIN_ERROR_SIGNATURE_VALIDATION);
        }
        // Check if date is valid and extract content
        if (areSignatureTimestampValid) {
            try (PDDocument doc = PDDocument.load(file.getBytes())) {
                checkDateValidAndExtractContent(doc, docResponse, attributes, allAttributes, areSignatureTimestampValid);
            } catch (Exception e) {
                throw new ContentValidationException(Messages.SGIN_ERROR_CONTENT_VALIDATION);
            }
        } else {
            docResponse.setStatus(DocStatus.NOT_VALID);
        }
    }

    /**
     * Check file : is present, mime-type, size
     * @param file
     * @param docResponse
     * @throws UnsupportedMediaException
     * @throws IOException
     */
    private void checkFile(MultipartFile file, DocValidationDTO docResponse) throws UnsupportedMediaException, IOException {
        // Check file present
        if (!FileUtils.isPresent(file)) {
            throw new UnsupportedMediaException(Messages.SGIN_NO_FILE);
        }
        docResponse.setFileName(file.getOriginalFilename());
        // Check mime-type and magic bytes PDF
        if (!FileUtils.isValidMimeTypePDF(file) || !FileUtils.isMagicBytesPDFPresent(file)) {
            throw new UnsupportedMediaException(Messages.SGIN_NOT_PDF);
        }
        // Check file size
        if (FileUtils.isFileTooLarge(file, fileConfig.getMaxFileSizeKb() * 1024)) {
            throw new UnsupportedMediaException(Messages.SGIN_FILE_TOO_LARGE);
        }
    }

    /**
     * Check if signatures and timestamps are valid
     * @param file
     * @param docResponse
     * @return
     * @throws Exception
     */
    private boolean areSignaturesAndTimestampsValid(MultipartFile file, DocValidationDTO docResponse) throws IOException {
        Reports reports = validationReportsService.getValidationReportsFromAttestation(file);
        if (fileConfig.isGetReports()) {
            docResponse.setXmlDetailedReport(Base64.getEncoder()
                    .encodeToString(reports.getXmlDetailedReport().getBytes(StandardCharsets.UTF_8)));
            docResponse.setXmlDiagnosticDataReport(Base64.getEncoder()
                    .encodeToString(reports.getXmlDiagnosticData().getBytes(StandardCharsets.UTF_8)));
        }
        return signatureValidationService.isAttestationSignatureTimestampValid(reports);
    }

    /**
     * Check if date valid and extract content
     * @param doc
     * @param docResponse
     * @param attributes
     * @param allAttributes
     * @param isSignatureTimestampValid
     * @throws TechnicalException
     */
    private void checkDateValidAndExtractContent(PDDocument doc, DocValidationDTO docResponse, Set<FieldAttribute> attributes, boolean allAttributes, boolean isSignatureTimestampValid) throws TechnicalException {
        // Retrieve attributes from PDF
        AttributesDTO attributesDTO;
        if (allAttributes) {
            attributesDTO = contentValidationService.getAttributesFromPDDocument(doc);
        } else {
            if (CollectionUtils.isEmpty(attributes)) {
                attributes = new HashSet<>();
            }
            attributes.add(FieldAttribute.VALIDITY_DATE);
            attributesDTO = contentValidationService.getAttributesFromPDDocument(doc, attributes);
        }
        boolean isDateValidNotExpire = contentValidationService.isDateValidNotExpire(attributesDTO.getValidityDate());
        docResponse.setAttributes(attributesDTO);
        docResponse.setValidDate(attributesDTO.getValidityDate());
        docResponse.setStatus(DocStatus.getStatusBySignatureAndContentResults(isSignatureTimestampValid, isDateValidNotExpire));
    }

}
