package fr.minint.sgin.attestationvalidatorapi;

import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log4j2
public class AttestationValidatorApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttestationValidatorApiApplication.class, args);
    }

    @PostConstruct
    public void init() {
        log.info("Attestation-validator-api started");
    }

}
