package fr.dossierfacile.process.file.service;

import fr.dossierfacile.process.file.repository.FileRepository;
import fr.dossierfacile.process.file.service.processors.BarCodeFileProcessor;
import fr.dossierfacile.process.file.service.processors.FileParserProcessor;
import fr.dossierfacile.process.file.service.processors.FINFileProcessor;
import fr.dossierfacile.process.file.service.processors.LoadFileProcessor;
import fr.dossierfacile.process.file.service.processors.OcrParserFileProcessor;
import fr.dossierfacile.process.file.util.FINFileAnalysisCriteria;
import fr.dossierfacile.process.file.util.FileParsingEligibilityCriteria;
import fr.dossierfacile.process.file.util.QrCodeFileAnalysisCriteria;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class AnalyzeFile {
    private final BarCodeFileProcessor barCodeFileProcessor;
    private final FileParserProcessor fileParserProcessor;
//    private final OcrParserFileProcessor ocrParserFileProcessor;
    private final FINFileProcessor finFileProcessor;
    private final FileRepository fileRepository;

    public void processFile(Long fileId) {
        Optional.ofNullable(fileRepository.findById(fileId).orElse(null))
                .filter(Objects::nonNull)
                .map(barCodeFileProcessor::process)
                .map(fileParserProcessor::process);

//        AnalysisContext context = new AnalysisContext();
//
//        Optional.of(context)
//                .map(ctx -> {
//                    ctx.setDfFile(fileRepository.findById(fileId).orElse(null));
//                    return ctx;
//                })
//                .filter(ctx -> QrCodeFileAnalysisCriteria.shouldBeAnalyzed(ctx.getDfFile()))
//                .map(barCodeFileProcessor::process);
//
//        Optional.of(context)
//                .filter(ctx -> FileParsingEligibilityCriteria.shouldBeParse(ctx.getDfFile()))
//                .map(loadFileProcessor::process)
//                .map(ocrParserFileProcessor::process)
//                .map(loadFileProcessor::cleanContext);

//        Optional.of(context)
//                .filter(ctx -> FINFileAnalysisCriteria.shouldBeAnalyzed(ctx.getDfFile()))
//                .map(loadFileProcessor::process)
//                .map(finFileProcessor::process)
//                .map(loadFileProcessor::cleanContext);
    }
}
