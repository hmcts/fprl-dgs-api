package uk.gov.hmcts.reform.fprl.documentgenerator.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.fprl.documentgenerator.config.TemplatesConfiguration;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.fprl.documentgenerator.factory.PDFGenerationFactory;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.PDFGenerationService;

import java.util.HashMap;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fprl.documentgenerator.functionaltest.DocumentGenerateAndStoreE2ETest.FILE_URL;
import static uk.gov.hmcts.reform.fprl.documentgenerator.functionaltest.DocumentGenerateAndStoreE2ETest.MIME_TYPE;
import static uk.gov.hmcts.reform.fprl.documentgenerator.functionaltest.DocumentGenerateAndStoreE2ETest.TEST_HASH_TOKEN;
import static uk.gov.hmcts.reform.fprl.documentgenerator.functionaltest.DocumentGenerateAndStoreE2ETest.mockCaseDocsDocuments;

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementServiceImplUTest {

    private static final String A_TEMPLATE = "a-certain-template";
    private static final String A_TEMPLATE_FILE_NAME = "fileName.pdf";
    private static final String TEST_AUTH_TOKEN = "someToken";
    private static final byte[] TEST_GENERATED_DOCUMENT = new byte[] {1};
    public static final String S2S_TOKEN = "s2s authenticated";

    private UploadResponse expectedUploadResponse;

    @Mock
    private PDFGenerationFactory pdfGenerationFactory;

    @Mock
    private PDFGenerationService pdfGenerationService;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private TemplatesConfiguration templatesConfiguration;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private DocumentManagementServiceImpl classUnderTest;

    @Before
    public void setUp() {
        expectedUploadResponse = new UploadResponse(asList(mockCaseDocsDocuments()));
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
    }

    @Test
    public void givenTemplateNameIsAosInvitation_whenGenerateAndStoreDocument_thenProceedAsExpected() {
        when(pdfGenerationFactory.getGeneratorService(A_TEMPLATE)).thenReturn(pdfGenerationService);
        when(pdfGenerationService.generate(eq(A_TEMPLATE), any())).thenReturn(TEST_GENERATED_DOCUMENT);
        when(templatesConfiguration.getFileNameByTemplateName(A_TEMPLATE)).thenReturn(A_TEMPLATE_FILE_NAME);
        when(caseDocumentClient.uploadDocuments(
            eq(TEST_AUTH_TOKEN), eq(S2S_TOKEN), eq("C100"), eq("PRIVATELAW"), any()
        )).thenReturn(expectedUploadResponse);

        GeneratedDocumentInfo generatedDocumentInfo = classUnderTest
            .generateAndStoreDocument(A_TEMPLATE, new HashMap<>(), TEST_AUTH_TOKEN);

        assertGeneratedDocumentInfoIsAsExpected(generatedDocumentInfo);
    }

    @Test
    public void givenPdfGeneratorIsUsed_whenGenerateDocumentWithHtmlCharacters_thenEscapeHtmlCharacters() {
        when(pdfGenerationFactory.getGeneratorService(A_TEMPLATE)).thenReturn(pdfGenerationService);
        when(pdfGenerationService.generate(eq(A_TEMPLATE), any())).thenReturn(TEST_GENERATED_DOCUMENT);

        byte[] generatedDocument = classUnderTest.generateDocument(A_TEMPLATE, new HashMap<>());

        assertThat(generatedDocument, equalTo(TEST_GENERATED_DOCUMENT));
        verify(pdfGenerationFactory).getGeneratorService(A_TEMPLATE);
        verify(pdfGenerationService).generate(eq(A_TEMPLATE), eq(emptyMap()));
    }

    @Test
    public void givenTemplateNameIsInvalid_whenGenerateAndStoreDocument_thenThrowException() {
        String unknownTemplateName = "unknown-template";
        HashMap<String, Object> placeholders = new HashMap<>();
        when(templatesConfiguration.getFileNameByTemplateName(unknownTemplateName))
            .thenThrow(new IllegalArgumentException("Unknown template: " + unknownTemplateName));

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> {
            classUnderTest.generateAndStoreDocument(unknownTemplateName, placeholders, "some-auth-token");
        });

        assertThat(illegalArgumentException.getMessage(), equalTo("Unknown template: " + unknownTemplateName));
    }

    private void assertGeneratedDocumentInfoIsAsExpected(GeneratedDocumentInfo generatedDocumentInfo) {
        assertThat(generatedDocumentInfo.getUrl(), equalTo(FILE_URL));
        assertThat(generatedDocumentInfo.getMimeType(), equalTo(MIME_TYPE));
        assertThat(generatedDocumentInfo.getHashToken(), equalTo(TEST_HASH_TOKEN));
    }
}
