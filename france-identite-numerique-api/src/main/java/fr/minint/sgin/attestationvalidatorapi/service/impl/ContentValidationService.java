package fr.minint.sgin.attestationvalidatorapi.service.impl;

import fr.minint.sgin.attestationvalidatorapi.dto.AttributesDTO;
import fr.minint.sgin.attestationvalidatorapi.enums.FieldAttribute;
import fr.minint.sgin.attestationvalidatorapi.exception.technical.TechnicalException;
import fr.minint.sgin.attestationvalidatorapi.service.IContentValidationService;
import fr.minint.sgin.attestationvalidatorapi.utils.Messages;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContentValidationService implements IContentValidationService {

    private static final DateTimeFormatter DATE_DD_MM_YYYY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Get the date valid from the attestation pdf
     * @param pdfDocument
     * @return
     * @throws IOException
     */
    public String getDateValid(PDDocument pdfDocument) throws TechnicalException {
        AttributesDTO attributesDTO = getAttributesFromPDDocument(pdfDocument);
        if (StringUtils.isBlank(attributesDTO.getValidityDate())) {
            throw new TechnicalException(Messages.SGIN_NO_VALID_DATE);
        }
        return attributesDTO.getValidityDate();
    }

    /**
     * Get attributes from the attestation pdf
     * @param pdfDocument
     * @return
     */
    public AttributesDTO getAttributesFromPDDocument(PDDocument pdfDocument) {
        return getAttributesFromPDDocument(pdfDocument, Arrays.stream(FieldAttribute.values()).collect(Collectors.toSet()));
    }

    /**
     * Get attributes from the attestation pdf
     * @param pdfDocument
     * @return
     */
    public AttributesDTO getAttributesFromPDDocument(PDDocument pdfDocument, Set<FieldAttribute> attributes) {
        return new AttributesDTO(pdfDocument.getPage(0).getCOSObject(), attributes);
    }

    /**
     * Check if the date valid is expired
     * @param dateValid
     * @return
     */
    public boolean isDateValidNotExpire(String dateValid) throws TechnicalException {
        if (StringUtils.isBlank(dateValid)) {
            throw new TechnicalException(Messages.SGIN_NO_VALID_DATE);
        }
        LocalDate date = LocalDate.parse(dateValid, DATE_DD_MM_YYYY_FORMATTER);
        LocalDate now = LocalDate.now();
        return date.isAfter(now) || date.isEqual(now);
    }
}
