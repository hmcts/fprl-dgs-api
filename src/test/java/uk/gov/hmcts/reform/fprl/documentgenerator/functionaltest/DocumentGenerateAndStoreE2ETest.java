package uk.gov.hmcts.reform.fprl.documentgenerator.functionaltest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fprl.documentgenerator.DocumentGeneratorApplication;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.GenerateDocumentRequest;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.FileUploadResponse;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.TemplateManagementService;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.impl.DocumentManagementServiceImpl;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = DocumentGeneratorApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@TestPropertySource(properties = {"management.endpoint.health.cache.time-to-live=0",
    "service-auth-provider.service.stub.enabled=false",
    "evidence-management-api.service.stub.enabled=false"})
@AutoConfigureMockMvc
public class DocumentGenerateAndStoreE2ETest {
    private static final String API_URL = "/version/1/generatePDF";
    private static final String CURRENT_DATE_KEY = "current_date";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSS";
    private static final String TEST_EXAMPLE = "FL-DIV-GOR-ENG-00062.docx";

    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private static final String FILE_URL = "fileURL";
    private static final String MIME_TYPE = "mimeType";
    private static final String CREATED_ON = "createdOn";
    private static final String CREATED_BY = "createdBy";
    private static final String IS_DRAFT = "isDraft";

    @Autowired
    private MockMvc webClient;

    @Value("${docmosis.service.pdf-service.uri}")
    private String docmosisPdfServiceUri;

    @Value("${service.evidence-management-client-api.uri}")
    private String emClientAPIUri;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DocumentManagementServiceImpl documentManagementService;

    private MockRestServiceServer mockRestServiceServer;

    @Before
    public void before() {
        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void givenTemplateNameIsNull_whenGenerateAndStoreDocument_thenReturnHttp400() throws Exception {
        final String template = null;
        final Map<String, Object> values = Collections.emptyMap();

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(template, values);

        webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenTemplateNameIsBlank_whenGenerateAndStoreDocument_thenReturnHttp400() throws Exception {
        final String template = "  ";
        final Map<String, Object> values = Collections.emptyMap();

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(template, values);

        webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenTemplateNotFound_whenGenerateAndStoreDocument_thenReturnHttp400() throws Exception {
        final String template = "nonExistingTemplate";
        final Map<String, Object> values = Collections.emptyMap();

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(template, values);

        webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Ignore
    public void givenCouldNotConnectToAuthService_whenGenerateAndStoreDocument_thenReturnHttp503() throws Exception {
        Map<String, Object> caseData = new HashMap<>();

        Map<String, Object> requestData = Collections.singletonMap(
            CASE_DETAILS, Collections.singletonMap(CASE_DATA, caseData)
        );

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(TEST_EXAMPLE, requestData);

        when(serviceTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    @Ignore
    public void givenAuthServiceReturnAuthenticationError_whenGenerateAndStoreDocument_thenReturnHttp401() throws Exception {
        Map<String, Object> caseData = new HashMap<>();

        Map<String, Object> requestData = Collections.singletonMap(
            CASE_DETAILS, Collections.singletonMap(CASE_DATA, caseData)
        );

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(TEST_EXAMPLE, requestData);

        when(serviceTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Ignore
    public void givenObjectMapperThrowsException_whenGenerateAndStoreDocument_thenReturnHttp500() throws Exception {
        final ObjectMapper objectMapper = mock(ObjectMapper.class);

        final Map<String, Object> values = new HashMap<>();
        values.put("someKey", "someValue");
        values.put(IS_DRAFT, false);

        Map<String, Object> requestData = Collections.singletonMap(
            CASE_DETAILS, Collections.singletonMap(CASE_DATA, values)
        );
        final String securityToken = "securityToken";
        final Instant instant = Instant.now();
        mockAndSetClock(instant);

        final Map<String, Object> valuesWithDate = new HashMap<>(values);
        valuesWithDate.put(CURRENT_DATE_KEY, new SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            .format(Date.from(instant)));

        Map<String, Object> requestDataWithDate = Collections.singletonMap(
            CASE_DETAILS, Collections.singletonMap(CASE_DATA, valuesWithDate)
        );

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(TEST_EXAMPLE, requestData);

        final GenerateDocumentRequest requestToPDFService = new GenerateDocumentRequest(TEST_EXAMPLE, requestDataWithDate);

        when(serviceTokenGenerator.generate()).thenReturn(securityToken);
        when(objectMapper.writeValueAsString(requestToPDFService)).thenThrow(mock(JsonProcessingException.class));

        webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        mockRestServiceServer.verify();
    }

    @Test
    public void givenAllGoesWellForTestExample_whenGenerateAndStoreDocument_thenReturn()
        throws Exception {
        assertReturnWhenAllGoesWellForGeneratingAndStoringDocuments(TEST_EXAMPLE);
    }

    private void assertReturnWhenAllGoesWellForGeneratingAndStoringDocuments(String templateId) throws Exception {
        //Given
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> values = new HashMap<>();
        values.put(CASE_DETAILS, Collections.singletonMap(CASE_DATA, caseData));

        mockDocmosisPDFService(HttpStatus.OK, new byte[] {1});
        final FileUploadResponse fileUploadResponse = getFileUploadResponse(HttpStatus.OK);
        mockEMClientAPI(HttpStatus.OK, Collections.singletonList(fileUploadResponse));

        final String securityToken = "securityToken";
        when(serviceTokenGenerator.generate()).thenReturn(securityToken);

        //When
        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(templateId, values);
        MvcResult result = webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        //Then
        final GeneratedDocumentInfo generatedDocumentInfo = getGeneratedDocumentInfo();
        assertEquals(ObjectMapperTestUtil.convertObjectToJsonString(generatedDocumentInfo), result.getResponse().getContentAsString());
        mockRestServiceServer.verify();
    }

    private FileUploadResponse getFileUploadResponse(HttpStatus httpStatus) {
        final FileUploadResponse fileUploadResponse = new FileUploadResponse(httpStatus);
        fileUploadResponse.setFileUrl(FILE_URL);
        fileUploadResponse.setMimeType(MIME_TYPE);
        fileUploadResponse.setCreatedOn(CREATED_ON);
        fileUploadResponse.setCreatedBy(CREATED_BY);
        return fileUploadResponse;
    }

    private GeneratedDocumentInfo getGeneratedDocumentInfo() {
        GeneratedDocumentInfo generatedDocumentInfo = new GeneratedDocumentInfo();
        generatedDocumentInfo.setUrl(FILE_URL);
        generatedDocumentInfo.setMimeType(MIME_TYPE);
        generatedDocumentInfo.setCreatedOn(CREATED_ON);
        return generatedDocumentInfo;
    }

    private void mockAndSetClock(Instant instant) {
        final Clock clock = mock(Clock.class);
        when(clock.instant()).thenReturn(instant);

        ReflectionTestUtils.setField(documentManagementService, "clock", clock);
    }

    private void mockDocmosisPDFService(HttpStatus expectedResponse, byte[] body) {
        mockRestServiceServer.expect(once(), requestTo(docmosisPdfServiceUri)).andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(expectedResponse)
                .body(ObjectMapperTestUtil.convertObjectToJsonBytes(body))
                .contentType(MediaType.APPLICATION_JSON));
    }

    private void mockEMClientAPI(HttpStatus expectedResponse, List<FileUploadResponse> fileUploadResponse) {
        mockRestServiceServer.expect(once(), requestTo(emClientAPIUri))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(expectedResponse)
                .body(ObjectMapperTestUtil.convertObjectToJsonString(fileUploadResponse))
                .contentType(MediaType.APPLICATION_JSON));
    }

}
