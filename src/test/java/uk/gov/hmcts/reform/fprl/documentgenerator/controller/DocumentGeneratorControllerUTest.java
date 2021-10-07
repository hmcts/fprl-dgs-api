package uk.gov.hmcts.reform.fprl.documentgenerator.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.GenerateDocumentRequest;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.PlaceholderData;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.DocumentManagementService;
import uk.gov.hmcts.reform.fprl.documentgenerator.util.PlaceholderDataProvider;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_TEMPLATE_NAME;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGeneratorControllerUTest {

    @Mock
    private DocumentManagementService documentManagementService;

    @InjectMocks
    private DocumentGeneratorController classUnderTest;

    @Test
    public void whenGeneratePDF_thenReturnGeneratedPDFDocumentInfo() {
        PlaceholderData placeholder = PlaceholderDataProvider.empty();
        final GeneratedDocumentInfo expected = GeneratedDocumentInfo.builder().build();

        when(documentManagementService.generateAndStoreDocument(TEST_TEMPLATE_NAME, placeholder, TEST_AUTH_TOKEN))
            .thenReturn(expected);

        GeneratedDocumentInfo actual = classUnderTest
            .generateAndUploadPdf(TEST_AUTH_TOKEN, new GenerateDocumentRequest(TEST_TEMPLATE_NAME, placeholder));

        assertEquals(expected, actual);

        verify(documentManagementService)
            .generateAndStoreDocument(TEST_TEMPLATE_NAME, placeholder, TEST_AUTH_TOKEN);
    }

    @Test
    public void whenGeneratePDF_thenReturnGeneratedDraftPDFDocumentInfo() {
        PlaceholderData placeholder = PlaceholderDataProvider.empty();
        GeneratedDocumentInfo expected = GeneratedDocumentInfo.builder().build();

        when(documentManagementService.generateAndStoreDraftDocument(TEST_TEMPLATE_NAME, placeholder, TEST_AUTH_TOKEN))
            .thenReturn(expected);

        GeneratedDocumentInfo actual = classUnderTest
            .generateAndUploadDraftPdf(TEST_AUTH_TOKEN, new GenerateDocumentRequest(TEST_TEMPLATE_NAME, placeholder));

        assertEquals(expected, actual);

        verify(documentManagementService)
            .generateAndStoreDraftDocument(TEST_TEMPLATE_NAME, placeholder, TEST_AUTH_TOKEN);
    }
}
