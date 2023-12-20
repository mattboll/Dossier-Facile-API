package fr.minint.sgin.attestationvalidatorapi.enums;

import lombok.Getter;

@Getter
public enum FieldAttribute {
    ID("id"),
    RECIPIENT("recipient"),
    REASON("reason"),
    GENERATED_DATE("todayDate"),
    VALIDITY_DATE("validityDate"),
    FAMILY_NAME("familyName"),
    GIVEN_NAME("givenName"),
    USAGE_NAME("usageName"),
    GENDER("gender"),
    NATIONALITY("nationality"),
    BIRTH_DATE("birthdate"),
    BIRTH_PLACE("birthplace");

    private String fieldName;

    FieldAttribute(String fieldName) {
        this.fieldName = fieldName;
    }
}
