package fr.minint.sgin.attestationvalidatorapi.utils;

import eu.europa.esig.dss.detailedreport.DetailedReportFacade;
import eu.europa.esig.dss.detailedreport.jaxb.XmlDetailedReport;
import eu.europa.esig.dss.diagnostic.DiagnosticDataFacade;
import eu.europa.esig.dss.diagnostic.jaxb.XmlDiagnosticData;
import eu.europa.esig.dss.simplereport.SimpleReportFacade;
import eu.europa.esig.dss.simplereport.jaxb.XmlSimpleReport;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.validationreport.ValidationReportFacade;
import eu.europa.esig.validationreport.jaxb.ValidationReportType;

import java.io.File;

public class ReportsUtils {

    private static Reports generateReportsFromXml(String folder) throws Exception {
        return new Reports(
                getDiagnosticDataFromXml(folder + "/diagnosticData.xml"),
                getDetailedReportFromXml(folder + "/detailedReport.xml"),
                getSimpleReportFromXml(folder + "/simpleReport.xml"),
                getValidationReportTypeFromXml(folder + "/validationReport.xml")
        );
    }

    private static XmlSimpleReport getSimpleReportFromXml(String path) throws Exception {
        SimpleReportFacade simpleReportFacade = SimpleReportFacade.newFacade();
        return simpleReportFacade.unmarshall(new File(path));
    }

    private static XmlDetailedReport getDetailedReportFromXml(String path) throws Exception {
        DetailedReportFacade detailedReportFacade = DetailedReportFacade.newFacade();
        return detailedReportFacade.unmarshall(new File(path));
    }

    private static XmlDiagnosticData getDiagnosticDataFromXml(String path) throws Exception {
        DiagnosticDataFacade diagnosticDataFacade = DiagnosticDataFacade.newFacade();
        return diagnosticDataFacade.unmarshall(new File(path));
    }

    private static ValidationReportType getValidationReportTypeFromXml(String path) throws Exception {
        ValidationReportFacade validationReportFacade = ValidationReportFacade.newFacade();
        return validationReportFacade.unmarshall(new File(path));
    }

    public static Reports getMockReportsValid() throws Exception {
        return ReportsUtils.generateReportsFromXml("src/test/resources/reports/valid");
    }

    public static Reports getMockReportsNotValid() throws Exception {
        return ReportsUtils.generateReportsFromXml("src/test/resources/reports/not_valid_not_signed");
    }

    public static Reports getMockReportsNotValidSigned() throws Exception {
        return ReportsUtils.generateReportsFromXml("src/test/resources/reports/not_valid_signed");
    }
}
