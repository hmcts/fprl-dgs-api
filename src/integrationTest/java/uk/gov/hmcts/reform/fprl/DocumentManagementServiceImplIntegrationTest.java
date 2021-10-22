package uk.gov.hmcts.reform.fprl;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

@Slf4j
public class DocumentManagementServiceImplIntegrationTest extends IntegrationTest {

    private static final String DOCUMENT_URL = "url";
    private static final String APPLICATION_MIME_TYPE = "application/pdf";
    private static final String MIME_TYPE = "mimeType";

    private static final String INVALID_TEMPLATE_NAME_JSON = "invalid-template-name.json";
    private static final String INVALID_TEMPLATE_DATA_JSON = "invalid-template-data.json";
    private static final String VALID_INPUT_JSON = "documentgenerator/valid-input.json";

    @Test
    public void givenTemplateAndJsonInput_ReturnExpectedDocumentInfo() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);


        //GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().url(getUrl).mimeType().createdOn().hashToken().build();
        Response response = callprlDocumentGenerator(requestBody);

        List<String> jsonResponse = response.jsonPath().getList("$");

        for (String json: jsonResponse) {
            log.debug("Json: ", json);
        }
    }
}
