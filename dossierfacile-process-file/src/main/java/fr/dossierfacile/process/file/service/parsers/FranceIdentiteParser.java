package fr.dossierfacile.process.file.service.parsers;

import fr.dossierfacile.common.entity.FranceIdentiteApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.File;

import static fr.dossierfacile.common.enums.DocumentSubCategory.FRANCE_IDENTITE;

@Service
@Slf4j
@RequiredArgsConstructor
public class FranceIdentiteParser implements FileParser<FranceIdentiteApiResult> {

    @Override
    public FranceIdentiteApiResult parse(File file) {
        FranceIdentiteApiResult result = FranceIdentiteApiResult.builder().build();
        try (PDDocument document = Loader.loadPDF(file)) {
            return result;
        } catch (Exception e) {
            log.error("Unable to parse");
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean shouldTryToApply(fr.dossierfacile.common.entity.File file) {
        return file.getDocument().getDocumentSubCategory() == FRANCE_IDENTITE
                && MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(file.getStorageFile().getContentType());
    }
}