package fr.dossierfacile.process.file.service.processors;

import fr.dossierfacile.common.entity.File;
import fr.dossierfacile.common.entity.ParsedFileAnalysis;
import fr.dossierfacile.common.repository.ParsedFileAnalysisRepository;
import fr.dossierfacile.common.service.interfaces.FileStorageService;
import fr.dossierfacile.process.file.barcode.InMemoryFile;
import fr.dossierfacile.process.file.repository.FileRepository;
import fr.dossierfacile.process.file.service.parsers.FinParser;
import fr.dossierfacile.process.file.service.qrcodeanalysis.DocumentClassifier;
import fr.dossierfacile.process.file.util.FINFileAnalysisCriteria;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class FINFileProcessor implements Processor {
    private final FileRepository fileRepository;
    private final ParsedFileAnalysisRepository parsedFileAnalysisRepository;
    private final FinParser finParser;
    private final FileStorageService fileStorageService;

    @Override
    public File process(File file) {
        if (FINFileAnalysisCriteria.shouldBeAnalyzed(file) &&
                parsedFileAnalysisRepository.findByFileId(file.getId()).isEmpty()) {
            long start = System.currentTimeMillis();
            log.info("Starting analysis of file");
            downloadAndAnalyze(file).map(analysis -> save(file, analysis));
            log.info("Analysis of file finished in {} ms", System.currentTimeMillis() - start);
        }
        return file;
    }

    private Optional<ParsedFileAnalysis> downloadAndAnalyze(File file) {
        try (InMemoryFile inMemoryFile = InMemoryFile.download(file, fileStorageService)) {
            return analyze(inMemoryFile)
                    .map(analysis -> {
                        boolean isAllowed = new DocumentClassifier(analysis.getDocumentType()).isCompatibleWith(file);
                        analysis.setAllowedInDocumentCategory(isAllowed);
                        return analysis;
                    });
        } catch (Exception e) {
            log.error("Unable to download file", e);
        }
        return Optional.empty();
    }

    private Optional<FINFileAnalysis> analyze(InMemoryFile file) {
        if (file.hasQrCode()) {
            return qrCodeFileAuthenticator.analyze(file);
        }

        if (file.has2DDoc()) {
            return Optional.of(twoDDocFileAuthenticator.analyze(file.get2DDoc()));
        }

        return Optional.empty();
    }

    private ParsedFileAnalysis save(File file, ParsedFileAnalysis analysis) {
        analysis.setFile(file);
        return parsedFileAnalysisRepository.save(analysis);
    }


    //    public AnalysisContext process(AnalysisContext context) {
//            try {
//                ParsedFile parsedDocument = finParser.parse(context.getFile());
//                ParsedFileAnalysis parsedFileAnalysis = ParsedFileAnalysis.builder()
//                        .analysisStatus(ParsedFileAnalysisStatus.COMPLETED)
//                        .parsedFile(parsedDocument)
//                        .classification(parsedDocument.getClassification())
//                        .build();
//
//                parsedFileAnalysis.setFile(context.getDfFile());
//                parsedFileAnalysisRepository.save(parsedFileAnalysis);
//                context.getDfFile().setParsedFileAnalysis(parsedFileAnalysis);
//                fileRepository.save(context.getDfFile());
//                log.info("Successfully parse file {}", context.getDfFile().getId());
//            } catch (Exception e) {
//                log.warn("Unable to parse file {}", context.getDfFile().getId());
//            }
//        return context;
//    }
}
