package fr.minint.sgin.attestationvalidatorapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocsValidationDTO {
    List<DocValidationDTO> docValidationDTOList;
}
