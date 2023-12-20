package fr.minint.sgin.attestationvalidatorapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Setter
@Getter
public class SignatureConfig {

    @Value("${sgin.signature.policy-id}")
    private String signaturePolicyId;
    @Value("${sgin.signature.pattern.organizational-unit}")
    private String organizationalUnit;
    @Value("${sgin.signature.pattern.organization-identifier}")
    private String organizationIdentifier;
    @Value("${sgin.signature.verify-hash}")
    private boolean verifyHashSignature;
}
