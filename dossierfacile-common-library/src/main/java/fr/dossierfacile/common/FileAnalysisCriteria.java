package fr.dossierfacile.common;

import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.entity.File;
import fr.dossierfacile.common.enums.DocumentSubCategory;

import java.util.Arrays;

import static fr.dossierfacile.common.enums.DocumentSubCategory.MY_NAME;
import static fr.dossierfacile.common.enums.DocumentSubCategory.SALARY;
import static fr.dossierfacile.common.enums.DocumentSubCategory.SCHOLARSHIP;
import static fr.dossierfacile.common.enums.DocumentSubCategory.SOCIAL_SERVICE;
import static fr.dossierfacile.common.enums.DocumentSubCategory.STUDENT;
import static fr.dossierfacile.common.enums.DocumentSubCategory.UNEMPLOYED;

public class FileAnalysisCriteria {

    public static boolean shouldBeAnalyzed(File file) {
        Document document = file.getDocument();
        return switch (document.getDocumentCategory()) {
            case TAX -> hasSubCategory(document, MY_NAME);
            case PROFESSIONAL -> hasSubCategory(document, STUDENT, UNEMPLOYED);
            case FINANCIAL -> hasSubCategory(document, SALARY, SCHOLARSHIP, SOCIAL_SERVICE);
            case IDENTIFICATION, RESIDENCY, IDENTIFICATION_LEGAL_PERSON, NULL -> false;
        };
    }

    private static boolean hasSubCategory(Document document, DocumentSubCategory... subCategories) {
        return Arrays.asList(subCategories).contains(document.getDocumentSubCategory());
    }

}