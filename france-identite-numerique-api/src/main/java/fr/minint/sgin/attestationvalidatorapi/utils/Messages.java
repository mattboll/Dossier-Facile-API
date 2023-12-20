package fr.minint.sgin.attestationvalidatorapi.utils;


public class Messages {
    public static final String SGIN_NO_FILE = "No file present";
    public static final String SGIN_NOT_PDF = "The file is not a PDF";
    public static final String SGIN_FILE_TOO_LARGE = "The file is too large";
    public static final String SGIN_ERROR_SIGNATURE_VALIDATION = "Error during document signature validation";
    public static final String SGIN_ERROR_CONTENT_VALIDATION = "Error during document content validation";
    public static final String SGIN_NO_VALID_DATE = "Missing required date validation field";

    private Messages() {
	throw new IllegalStateException("Messages class");
    }
}
