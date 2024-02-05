package fr.minint.sgin.attestationvalidatorapi.controller;

import fr.minint.sgin.attestationvalidatorapi.config.FileConfig;
import fr.minint.sgin.attestationvalidatorapi.dto.DocValidationDTO;
import fr.minint.sgin.attestationvalidatorapi.dto.DocsValidationDTO;
import fr.minint.sgin.attestationvalidatorapi.enums.DocStatus;
import fr.minint.sgin.attestationvalidatorapi.enums.FieldAttribute;
import fr.minint.sgin.attestationvalidatorapi.exception.functional.ContentValidationException;
import fr.minint.sgin.attestationvalidatorapi.exception.functional.UnsupportedMediaException;
import fr.minint.sgin.attestationvalidatorapi.service.IContentValidationService;
import fr.minint.sgin.attestationvalidatorapi.service.ISignatureValidationService;
import fr.minint.sgin.attestationvalidatorapi.service.IValidationReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;

@Tag(name = "AttestationValidationController", description = "API attestation validation")
@Validated
@RestController
@RequestMapping("/validation/v1")
@Log4j2
public class AttestationValidationController extends AbstractValidationController {

    public AttestationValidationController(ISignatureValidationService signatureValidationService,
                                           IValidationReportService validationReportsService, IContentValidationService contentValidationService,
                                           FileConfig fileConfig) {
        super(signatureValidationService, validationReportsService, contentValidationService, fileConfig);
    }

    /**
     * Check if the document is valid
     *
     * @param request
     * @param file PDF file used for validation
     * @param attributes
     * @param allAttributes
     * @return
     */
    @PostMapping(value = "/check-doc-valid", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Returns the status of validation for a single attestation")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Result of the validation")})
    public ResponseEntity<DocValidationDTO> checkDocValid(HttpServletRequest request,
                                                          @Parameter(description = "pdf file to validate") @RequestPart("file") @NotNull MultipartFile file,
                                                          @Parameter(description = "attributes to extract from the pdf content") @RequestParam(required = false, value = "attributes") Set<FieldAttribute> attributes,
                                                          @Parameter(description = "if extract all content from pdf, override 'attributes' parameter") @RequestParam(required = false, value = "all-attributes") boolean allAttributes) {
        log.info("Start document validation");
        DocValidationDTO docResponse = new DocValidationDTO();
        try {
            checkDocValidity(file, docResponse, attributes, allAttributes);
        } catch (Exception e) {
            log.warn(e.getMessage());
            docResponse.setMessage(e.getMessage());
            if (e instanceof UnsupportedMediaException || e instanceof IOException) {
                docResponse.setStatus(DocStatus.ERROR_FILE);
            } else if (e instanceof ContentValidationException) {
                docResponse.setStatus(DocStatus.ERROR_CONTENT);
            } else {
                docResponse.setStatus(DocStatus.NOT_VALID);
            }
        }
        return new ResponseEntity<>(docResponse, HttpStatus.OK);
    }

    /**
     * Check if documents are valid
     *
     * @param request
     * @param files PDF files used for validation
     * @param attributes
     * @param allAttributes
     * @return
     */
    @PostMapping(value = "/check-docs-valid", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Returns the status of validation for multiple attestations")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Result of validations")})
    public ResponseEntity<DocsValidationDTO> checkDocsValid(HttpServletRequest request,
                                                            @Parameter(description = "pdf files to validate") @RequestPart("file") @NotEmpty List<MultipartFile> files,
                                                            @Parameter(description = "attributes to extract from the pdf content") @RequestParam(required = false, value = "attributes") Set<FieldAttribute> attributes,
                                                            @Parameter(description = "if extract all content from pdf, override 'attributes' parameter") @RequestParam(required = false, value = "all-attributes") boolean allAttributes) {
        log.info("Start documents validation");
        List<DocValidationDTO> docRespList = new ArrayList<>();
        for (MultipartFile file : files) {
            DocValidationDTO docResponse = new DocValidationDTO();
            try {
                checkDocValidity(file, docResponse, attributes, allAttributes);
            } catch (Exception e) {
                log.warn(e.getMessage());
                docResponse.setMessage(e.getMessage());
                if (e instanceof UnsupportedMediaException || e instanceof IOException) {
                    docResponse.setStatus(DocStatus.ERROR_FILE);
                } else if (e instanceof ContentValidationException) {
                    docResponse.setStatus(DocStatus.ERROR_CONTENT);
                } else {
                    docResponse.setStatus(DocStatus.NOT_VALID);
                }
            }
            docRespList.add(docResponse);
        }
        return new ResponseEntity<>(new DocsValidationDTO(docRespList), HttpStatus.OK);
    }
}
