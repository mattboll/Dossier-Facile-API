package fr.minint.sgin.attestationvalidatorapi.enums;

public enum DocStatus {
    VALID,
    NOT_VALID,
    ERROR_FILE,
    ERROR_CONTENT,
    EXPIRED;

    /**
     * Return the validation attestation status depending on the signature and the valid date results
     * @param isSignatureTimestampValid
     * @param isDateValidNotExpire
     * @return
     */
    public static DocStatus getStatusBySignatureAndContentResults(boolean isSignatureTimestampValid, boolean isDateValidNotExpire) {
        if (isSignatureTimestampValid) {
            if (isDateValidNotExpire) {
                return DocStatus.VALID;
            } else {
                return DocStatus.EXPIRED;
            }
        } else {
            return DocStatus.NOT_VALID;
        }
    }
}
