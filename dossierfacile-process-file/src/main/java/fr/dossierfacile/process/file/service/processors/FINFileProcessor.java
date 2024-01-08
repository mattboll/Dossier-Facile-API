package fr.dossierfacile.process.file.service.processors;

import fr.dossierfacile.common.entity.ParsedFileAnalysis;
import fr.dossierfacile.common.entity.ocr.ParsedFile;
import fr.dossierfacile.common.enums.ParsedFileAnalysisStatus;
import fr.dossierfacile.common.repository.ParsedFileAnalysisRepository;
import fr.dossierfacile.process.file.repository.FileRepository;
import fr.dossierfacile.process.file.service.AnalysisContext;
import fr.dossierfacile.process.file.service.parsers.FinParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class FINFileProcessor implements Processor {
    private final FileRepository fileRepository;
    private final ParsedFileAnalysisRepository parsedFileAnalysisRepository;
    private final FinParser finParser;

    public AnalysisContext process(AnalysisContext context) {
            try {
                ParsedFile parsedDocument = finParser.parse(context.getFile());
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
            } catch (Exception e) {
                log.warn("Unable to parse file {}", context.getDfFile().getId());
            }
        return context;
    }
}
