package uk.gov.hmcts.reform.fprl.documentgenerator.service.impl;

import org.junit.Before;
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
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.PlaceholderData;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.PDFGenerationService;
import uk.gov.hmcts.reform.fprl.documentgenerator.util.PlaceholderDataProvider;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_DATE;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_GENERATED_DOCUMENT;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_MIME_TYPE;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_S2S_TOKEN;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_URL;

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementServiceImplTest {

    public static final PlaceholderData empty = PlaceholderDataProvider.empty();

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

    @Before
    public void setup() {
        when(serviceAuthTokenGenerator.generate()).thenReturn(TEST_S2S_TOKEN);
        when(pdfGenerationService.generate(eq(TEST_TEMPLATE_NAME), any())).thenReturn(TEST_GENERATED_DOCUMENT);
        when(caseDocumentClientApi.uploadDocuments(eq(TEST_AUTH_TOKEN), eq(TEST_S2S_TOKEN), any(DocumentUploadRequest.class)))
            .thenReturn(buildUploadResponse());
    }

    @Test
    public void generateAndStoreDocumentIsExecutedSuccessfully() {
        GeneratedDocumentInfo generatedDocumentInfo = classUnderTest
            .generateAndStoreDocument(TEST_TEMPLATE_NAME, empty, TEST_AUTH_TOKEN);

        assertGeneratedDocumentInfoIsAsExpected(generatedDocumentInfo);
    }

    @Test
    public void generateDocumentIsExecutedSuccessfully() {
        when(pdfGenerationService.generate(eq(TEST_TEMPLATE_NAME), any())).thenReturn(TEST_GENERATED_DOCUMENT);

        byte[] generatedDocument = classUnderTest.generateDocument(TEST_TEMPLATE_NAME, empty);

        assertThat(generatedDocument, equalTo(TEST_GENERATED_DOCUMENT));
        verify(pdfGenerationService).generate(eq(TEST_TEMPLATE_NAME), eq(empty));
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
