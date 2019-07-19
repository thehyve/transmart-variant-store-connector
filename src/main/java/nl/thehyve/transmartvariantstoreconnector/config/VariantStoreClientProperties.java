package nl.thehyve.transmartvariantstoreconnector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
    prefix = "variant-store-client",
    ignoreUnknownFields = false
)
@Data
public class VariantStoreClientProperties {
    private String variantStoreUrl;
}
