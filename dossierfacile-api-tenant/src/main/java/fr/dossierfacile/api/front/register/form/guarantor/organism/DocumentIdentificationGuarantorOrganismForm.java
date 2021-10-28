package fr.dossierfacile.api.front.register.form.guarantor.organism;

import fr.dossierfacile.api.front.register.form.guarantor.DocumentGuarantorFormAbstract;
import fr.dossierfacile.api.front.validator.anotation.NumberOfPages;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.TypeGuarantor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@NumberOfPages(category = DocumentCategory.IDENTIFICATION, max = 10)
public class DocumentIdentificationGuarantorOrganismForm extends DocumentGuarantorFormAbstract {

    private TypeGuarantor typeGuarantor = TypeGuarantor.ORGANISM;

    private DocumentCategory documentCategory = DocumentCategory.IDENTIFICATION;
}
