package fr.dossierfacile.common.enums;

import lombok.Getter;

@Getter
public enum DocumentSubCategory {
    FRENCH_IDENTITY_CARD,
    FRENCH_PASSPORT,
    FRENCH_RESIDENCE_PERMIT,
    OTHER_IDENTIFICATION,
    CERTIFICATE_VISA,

    TENANT,
    OWNER,
    GUEST_PARENTS,
    GUEST,

    CDI,
    CDI_TRIAL,
    CDD,
    ALTERNATION,
    INTERNSHIP,
    STUDENT,
    PUBLIC,
    CTT,
    RETIRED,
    UNEMPLOYED,
    INDEPENDENT,
    OTHER,

    SALARY,
    SCHOLARSHIP,
    SOCIAL_SERVICE,
    RENT,
    PENSION,
    NO_INCOME,

    MY_NAME,
    MY_PARENTS,
    LESS_THAN_YEAR,
    OTHER_TAX,

    LEGAL_PERSON,
    UNDEFINED,
    OTHER_PROFESSION_GUARANTOR
}