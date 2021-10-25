package uk.gov.hmcts.reform.fprl;

import feign.Response;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.fprl.documentgenerator.controller.DocumentGeneratorController;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class DocumentGeneratorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void givenTemplateShouldGeneratePdf_VerifyResponse() throws Exception {

        final String EXPC_URL = "http://dm-store-aat.service.core-compute-aat.internal/documents/*";
        final String MIME_TYPE = "application/pdf";
        //when
        ResultActions response = this.mockMvc.perform(MockMvcRequestBuilders.post("/generatePdf")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        response.andExpect(status().isCreated());
        response.andExpect( jsonPath("$.url", Matchers.contains(EXPC_URL)))
           .andExpect(jsonPath("$.mimeType").value(MIME_TYPE));

        System.out.println("Status Code: " +response.andExpect(status().isOk()) );

    }

}
