package fr.minint.sgin.attestationvalidatorapi.service;

import fr.minint.sgin.attestationvalidatorapi.dto.AttributesDTO;
import fr.minint.sgin.attestationvalidatorapi.enums.FieldAttribute;
import fr.minint.sgin.attestationvalidatorapi.exception.technical.TechnicalException;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.Set;

public interface IContentValidationService {

    AttributesDTO getAttributesFromPDDocument(PDDocument pdfDocument) throws TechnicalException;

    AttributesDTO getAttributesFromPDDocument(PDDocument pdfDocument, Set<FieldAttribute> attributes) throws TechnicalException;

    boolean isDateValidNotExpire(String dateValid) throws TechnicalException;
}
