package fr.dossierfacile.process.file.service.processors;

import fr.dossierfacile.common.entity.File;
import fr.dossierfacile.common.entity.ParsedFileAnalysis;
import fr.dossierfacile.common.entity.ocr.ParsedFile;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.ParsedFileAnalysisStatus;
import fr.dossierfacile.common.repository.ParsedFileAnalysisRepository;
import fr.dossierfacile.process.file.repository.FileRepository;
import fr.dossierfacile.process.file.service.AnalysisContext;
import fr.dossierfacile.process.file.service.ocr.GuaranteeVisaleParser;
import fr.dossierfacile.process.file.service.ocr.OcrParser;
import fr.dossierfacile.process.file.service.ocr.TaxIncomeLeafParser;
import fr.dossierfacile.process.file.service.ocr.TaxIncomeParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fr.dossierfacile.common.enums.DocumentSubCategory.CERTIFICATE_VISA;

@Slf4j
@Service
@AllArgsConstructor
public class OcrParserFileProcessor implements Processor {
    private final FileRepository fileRepository;
    private final ParsedFileAnalysisRepository parsedFileAnalysisRepository;
    private final TaxIncomeParser taxIncomeParser;
    private final TaxIncomeLeafParser taxIncomeLeafParser;
    private final GuaranteeVisaleParser guaranteeVisaleParser;

    /**
     * Gets configured parsers list for the specified type of dffile
     */
    private List<OcrParser> getParsers(File file) {
        if (file.getDocument().getDocumentCategory() == DocumentCategory.TAX) {
            return Arrays.asList(taxIncomeParser, taxIncomeLeafParser);
        }
        if (file.getDocument().getDocumentCategory() == DocumentCategory.IDENTIFICATION
                && file.getDocument().getDocumentSubCategory() == CERTIFICATE_VISA)
            return Collections.singletonList(guaranteeVisaleParser);
        return null;
    }

    public AnalysisContext process(AnalysisContext context) {
        List<OcrParser> parsers = getParsers(context.getDfFile());
        if (CollectionUtils.isEmpty(parsers)) {
            log.error("There is not parser associateed to this kind of document - configuration error");
            return context;
        }
        for (OcrParser parser : parsers) {
            try {
                ParsedFile parsedDocument = parser.parse(context.getFile());
                ParsedFileAnalysis parsedFileAnalysis = ParsedFileAnalysis.builder()
                        .analysisStatus(ParsedFileAnalysisStatus.COMPLETED)
                        .parsedFile(parsedDocument)
                        .classification(parsedDocument.getClassification())
                        .build();

                parsedFileAnalysis.setFile(context.getDfFile());
                parsedFileAnalysisRepository.save(parsedFileAnalysis);
                context.getDfFile().setParsedFileAnalysis(parsedFileAnalysis);
                fileRepository.save(context.getDfFile());
                log.info("Successfully parse file {}", context.getDfFile().getId());
                break;
            } catch (Exception e) {
                log.warn("Unable to parse file {}", context.getDfFile().getId());
            }
        }
        return context;
    }
}