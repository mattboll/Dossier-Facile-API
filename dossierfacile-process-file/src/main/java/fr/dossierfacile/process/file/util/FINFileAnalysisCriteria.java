package fr.dossierfacile.process.file.util;

import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.entity.File;
import fr.dossierfacile.common.enums.DocumentCategory;
import org.springframework.http.MediaType;

import static fr.dossierfacile.common.enums.DocumentSubCategory.FRANCE_ID_NUM;

public class FINFileAnalysisCriteria {

    public static boolean shouldBeAnalyzed(File file) {
        if (file == null)
            return false;
        Document document = file.getDocument();
        if ((document.getDocumentCategory() == DocumentCategory.IDENTIFICATION
                || document.getDocumentCategory() == DocumentCategory.IDENTIFICATION_LEGAL_PERSON )
                && document.getDocumentSubCategory() == FRANCE_ID_NUM) {
            return MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(file.getStorageFile().getContentType());
        }
        return false;
    }
}
