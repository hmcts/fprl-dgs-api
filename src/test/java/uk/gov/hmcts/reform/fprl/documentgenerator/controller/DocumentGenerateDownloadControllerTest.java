package uk.gov.hmcts.reform.fprl.documentgenerator.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.GenerateDocumentRequest;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.PlaceholderData;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.DocumentManagementService;
import uk.gov.hmcts.reform.fprl.documentgenerator.util.PlaceholderDataProvider;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_GENERATED_DOCUMENT;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_TEMPLATE_NAME;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGenerateDownloadControllerTest {

    @Mock
    private DocumentManagementService documentManagementService;

    @InjectMocks
    private DocumentGenerateDownloadController classUnderTest;

    @Test
    public void whenGenerateDocument_thenReturnProperResponse() {
        final String someOtherKeyName = "someOtherKeyName";
        final String someOtherKeyValue = "someOtherKeyValue";

        PlaceholderData placeHolders = PlaceholderDataProvider
            .withData(Collections.singletonMap(someOtherKeyName, someOtherKeyValue));

        GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(
            TEST_TEMPLATE_NAME,
            placeHolders
        );

        when(documentManagementService.generateDocument(TEST_TEMPLATE_NAME, placeHolders))
            .thenReturn(TEST_GENERATED_DOCUMENT);

        classUnderTest.generatePdfBinary(generateDocumentRequest);

        verify(documentManagementService).generateDocument(TEST_TEMPLATE_NAME, placeHolders);
    }
}
