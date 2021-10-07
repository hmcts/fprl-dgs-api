package uk.gov.hmcts.reform.fprl.documentgenerator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.fprl.documentgenerator.config.TemplatesConfiguration;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.DocumentManagementService;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.PDFGenerationService;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.emptyMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentManagementServiceImpl implements DocumentManagementService {

    private static final String CURRENT_DATE_KEY = "current_date";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSS";

    private static final String DRAFT_PREFIX = "Draft";
    private static final String IS_DRAFT = "isDraft";

    private final Clock clock = Clock.systemDefaultZone();

    private final PDFGenerationService pdfGenerationService;
    private final CaseDocumentClientApi caseDocumentClientApi;
    private final ServiceAuthTokenGenerator serviceAuthTokenGenerator;
    private final TemplatesConfiguration templatesConfiguration;

    @Override
    public GeneratedDocumentInfo generateAndStoreDocument(
        String templateName,
        Map<String, Object> placeholders,
        String userAuthToken) {
        String fileName = templatesConfiguration.getFileNameByTemplateName(templateName);

        return getGeneratedDocumentInfo(templateName, placeholders, userAuthToken, fileName);
    }

    @Override
    public GeneratedDocumentInfo generateAndStoreDraftDocument(
        String templateName,
        Map<String, Object> placeholders,
        String userAuthToken) {
        String fileName = templatesConfiguration.getFileNameByTemplateName(templateName);
        if (!fileName.startsWith(DRAFT_PREFIX)) {
            fileName = String.join("", DRAFT_PREFIX, fileName);
        }
        placeholders.put(IS_DRAFT, true);

        return getGeneratedDocumentInfo(templateName, placeholders, userAuthToken, fileName);
    }

    private GeneratedDocumentInfo getGeneratedDocumentInfo(
        String templateName,
        Map<String, Object> placeholders,
        String userAuthToken,
        String fileName) {
        log.debug("Generate and Store Document requested with templateName [{}], placeholders of size [{}]",
            templateName, placeholders.size());
        String caseId = getCaseId(placeholders);
        if (caseId == null) {
            log.warn("caseId is null for template \"" + templateName + "\"");
        }

        log.info("Generating document for case Id {}", caseId);

        placeholders.put(
            CURRENT_DATE_KEY,
            new SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                .format(Date.from(clock.instant()))
        );

        byte[] generatedDocument = generateDocument(templateName, placeholders);
        log.info("Document generated for case Id {}", caseId);

        return storeDocument(generatedDocument, userAuthToken, fileName);
    }

    private GeneratedDocumentInfo storeDocument(byte[] document, String userAuthToken, String fileName) {
        log.debug("Store document requested with document of size [{}]", document.length);

        // we need to map byte[] document to List<MultipartFile> files
        DocumentUploadRequest documentUploadRequest = new DocumentUploadRequest(
            "PUBLIC", "C100", "Family Law", new ArrayList<>()
        );

        UploadResponse uploadResponse = caseDocumentClientApi
            .uploadDocuments(
                userAuthToken,
                serviceAuthTokenGenerator.generate(),
                documentUploadRequest
            );

        Document uploadedDocument = uploadResponse.getDocuments().get(0);

        return GeneratedDocumentInfo.builder()
            .createdOn(uploadedDocument.createdOn.toString())
            .mimeType(uploadedDocument.mimeType)
            .url(uploadedDocument.links.self.href)
            .build();
    }

    @Override
    public byte[] generateDocument(String templateName, Map<String, Object> placeholders) {
        log.info("Generate document [{}], placeholders of size[{}]", templateName, placeholders.size());

        return pdfGenerationService.generate(templateName, placeholders);
    }

    private String getCaseId(Map<String, Object> placeholders) {
        Map<String, Object> caseDetails = (Map<String, Object>) placeholders.getOrDefault("caseDetails", emptyMap());

        return (String) caseDetails.get("id");
    }
}
