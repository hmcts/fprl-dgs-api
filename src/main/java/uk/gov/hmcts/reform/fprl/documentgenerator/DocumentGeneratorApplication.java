package uk.gov.hmcts.reform.fprl.documentgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.hmcts.reform.authorisation.ServiceAuthAutoConfiguration;

@SpringBootApplication(
    scanBasePackages = {"uk.gov.hmcts.reform.fprl",  "uk.gov.hmcts.reform.logging.appinsights"},
    exclude = {ServiceAuthAutoConfiguration.class})
public class DocumentGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentGeneratorApplication.class, args);
    }
}
