package fr.dossierfacile.process.file.service.documentrules;

import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.entity.DocumentAnalysisReport;
import fr.dossierfacile.common.entity.DocumentAnalysisStatus;
import fr.dossierfacile.common.entity.DocumentBrokenRule;
import fr.dossierfacile.common.entity.DocumentRule;
import fr.dossierfacile.common.entity.File;
import fr.dossierfacile.common.entity.FranceIdentiteApiResult;
import fr.dossierfacile.common.entity.ParsedFileAnalysis;
import fr.dossierfacile.common.entity.Person;
import fr.dossierfacile.common.entity.ocr.TaxIncomeMainFile;
import fr.dossierfacile.common.enums.ParsedFileAnalysisStatus;
import fr.dossierfacile.common.enums.ParsedFileClassification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static fr.dossierfacile.common.enums.DocumentSubCategory.FRANCE_IDENTITE;
import static fr.dossierfacile.process.file.util.NameUtil.normalizeName;

@Service
@RequiredArgsConstructor
@Slf4j
public class FranceIdentiteNumeriqueRulesValidationService implements RulesValidationService {

    @Override
    public boolean shouldBeApplied(Document document) {
        return document.getDocumentSubCategory() == FRANCE_IDENTITE;
    }

    @Override
    public DocumentAnalysisReport process(Document document, DocumentAnalysisReport report) {
        List<DocumentBrokenRule> brokenRules = Optional.ofNullable(report.getBrokenRules())
                .orElseGet(() -> {
                    report.setBrokenRules(new LinkedList<>());
                    return report.getBrokenRules();
                });
        for (File dfFile : document.getFiles()) {
            ParsedFileAnalysis analysis = dfFile.getParsedFileAnalysis();
            if (analysis == null || analysis.getAnalysisStatus() == ParsedFileAnalysisStatus.FAILED) {
                continue;
            }
            if (analysis.getClassification() == ParsedFileClassification.FRANCE_IDENTITE_NUMERIQUE) {
                FranceIdentiteApiResult parsedDocument = (FranceIdentiteApiResult) analysis.getParsedFile();

                // Parse Rule
                if (parsedDocument == null
                        || parsedDocument.getStatus() == null
                        || parsedDocument.getFamilyName() == null
                        || parsedDocument.getGivenName() == null
                        || parsedDocument.getValidityDate() == null) {
                    brokenRules.add(DocumentBrokenRule.builder()
                            .rule(DocumentRule.R_FRANCE_IDENTITE_STATUS)
                            .message(DocumentRule.R_FRANCE_IDENTITE_STATUS.getDefaultMessage())
                            .build());
                    continue;
                }

                // Fake Rule
                if (!("VALID".equals(parsedDocument.getStatus()))) {
                    brokenRules.add(DocumentBrokenRule.builder()
                            .rule(DocumentRule.R_FRANCE_IDENTITE_STATUS)
                            .message(DocumentRule.R_FRANCE_IDENTITE_STATUS.getDefaultMessage())
                            .build());
                    continue;
                }

                // TODO : check that France Identité verifies that names on pdf matches qrcode
                Person documentOwner = Optional.ofNullable((Person) document.getTenant()).orElseGet(document::getGuarantor);
                String firstName = documentOwner.getFirstName();
                String lastName = document.getName();
                if (!(normalizeName(parsedDocument.getGivenName()).contains(normalizeName(firstName))
                        && (normalizeName(parsedDocument.getFamilyName()).contains(normalizeName(lastName)))
                )) {
                    log.error("Le nom/prenom ne correpond pas à l'utilisateur tenantId:" + document.getTenant().getId() + " firstname: " + firstName);
                    brokenRules.add(DocumentBrokenRule.builder()
                            .rule(DocumentRule.R_FRANCE_IDENTITE_NAMES)
                            .message(DocumentRule.R_FRANCE_IDENTITE_NAMES.getDefaultMessage())
                            .build());
                }
            }
        }
        if (brokenRules.isEmpty()) {
            report.setAnalysisStatus(DocumentAnalysisStatus.CHECKED);
        } else if (brokenRules.stream().anyMatch(r -> r.getRule().getLevel() == DocumentRule.Level.CRITICAL)) {
            report.setAnalysisStatus(DocumentAnalysisStatus.DENIED);
        } else {
            report.setAnalysisStatus(DocumentAnalysisStatus.UNDEFINED);
        }
        return report;
    }
}