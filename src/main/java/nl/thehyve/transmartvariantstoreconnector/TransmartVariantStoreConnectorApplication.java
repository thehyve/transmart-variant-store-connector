package nl.thehyve.transmartvariantstoreconnector;

import nl.thehyve.transmartvariantstoreconnector.config.VariantStoreClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.transmartproject.common.config.TransmartClientProperties;

@EnableFeignClients
@EnableConfigurationProperties({ TransmartClientProperties.class, VariantStoreClientProperties.class})
@SpringBootApplication
@ComponentScan(basePackages = {"nl.thehyve.transmartvariantstoreconnector", "org.transmartproject.proxy"})
public class TransmartVariantStoreConnectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransmartVariantStoreConnectorApplication.class, args);
	}

}
