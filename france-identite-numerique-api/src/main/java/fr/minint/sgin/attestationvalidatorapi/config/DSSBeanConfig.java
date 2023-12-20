package fr.minint.sgin.attestationvalidatorapi.config;

import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pdf.IPdfObjFactory;
import eu.europa.esig.dss.pdf.ServiceLoaderPdfObjFactory;
import eu.europa.esig.dss.pdf.modifications.DefaultPdfDifferencesFinder;
import eu.europa.esig.dss.pdf.modifications.DefaultPdfObjectModificationsFinder;
import eu.europa.esig.dss.service.crl.JdbcCacheCRLSource;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.http.proxy.ProxyConfig;
import eu.europa.esig.dss.service.http.proxy.ProxyProperties;
import eu.europa.esig.dss.service.ocsp.JdbcCacheOCSPSource;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.service.x509.aia.JdbcCacheAIASource;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.client.http.DSSFileLoader;
import eu.europa.esig.dss.spi.client.http.IgnoreDataLoader;
import eu.europa.esig.dss.spi.client.jdbc.JdbcCacheConnector;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.spi.x509.aia.OnlineAIASource;
import eu.europa.esig.dss.tsl.cache.CacheCleaner;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.TLSource;
import eu.europa.esig.dss.validation.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
@Import({SchedulingConfig.class})
@Log4j2
public class DSSBeanConfig {

    @Value("${dss.truststore.type}")
    private String ksType;

    @Value("${dss.truststore.file}")
    private String ksFilename;

    @Value("${dss.truststore.password}")
    private String ksPassword;

    @Value("${dss.trustedlist.url}")
    private String tlSourceUrl;

    @Value("${dss.trustedlist.proxy.enable}")
    private boolean proxyEnable;

    @Value("${dss.trustedlist.proxy.host}")
    private String proxyHost;

    @Value("#{T(java.lang.Integer).valueOf('${dss.trustedlist.proxy.port}')}")
    private int proxyPort;

    @Value("#{T(java.lang.Integer).valueOf('${dss.dataloader.connection.timeout}')}")
    private int connectionTimeout;

    @Value("#{T(java.lang.Integer).valueOf('${dss.dataloader.connection.request.timeout}')}")
    private int connectionRequestTimeout;

    @Value("${dss.dataloader.ssl-protocols-supported:}#{T(java.util.Collections).emptyList()}")
    private List<String> supportedSSLProtocols;

    @Value("${dss.dataloader.ssl-protocol}")
    private String useProtocolSSL;

    @Value("${dss.dataloader.redirect.enabled}")
    private boolean redirectEnabled;

    @Value("#{T(java.lang.Long).valueOf('${dss.cache.tl-expiration-time-min}')}")
    private long tlCacheExpirationTimeInMinutes;

    @Value("#{T(java.lang.Long).valueOf('${dss.cache.ocsp-expiration-time-min}')}")
    private long ocspCacheExpirationTimeInMinutes;

    @Value("#{T(java.lang.Long).valueOf('${dss.cache.crl-expiration-time-min}')}")
    private long crlCacheExpirationTimeInMinutes;

    @Value("${dss.crt.extra:}#{T(java.util.Collections).emptyList()}")
    private List<String> extraCerts;

    private final DataSource dataSource;

    public DSSBeanConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean(name = "tl-source")
    public TLSource tlSource() throws IOException {
        TLSource tlSource = new TLSource();
        tlSource.setUrl(tlSourceUrl);
        tlSource.setCertificateSource(certificateSource());
        return tlSource;
    }

    @Bean
    public File tlCacheDirectory() {
        File rootFolder = new File(System.getProperty("java.io.tmpdir"));
        File tslCache = new File(rootFolder, "sgin-dss-tsl-loader");
        if (tslCache.mkdirs()) {
            log.info("DSS trusted list cache folder : {}", tslCache.getAbsolutePath());
        }
        return tslCache;
    }

    @Bean
    public KeyStoreCertificateSource certificateSource() throws IOException {
        InputStream inputStream = new ClassPathResource(ksFilename).getInputStream();
        return new KeyStoreCertificateSource(inputStream, ksType, ksPassword);
    }

    @Bean(name = "trusted-list-certificate-source")
    public TrustedListsCertificateSource trustedListSource() {
        return new TrustedListsCertificateSource();
    }

    @Bean
    public CertificateSource trustStoreSource() throws IOException {
        CertificateSource trustedCertificateSource = new CommonTrustedCertificateSource();
        // Add extra AC certificates for attestations validation (don't specify extraCerts in production)
        addTrustedCertificates(extraCerts, trustedCertificateSource);
        return trustedCertificateSource;
    }

    private void addTrustedCertificates(List<String> crtPaths, CertificateSource trustedCertificateSource) throws IOException {
        for (String certificatePath : crtPaths) {
            if (StringUtils.isNotBlank(certificatePath)) {
                CertificateToken crtACRootCertificate = DSSUtils.loadCertificate(new ClassPathResource(certificatePath).getInputStream());
                trustedCertificateSource.addCertificate(crtACRootCertificate);
            }
        }
    }

    /* Validation job */

    @Bean
    public TLValidationJob job() throws IOException {
        TLValidationJob job = new TLValidationJob();
        job.setTrustedListCertificateSource(trustedListSource());
        job.setTrustedListSources(tlSource());
        job.setOfflineDataLoader(offlineFileLoader());
        job.setOnlineDataLoader(onlineFileLoader());
        job.setCacheCleaner(cacheCleaner());
        return job;
    }

    @Bean
    public DSSFileLoader onlineFileLoader() {
        FileCacheDataLoader onlineFileLoader = new FileCacheDataLoader();
        onlineFileLoader.setCacheExpirationTime((60000 * tlCacheExpirationTimeInMinutes)); // Expiration time in milliseconds
        onlineFileLoader.setDataLoader(trustAllDataLoader());
        onlineFileLoader.setFileCacheDirectory(tlCacheDirectory());
        return onlineFileLoader;
    }

    @Bean
    public DSSFileLoader offlineFileLoader() {
        FileCacheDataLoader offlineFileLoader = new FileCacheDataLoader();
        offlineFileLoader.setCacheExpirationTime(-1); // negative value means cache never expires
        offlineFileLoader.setDataLoader(new IgnoreDataLoader()); // do not download from Internet
        offlineFileLoader.setFileCacheDirectory(tlCacheDirectory());
        return offlineFileLoader;
    }

    @Bean
    public CacheCleaner cacheCleaner() {
        CacheCleaner cacheCleaner = new CacheCleaner();
        cacheCleaner.setCleanMemory(true);
        cacheCleaner.setCleanFileSystem(true);
        cacheCleaner.setDSSFileLoader(onlineFileLoader());
        return cacheCleaner;
    }

    /* Certificate Verifier */

    @Bean
    public CertificateVerifier certificateVerifier() throws IOException {
        CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
        certificateVerifier.setTrustedCertSources(trustedListSource(), trustStoreSource());
        certificateVerifier.setAIASource(cacheAIASource());
        certificateVerifier.setOcspSource(cacheOCSPSource());
        certificateVerifier.setCrlSource(cacheCRLSource());
        certificateVerifier.setRevocationDataLoadingStrategyFactory(new OCSPFirstRevocationDataLoadingStrategyFactory());
        certificateVerifier.setRevocationDataVerifier(revocationDataVerifier());
        return certificateVerifier;
    }

    @Bean
    public RevocationDataVerifier revocationDataVerifier() {
        return RevocationDataVerifier.createDefaultRevocationDataVerifier();
    }

    /* Cache sources */

    @Bean
    public JdbcCacheAIASource cacheAIASource() {
        JdbcCacheAIASource cacheAIASource = new JdbcCacheAIASource();
        cacheAIASource.setJdbcCacheConnector(jdbcCacheConnector());
        cacheAIASource.setProxySource(onlineAIASource());
        return cacheAIASource;
    }

    @Bean
    public JdbcCacheOCSPSource cacheOCSPSource() {
        long ocpsExpirationTimeSeconds = 60 * ocspCacheExpirationTimeInMinutes; // Expiration time in seconds
        JdbcCacheOCSPSource cacheOCSPSource = new JdbcCacheOCSPSource();
        cacheOCSPSource.setJdbcCacheConnector(jdbcCacheConnector());
        cacheOCSPSource.setProxySource(onlineOCSPSource());
        cacheOCSPSource.setDefaultNextUpdateDelay(ocpsExpirationTimeSeconds);
        cacheOCSPSource.setMaxNextUpdateDelay(ocpsExpirationTimeSeconds);
        cacheOCSPSource.setRemoveExpired(true);
        return cacheOCSPSource;
    }

    @Bean
    public JdbcCacheCRLSource cacheCRLSource() {
        long clrExpirationTimeSeconds = 60 * crlCacheExpirationTimeInMinutes; // Expiration time in seconds
        JdbcCacheCRLSource cacheCRLSource = new JdbcCacheCRLSource();
        cacheCRLSource.setJdbcCacheConnector(jdbcCacheConnector());
        cacheCRLSource.setProxySource(onlineCRLSource());
        cacheCRLSource.setDefaultNextUpdateDelay(clrExpirationTimeSeconds);
        cacheCRLSource.setMaxNextUpdateDelay(clrExpirationTimeSeconds);
        cacheCRLSource.setRemoveExpired(true);
        return cacheCRLSource;
    }

    @Bean
    public JdbcCacheConnector jdbcCacheConnector() {
        return new JdbcCacheConnector(dataSource);
    }

    /* Online sources */

    @Bean
    public OnlineAIASource onlineAIASource() {
        return new DefaultAIASource(dataLoader());
    }

    @Bean
    public OnlineOCSPSource onlineOCSPSource() {
        OCSPDataLoader ocspDataLoader = configureCommonsDataLoader(new OCSPDataLoader());
        return new OnlineOCSPSource(ocspDataLoader);
    }

    @Bean
    public OnlineCRLSource onlineCRLSource() {
        return new OnlineCRLSource(dataLoader());
    }

    /* Signature policy */

    @Bean
    public SignaturePolicyProvider signaturePolicyProvider() {
        SignaturePolicyProvider signaturePolicyProvider = new SignaturePolicyProvider();
        signaturePolicyProvider.setDataLoader(fileCacheDataLoader());
        return signaturePolicyProvider;
    }

    /* Data loader */

    private <C extends CommonsDataLoader> C configureCommonsDataLoader(C dataLoader) {
        dataLoader.setTimeoutConnection(connectionTimeout);
        dataLoader.setTimeoutConnectionRequest(connectionRequestTimeout);
        dataLoader.setRedirectsEnabled(redirectEnabled);
        if (!CollectionUtils.isEmpty(supportedSSLProtocols)) {
            dataLoader.setSupportedSSLProtocols(supportedSSLProtocols.toArray(String[]::new));
        }
        if (StringUtils.isNotBlank(useProtocolSSL)) {
            dataLoader.setSslProtocol(useProtocolSSL);
        }
        if (proxyEnable) {
            // Configure proxy properties
            ProxyProperties proxyProp = new ProxyProperties();
            proxyProp.setHost(proxyHost);
            if (proxyPort != -1) {
                proxyProp.setPort(proxyPort);
            }
            // Configure proxy configuration
            ProxyConfig proxyConfig = new ProxyConfig();
            proxyConfig.setHttpsProperties(proxyProp);
            proxyConfig.setHttpProperties(proxyProp);
            dataLoader.setProxyConfig(proxyConfig);
        }
        return dataLoader;
    }

    @Bean
    public CommonsDataLoader dataLoader() {
        return configureCommonsDataLoader(new CommonsDataLoader());
    }

    @Bean
    public FileCacheDataLoader fileCacheDataLoader() {
        FileCacheDataLoader fileCacheDataLoader = new FileCacheDataLoader();
        fileCacheDataLoader.setDataLoader(dataLoader());
        return fileCacheDataLoader;
    }

    @Bean
    public CommonsDataLoader trustAllDataLoader() {
        CommonsDataLoader trustAllDataLoader = configureCommonsDataLoader(new CommonsDataLoader());
        trustAllDataLoader.setTrustStrategy(TrustAllStrategy.INSTANCE);
        return trustAllDataLoader;
    }

    @Bean
    public DefaultPdfDifferencesFinder pdfDifferencesFinder() {
        return new DefaultPdfDifferencesFinder();
    }

    @Bean
    public IPdfObjFactory pdfObjFactory() {
        return new ServiceLoaderPdfObjFactory();
    }

    @Bean
    public DefaultPdfObjectModificationsFinder pdfObjectModificationsFinder() {
        return new DefaultPdfObjectModificationsFinder();
    }
}
