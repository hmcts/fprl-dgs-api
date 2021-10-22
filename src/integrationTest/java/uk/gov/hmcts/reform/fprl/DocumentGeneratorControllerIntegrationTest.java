package uk.gov.hmcts.reform.fprl;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(MockitoJUnitRunner.class)
public class DocumentGeneratorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void givenTemplateShouldGeneratePdf_VerifyResponse() throws Exception {

        final String EXPC_URL = "http://dm-store-aat.service.core-compute-aat.internal/documents/*";
        final String MIME_TYPE = "application/pdf";
        //when
        var response = this.mockMvc.perform(MockMvcRequestBuilders.post("/generatePdf")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        response.andExpect(status().isCreated());
        response.andExpect((ResultMatcher) jsonPath("$.url", Matchers.contains(EXPC_URL)))
           .andExpect(jsonPath("$.mimeType").value(MIME_TYPE));

    }

}
