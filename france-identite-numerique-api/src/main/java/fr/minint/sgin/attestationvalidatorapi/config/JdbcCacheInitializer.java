package fr.minint.sgin.attestationvalidatorapi.config;

import eu.europa.esig.dss.service.crl.JdbcCacheCRLSource;
import eu.europa.esig.dss.service.ocsp.JdbcCacheOCSPSource;
import eu.europa.esig.dss.service.x509.aia.JdbcCacheAIASource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.SQLException;

/**
 * This class is used to construct/destroy JDBC cache sources
 *
 */
@Component
public class JdbcCacheInitializer {

    private final JdbcCacheAIASource cachedAIASource;

    private final JdbcCacheCRLSource cachedCRLSource;

    private final JdbcCacheOCSPSource cachedOCSPSource;

    public JdbcCacheInitializer(JdbcCacheAIASource cachedAIASource, JdbcCacheCRLSource cachedCRLSource, JdbcCacheOCSPSource cachedOCSPSource) {
        this.cachedAIASource = cachedAIASource;
        this.cachedCRLSource = cachedCRLSource;
        this.cachedOCSPSource = cachedOCSPSource;
    }

    @PostConstruct
    public void cachedAIASourceInitialization() throws SQLException {
        cachedAIASource.initTable();
    }

    @PostConstruct
    public void cachedCRLSourceInitialization() throws SQLException {
        cachedCRLSource.initTable();
    }

    @PostConstruct
    public void cachedOCSPSourceInitialization() throws SQLException {
        cachedOCSPSource.initTable();
    }

    @PreDestroy
    public void cachedAIASourceClean() throws SQLException {
        if (cachedAIASource.isTableExists()) {
            cachedAIASource.destroyTable();
        }
    }

    @PreDestroy
    public void cachedCRLSourceClean() throws SQLException {
        if (cachedCRLSource.isTableExists()) {
            cachedCRLSource.destroyTable();
        }
    }

    @PreDestroy
    public void cachedOCSPSourceClean() throws SQLException {
        if (cachedOCSPSource.isTableExists()) {
            cachedOCSPSource.destroyTable();
        }
    }
}
