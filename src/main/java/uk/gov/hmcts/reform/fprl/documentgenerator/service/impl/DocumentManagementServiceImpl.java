package uk.gov.hmcts.reform.fprl.documentgenerator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.fprl.documentgenerator.config.TemplatesConfiguration;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.PlaceholderData;
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
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final TemplatesConfiguration templatesConfiguration;

    @Override
    public GeneratedDocumentInfo generateAndStoreDocument(
        String templateName,
        PlaceholderData placeholders,
        String userAuthToken) {
        String fileName = templatesConfiguration.getFileNameByTemplateName(templateName);

        return getGeneratedDocumentInfo(templateName, placeholders, userAuthToken);
    }

    @Override
    public GeneratedDocumentInfo generateAndStoreDraftDocument(
        String templateName,
        PlaceholderData placeholders,
        String userAuthToken) {
        String fileName = templatesConfiguration.getFileNameByTemplateName(templateName);

        placeholders.getCaseDetails().getCaseData().put(IS_DRAFT, true);

        return getGeneratedDocumentInfo(templateName, placeholders, userAuthToken);
    }

    private GeneratedDocumentInfo getGeneratedDocumentInfo(
        String templateName,
        PlaceholderData placeholders,
        String userAuthToken) {
        log.debug("Generate and Store Document requested with templateName [{}], placeholders of size [{}]",
            templateName, placeholders.getCaseDetails().getCaseData().size());
        String caseId = placeholders.getCaseDetails().getCaseId();
        if (caseId == null) {
            log.warn("caseId is null for template \"" + templateName + "\"");
        } else {
            log.info("Generating document for case Id {}", caseId);
        }

        placeholders.getCaseDetails().getCaseData().put(
            CURRENT_DATE_KEY,
            new SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                .format(Date.from(clock.instant()))
        );

        byte[] generatedDocument = generateDocument(templateName, placeholders);
        log.info("Document generated for case Id {}", caseId);

        return storeDocument(generatedDocument, userAuthToken);
    }

    private GeneratedDocumentInfo storeDocument(byte[] document, String userAuthToken) {
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
    public byte[] generateDocument(String templateName, PlaceholderData placeholders) {
        log.info("Generate document [{}], placeholders of size[{}]", templateName, placeholders.getCaseDetails().getCaseData().size());

        return pdfGenerationService.generate(templateName, placeholders);
    }
}
