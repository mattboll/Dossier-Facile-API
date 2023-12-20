package fr.minint.sgin.attestationvalidatorapi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class FileConfig {

    @Value("#{T(java.lang.Long).valueOf('${sgin.file.max-file-size-kb}')}")
    private long maxFileSizeKb;
    @Value("${sgin.file.get-reports}")
    private boolean getReports;
}
