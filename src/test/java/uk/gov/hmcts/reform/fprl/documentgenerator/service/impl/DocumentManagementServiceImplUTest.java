package uk.gov.hmcts.reform.fprl.documentgenerator.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.fprl.documentgenerator.config.TemplatesConfiguration;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.PDFGenerationService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementServiceImplUTest {

    private static final String A_TEMPLATE = "a-certain-template";
    private static final String A_TEMPLATE_FILE_NAME = "fileName.pdf";
    private static final String TEST_AUTH_TOKEN = "someToken";
    private static final byte[] TEST_GENERATED_DOCUMENT = new byte[]{1};
    public static final String TEST_S2S_TOKEN = "s2s-token";
    public static final String TEST_URL = "someUrl";
    public static final String TEST_MIME_TYPE = "someMimeType";
    public static final Date TEST_DATE = new Date();

    @Mock
    private PDFGenerationService pdfGenerationService;

    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;

    @Mock
    private ServiceAuthTokenGenerator serviceAuthTokenGenerator;

    @Mock
    private TemplatesConfiguration templatesConfiguration;

    @InjectMocks
    private DocumentManagementServiceImpl classUnderTest;

    @Test
    public void generateAndStoreDocumentIsExecutedSuccessfully() {
        when(pdfGenerationService.generate(eq(A_TEMPLATE), any())).thenReturn(TEST_GENERATED_DOCUMENT);
        when(templatesConfiguration.getFileNameByTemplateName(A_TEMPLATE)).thenReturn(A_TEMPLATE_FILE_NAME);
        when(serviceAuthTokenGenerator.generate()).thenReturn(TEST_S2S_TOKEN);
        when(caseDocumentClientApi.uploadDocuments(eq(TEST_AUTH_TOKEN), eq(TEST_S2S_TOKEN), any(DocumentUploadRequest.class)))
            .thenReturn(buildUploadResponse());

        GeneratedDocumentInfo generatedDocumentInfo = classUnderTest.generateAndStoreDocument(A_TEMPLATE, new HashMap<>(), TEST_AUTH_TOKEN);

        assertGeneratedDocumentInfoIsAsExpected(generatedDocumentInfo);
    }

    @Test
    public void generateDocumentIsExecutedSuccessfully() {
        when(pdfGenerationService.generate(eq(A_TEMPLATE), any())).thenReturn(TEST_GENERATED_DOCUMENT);

        byte[] generatedDocument = classUnderTest.generateDocument(A_TEMPLATE, new HashMap<>());

        assertThat(generatedDocument, equalTo(TEST_GENERATED_DOCUMENT));
        verify(pdfGenerationService).generate(eq(A_TEMPLATE), eq(emptyMap()));
    }

    @Test
    public void shouldThrowExceptionWhenTemplateNameIsInvalid() {
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
        assertThat(generatedDocumentInfo.getUrl(), equalTo(TEST_URL));
        assertThat(generatedDocumentInfo.getMimeType(), equalTo(TEST_MIME_TYPE));
        assertThat(generatedDocumentInfo.getCreatedOn(), equalTo(TEST_DATE.toString()));
    }

    private UploadResponse buildUploadResponse() {
        ArrayList<Document> documents = new ArrayList<>();
        Document.Links links = new Document.Links();
        Document.Link link = new Document.Link();

        ReflectionTestUtils.setField(links, "self", link);
        ReflectionTestUtils.setField(link, "href", TEST_URL);

        documents.add(
            Document.builder()
                .mimeType(TEST_MIME_TYPE)
                .createdOn(TEST_DATE)
                .links(links)
                .build()
        );

        return new UploadResponse(documents);
    }
}
