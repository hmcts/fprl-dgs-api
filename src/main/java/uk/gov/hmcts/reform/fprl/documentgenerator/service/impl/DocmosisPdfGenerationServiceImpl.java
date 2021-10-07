package uk.gov.hmcts.reform.fprl.documentgenerator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fprl.documentgenerator.clients.DocmosisApiClient;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.PdfDocumentRequest;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.PlaceholderData;
import uk.gov.hmcts.reform.fprl.documentgenerator.exception.PDFGenerationException;
import uk.gov.hmcts.reform.fprl.documentgenerator.mapper.TemplateDataMapper;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.PDFGenerationService;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocmosisPdfGenerationServiceImpl implements PDFGenerationService {

    private final DocmosisApiClient docmosisApiClient;

    @Value("${docmosis.service.pdf-service.accessKey}")
    private String docmosisPdfServiceAccessKey;

    @Value("${docmosis.service.pdf-service.devMode}")
    private String docmosisDevMode;

    @Override
    public byte[] generate(String templateName, PlaceholderData placeholders) {
        checkArgument(!isNullOrEmpty(templateName), "document generation template cannot be empty");
        checkNotNull(placeholders, "placeholders map cannot be null");

        Map<String, Object> templateVars = placeholders.getCaseDetails().getCaseData();

        log.info("Call docmosis with template [{}] and placeholders of size [{}]", templateName, templateVars.size());

        try {
            ResponseEntity<byte[]> response = docmosisApiClient.generatePdf(request(templateName, templateVars));

            return response.getBody();
        } catch (Exception e) {
            throw new PDFGenerationException("Failed to request PDF from REST endpoint " + e.getMessage(), e);
        }
    }

    private PdfDocumentRequest request(String templateName, Map<String, Object> templateVars) {
        return PdfDocumentRequest.builder()
            .accessKey(docmosisPdfServiceAccessKey)
            .templateName(templateName)
            .outputName("result.pdf")
            .devMode(docmosisDevMode)
            .data(templateVars).build();
    }
}
