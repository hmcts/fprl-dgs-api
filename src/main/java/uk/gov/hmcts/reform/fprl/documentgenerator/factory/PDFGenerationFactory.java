package uk.gov.hmcts.reform.fprl.documentgenerator.factory;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fprl.documentgenerator.config.TemplatesConfiguration;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.TemplateConstants;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.PDFGenerationService;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.impl.DocmosisPDFGenerationServiceImpl;

import java.util.Map;

@Component
public class PDFGenerationFactory {

    private final TemplatesConfiguration templatesConfiguration;
    private final Map<String, PDFGenerationService> generatorMap;

    @Autowired
    public PDFGenerationFactory(TemplatesConfiguration templatesConfiguration,
                                DocmosisPDFGenerationServiceImpl docmosisPdfGenerationService) {
        this.templatesConfiguration = templatesConfiguration;

        // Setup generator type mapping against expected template map values
        this.generatorMap = ImmutableMap.of(
            TemplateConstants.DOCMOSIS_TYPE, docmosisPdfGenerationService
        );
    }

    public PDFGenerationService getGeneratorService(String templateId) {
        String generatorServiceName = templatesConfiguration.getGeneratorServiceNameByTemplateName(templateId);
        return generatorMap.get(generatorServiceName);
    }

}
