package fr.minint.sgin.attestationvalidatorapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fr.minint.sgin.attestationvalidatorapi.enums.DocStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocValidationDTO {
    private String fileName;
    private DocStatus status;
    @Schema(format = "dd/MM/YYYY")
    private String validDate;
    private AttributesDTO attributes;
    private String message;
    @Schema(format = "Encoded in Base64")
    private String xmlDetailedReport;
    @Schema(format = "Encoded in Base64")
    private String xmlDiagnosticDataReport;
}
