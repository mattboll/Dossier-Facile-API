package fr.minint.sgin.attestationvalidatorapi.utils;

import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    private FileUtils() {}

    private static final MimeType MIME_TYPE_APPLICATION_PDF = new MimeType("application", "pdf");

    /**
     * Check if the file is present
     * @param multipartFile
     * @return
     */
    public static boolean isPresent(MultipartFile multipartFile) {
        return !multipartFile.isEmpty();
    }

    /**
     * Check the file size doesn't exceed the max file size
     * @param file
     * @param maxSize
     * @return
     */
    public static boolean isFileTooLarge(MultipartFile file, Long maxSize) {
        return file.getSize() > maxSize;
    }

    /**
     * Check if the mime-type file is PDF
     * @param multipartFile
     * @return
     */
    public static boolean isValidMimeTypePDF(MultipartFile multipartFile) {
        return multipartFile.getContentType() != null && MimeTypeUtils.parseMimeType(multipartFile.getContentType()).equals(MIME_TYPE_APPLICATION_PDF);
    }

    /**
     * Get the first 5 bytes of file signature.
     * Check if the file signature matches the magic bytes PDF.
     * @param multipartFile
     * @return
     * @throws IOException
     */
    public static boolean isMagicBytesPDFPresent(MultipartFile multipartFile) throws IOException {
        try (InputStream inStream = multipartFile.getInputStream()) {
            byte[] bytes = new byte[5];
            int readCount = inStream.read(bytes, 0, 5);
            if (readCount == 5) {
                return Byte.toUnsignedInt(bytes[0]) == 0x25 &&
                        Byte.toUnsignedInt(bytes[1]) == 0x50 &&
                        Byte.toUnsignedInt(bytes[2]) == 0x44 &&
                        Byte.toUnsignedInt(bytes[3]) == 0x46 &&
                        Byte.toUnsignedInt(bytes[4]) == 0x2D;
            }
        }
        return false;
    }
}
