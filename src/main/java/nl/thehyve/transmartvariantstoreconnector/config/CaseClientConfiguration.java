package nl.thehyve.transmartvariantstoreconnector.config;

import feign.Feign;
import feign.RequestInterceptor;
import feign.jackson.JacksonDecoder;
import nl.thehyve.transmartvariantstoreconnector.client.CaseClient;
import nl.thehyve.transmartvariantstoreconnector.client.MockCaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class CaseClientConfiguration {

    private Logger log = LoggerFactory.getLogger(CaseClientConfiguration.class);

    @Bean
    @Profile("!dev")
    public CaseClient defaultCaseClient(
        RequestInterceptor requestInterceptor,
        VariantStoreClientProperties variantStoreClientProperties) {
        log.info("Configuring case client for {}.", variantStoreClientProperties.getVariantStoreUrl());
        return Feign.builder()
            .requestInterceptor(requestInterceptor)
            .decoder(new JacksonDecoder())
            .target(CaseClient.class, variantStoreClientProperties.getVariantStoreUrl());
    }

    @Bean
    @Profile("dev")
    public CaseClient devCaseClient() {
        log.info("Configuring mock case client.");
        return new MockCaseClient();
    }

}
