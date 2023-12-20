package fr.minint.sgin.attestationvalidatorapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fr.minint.sgin.attestationvalidatorapi.enums.FieldAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.apache.pdfbox.cos.COSDictionary;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributesDTO {
    private String id;
    private String recipient;
    private String reason;
    @Schema(format = "dd/MM/YYYY")
    private String generatedDate;
    @Schema(format = "dd/MM/YYYY")
    private String validityDate;
    private String familyName;
    private String givenName;
    private String usageName;
    private String gender;
    private String nationality;
    @Schema(format = "dd/MM/YYYY")
    private String birthDate;
    private String birthPlace;

    public AttributesDTO(COSDictionary dictionaryContent, Set<FieldAttribute> attributes) {
        this.id = getFieldAttributeValue(FieldAttribute.ID, dictionaryContent, attributes);
        this.recipient = getFieldAttributeValue(FieldAttribute.RECIPIENT, dictionaryContent, attributes);
        this.reason = getFieldAttributeValue(FieldAttribute.REASON, dictionaryContent, attributes);
        this.generatedDate = getFieldAttributeValue(FieldAttribute.GENERATED_DATE, dictionaryContent, attributes);
        this.validityDate = getFieldAttributeValue(FieldAttribute.VALIDITY_DATE, dictionaryContent, attributes);
        this.familyName = getFieldAttributeValue(FieldAttribute.FAMILY_NAME, dictionaryContent, attributes);
        this.givenName = getFieldAttributeValue(FieldAttribute.GIVEN_NAME, dictionaryContent, attributes);
        this.usageName = getFieldAttributeValue(FieldAttribute.USAGE_NAME, dictionaryContent, attributes);
        this.gender = getFieldAttributeValue(FieldAttribute.GENDER, dictionaryContent, attributes);
        this.nationality = getFieldAttributeValue(FieldAttribute.NATIONALITY, dictionaryContent, attributes);
        this.birthDate = getFieldAttributeValue(FieldAttribute.BIRTH_DATE, dictionaryContent, attributes);
        this.birthPlace = getFieldAttributeValue(FieldAttribute.BIRTH_PLACE, dictionaryContent, attributes);
    }

    /**
     * Retrieve the field value from the field list if required attribute
     * @param fieldAttribute
     * @param dictionaryContent
     * @param attributes
     * @return
     */
    private String getFieldAttributeValue(FieldAttribute fieldAttribute, COSDictionary dictionaryContent, Set<FieldAttribute> attributes) {
        if (attributes.contains(fieldAttribute)) {
            return dictionaryContent.getEmbeddedString("sgin_content", fieldAttribute.getFieldName());
        }
        return null;
    }
}
