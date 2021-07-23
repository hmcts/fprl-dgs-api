package uk.gov.hmcts.reform.fprl.documentgenerator.factory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.fprl.documentgenerator.config.TemplatesConfiguration;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.impl.DocmosisPDFGenerationServiceImpl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fprl.documentgenerator.domain.TemplateConstants.DOCMOSIS_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class PDFGenerationFactoryTest {

    @Mock
    private TemplatesConfiguration templatesConfiguration;

    @Mock
    private DocmosisPDFGenerationServiceImpl docmosisPDFGenerationService;

    @InjectMocks
    private PDFGenerationFactory classUnderTest;

    @Test
    public void shouldReturnTheRightGeneratorServiceByTemplateName() {
        when(templatesConfiguration.getGeneratorServiceNameByTemplateName("templateUsingDocmosisGenerator")).thenReturn(DOCMOSIS_TYPE);

        assertThat(classUnderTest.getGeneratorService("templateUsingDocmosisGenerator"), is(docmosisPDFGenerationService));
    }

}
