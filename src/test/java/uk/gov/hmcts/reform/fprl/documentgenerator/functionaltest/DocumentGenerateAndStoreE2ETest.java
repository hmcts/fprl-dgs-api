package uk.gov.hmcts.reform.fprl.documentgenerator.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.fprl.documentgenerator.DocumentGeneratorApplication;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.CaseDetails;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.GenerateDocumentRequest;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.PlaceholderData;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.GeneratedDocumentInfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fprl.documentgenerator.util.TestConsts.TEST_TEMPLATE_NAME;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = DocumentGeneratorApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@TestPropertySource(properties = {"management.endpoint.health.cache.time-to-live=0",
    "service-auth-provider.service.stub.enabled=true"})
@AutoConfigureMockMvc
public class DocumentGenerateAndStoreE2ETest {
    private static final String API_URL = "/version/1/generatePDF";
    private static final String CASE_DOCS_API_URL = "/cases/documents";
    private static final String DOCMOSIS_API_URL = "/rs/render";

    private static final String NOT_ALLOWED_SERVICE = "test_service_not_allowed";

    private static final String TEST_DATE = "18/8/2021";

    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private static final String FILE_URL = "fileURL";
    private static final String MIME_TYPE = "mimeType";

    private static final String TEST_HASH_TOKEN = "hashToken";
    private static final String TEST_DEFAULT_NAME_FOR_PDF_FILE = "FPRLDocument.pdf";

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule caseDocsClientApiServiceServer = new WireMockClassRule(5170);

    @ClassRule
    public static WireMockClassRule docmosisClientServiceServer = new WireMockClassRule(5501);

    @ClassRule
    public static WireMockClassRule serviceAuthServer = new WireMockClassRule(4502);

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @Test
    public void givenTemplateNameIsNull_whenGenerateAndStoreDocument_thenReturnHttp400() throws Exception {
        final String template = null;
        PlaceholderData placeholderData = PlaceholderData.builder().build();

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(template, placeholderData);

        webClient.perform(post(API_URL)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenTemplateNameIsBlank_whenGenerateAndStoreDocument_thenReturnHttp400() throws Exception {
        final String template = "  ";
        PlaceholderData placeholderData = PlaceholderData.builder()
            .caseDetails(CaseDetails.builder().caseData(Collections.emptyMap()).build()).build();

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(template, placeholderData);

        webClient.perform(post(API_URL)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenTemplateNotFound_whenGenerateAndStoreDocument_thenReturnHttp400() throws Exception {
        final String template = "nonExistingTemplate";
        PlaceholderData placeholderData = PlaceholderData.builder()
            .caseDetails(CaseDetails.builder().caseData(Collections.emptyMap()).build()).build();

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(template, placeholderData);

        webClient.perform(post(API_URL)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCouldNotConnectToAuthService_whenGenerateAndStoreDocument_thenReturnHttp503() throws Exception {
        PlaceholderData placeholderData = PlaceholderData.builder()
            .caseDetails(CaseDetails.builder().caseData(Collections.emptyMap()).build()).build();

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(TEST_TEMPLATE_NAME, placeholderData);

        mockDocmosisPdfService(HttpStatus.OK, new byte[]{1});
        when(serviceTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        webClient.perform(post(API_URL)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void givenAuthServiceReturnAuthenticationError_whenGenerateAndStoreDocument_thenReturnHttp401() throws Exception {
        PlaceholderData placeholderData = PlaceholderData.builder()
            .caseDetails(CaseDetails.builder().caseData(Collections.emptyMap()).build()).build();

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(TEST_TEMPLATE_NAME, placeholderData);

        mockDocmosisPdfService(HttpStatus.OK, new byte[]{1});
        when(serviceTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        webClient.perform(post(API_URL)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenAllGoesWellForTestExample_whenGenerateAndStoreDocument_thenReturn()
        throws Exception {
        assertReturnWhenAllGoesWellForGeneratingAndStoringDocuments(TEST_TEMPLATE_NAME);
    }

    private void assertReturnWhenAllGoesWellForGeneratingAndStoringDocuments(String templateId) throws Exception {
        final PlaceholderData placeholderData = PlaceholderData.builder()
            .caseDetails(CaseDetails.builder().caseData(Collections.emptyMap()).build()).build();

        mockDocmosisPdfService(HttpStatus.OK, new byte[]{1});

        UploadResponse uploadResponse = new UploadResponse(List.of(mockCaseDocsDocuments()));
        mockCaseDocsClientApi(HttpStatus.OK, uploadResponse);

        final String securityToken = "securityToken";
        when(serviceTokenGenerator.generate()).thenReturn(securityToken);

        //When
        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(templateId, placeholderData);
        MvcResult result = webClient.perform(post(API_URL)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        //Then
        final GeneratedDocumentInfo generatedDocumentInfo = getGeneratedDocumentInfo();
        assertEquals(ObjectMapperTestUtil.convertObjectToJsonString(generatedDocumentInfo), result.getResponse().getContentAsString());
    }

    private GeneratedDocumentInfo getGeneratedDocumentInfo() throws ParseException {
        return GeneratedDocumentInfo.builder()
            .url(FILE_URL)
            .mimeType(MIME_TYPE)
            .createdOn(generateDate().toString())
            .build();
    }

    private void mockDocmosisPdfService(HttpStatus expectedResponse, byte[] body) {
        docmosisClientServiceServer.stubFor(WireMock.post(DOCMOSIS_API_URL)
            .willReturn(aResponse()
                .withStatus(expectedResponse.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(ObjectMapperTestUtil.convertObjectToJsonString(body))
            ));
    }

    private void mockServiceAuthServer(HttpStatus expectedResponse, String body) {
        serviceAuthServer.stubFor(WireMock.post(DOCMOSIS_API_URL)
            .willReturn(aResponse()
                .withStatus(expectedResponse.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(ObjectMapperTestUtil.convertObjectToJsonString(body))
            ));
    }

    private void mockCaseDocsClientApi(HttpStatus expectedResponse, UploadResponse uploadResponse) {
        caseDocsClientApiServiceServer.stubFor(WireMock.post(CASE_DOCS_API_URL)
            .willReturn(aResponse()
                .withStatus(expectedResponse.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(ObjectMapperTestUtil.convertObjectToJsonString(uploadResponse))
            ));
    }

    private Document mockCaseDocsDocuments() throws ParseException {
        Document.Link link = new Document.Link();
        link.href = FILE_URL;

        Document.Links links = new Document.Links();
        links.self = link;

        return Document.builder()
            .createdOn(generateDate())
            .links(links)
            .hashToken(TEST_HASH_TOKEN)
            .mimeType(MIME_TYPE)
            .originalDocumentName(TEST_DEFAULT_NAME_FOR_PDF_FILE)
            .build();
    }

    private Date generateDate() throws ParseException {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");

        return format.parse(TEST_DATE);
    }
}
