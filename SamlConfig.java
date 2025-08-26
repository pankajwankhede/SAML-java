import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SamlConfig {

    @Bean
    public XMLObjectBuilderFactory xmlObjectBuilderFactory() {
        return XMLObjectProviderRegistrySupport.getBuilderFactory();
    }
}
