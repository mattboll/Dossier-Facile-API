package fr.dossierfacile.common.enums;

import fr.dossierfacile.common.entity.FINApiResult;
import fr.dossierfacile.common.entity.ocr.GuaranteeProviderFile;
import fr.dossierfacile.common.entity.ocr.ParsedFile;
import fr.dossierfacile.common.entity.ocr.PublicPayslipFile;
import fr.dossierfacile.common.entity.ocr.TaxIncomeLeaf;
import fr.dossierfacile.common.entity.ocr.TaxIncomeMainFile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ParsedFileClassification {
    TAX_INCOME(TaxIncomeMainFile.class),
    TAX_INCOME_LEAF(TaxIncomeLeaf.class),
    GUARANTEE_PROVIDER(GuaranteeProviderFile.class),
    PUBLIC_PAYSLIP(PublicPayslipFile.class),
    RENTAL_RECEIPT(RentalReceiptFile.class),
    FRANCE_IDENTITE_NUMERIQUE(FINApiResult.class);

    Class<? extends ParsedFile> classificationClass;
}
