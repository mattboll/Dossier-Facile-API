package fr.minint.sgin.attestationvalidatorapi.utils;

import org.apache.commons.lang3.StringUtils;

public class TextUtils {

    private TextUtils() {}

    /**
     * Remove white spaces from the text
     * @param textToFormat
     * @return
     */
    public static String removeWhiteSpaces(String textToFormat) {
        if (StringUtils.isNotEmpty(textToFormat)) {
            return textToFormat.replaceAll("\\s+", "");
        }
        return textToFormat;
    }
}
