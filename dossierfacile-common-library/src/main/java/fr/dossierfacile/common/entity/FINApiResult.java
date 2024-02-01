package fr.dossierfacile.common.entity;

import fr.dossierfacile.common.entity.ocr.ParsedFile;
import fr.dossierfacile.common.enums.ParsedFileClassification;
import fr.dossierfacile.common.enums.ParsedStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@NoArgsConstructor
//@AllArgsConstructor
public class FINApiResult implements ParsedFile {
    @Builder.Default
    ParsedFileClassification classification = ParsedFileClassification.FRANCE_IDENTITE_NUMERIQUE;
    ParsedStatus status;
    String apiStatus;
    String lastname;
    String firstname;
    String validityDate;
}
