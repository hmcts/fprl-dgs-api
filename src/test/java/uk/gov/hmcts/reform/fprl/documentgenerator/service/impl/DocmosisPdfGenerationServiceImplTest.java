package uk.gov.hmcts.reform.fprl.documentgenerator.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.fprl.documentgenerator.clients.DocmosisApiClient;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.PdfDocumentRequest;
import uk.gov.hmcts.reform.fprl.documentgenerator.exception.PDFGenerationException;
import uk.gov.hmcts.reform.fprl.documentgenerator.util.PlaceholderDataProvider;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_GENERATED_DOCUMENT;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_TEMPLATE_NAME;

@RunWith(MockitoJUnitRunner.class)
public class DocmosisPdfGenerationServiceImplTest {

    @Mock
    private DocmosisApiClient docmosisApiClient;

    @InjectMocks
    private DocmosisPdfGenerationServiceImpl classUnderTest;

    @Test
    public void generateSuccessful() {
        ResponseEntity<byte[]> response = new ResponseEntity<>(TEST_GENERATED_DOCUMENT, HttpStatus.OK);
        PdfDocumentRequest request = PdfDocumentRequest.builder()
            .templateName(TEST_TEMPLATE_NAME)
            .outputName("result.pdf")
            .data(new HashMap<>())
            .build();

        when(docmosisApiClient.generatePdf(request)).thenReturn(response);

        assertThat(
            classUnderTest.generate(TEST_TEMPLATE_NAME, PlaceholderDataProvider.empty()),
            is(TEST_GENERATED_DOCUMENT)
        );
    }

    @Test
    public void generateHandleException() {
        when(docmosisApiClient.generatePdf(any())).thenThrow(new NullPointerException("a"));

        PDFGenerationException exception = assertThrows(
            PDFGenerationException.class,
            () -> classUnderTest.generate(TEST_TEMPLATE_NAME, PlaceholderDataProvider.empty())
        );

        assertThat(exception.getMessage(), is("Failed to request PDF from REST endpoint a"));
    }
}
