package uk.gov.hmcts.reform.fprl.documentgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.authorisation.ServiceAuthAutoConfiguration;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.fprl"})
@SpringBootApplication(
    scanBasePackages = {
        "uk.gov.hmcts.reform.fprl",
        "uk.gov.hmcts.reform.logging.appinsights",
        "uk.gov.hmcts.reform.ccd.document"
    },
    exclude = {ServiceAuthAutoConfiguration.class})
public class DocumentGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentGeneratorApplication.class, args);
    }
}
