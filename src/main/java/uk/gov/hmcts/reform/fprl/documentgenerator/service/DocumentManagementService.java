package uk.gov.hmcts.reform.fprl.documentgenerator.service;

import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.PlaceholderData;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.GeneratedDocumentInfo;

import java.util.Map;

public interface DocumentManagementService {

    GeneratedDocumentInfo generateAndStoreDocument(
        String templateName,
        PlaceholderData placeholders,
        String userAuthToken
    );

    GeneratedDocumentInfo generateAndStoreDraftDocument(
        String templateName,
        PlaceholderData placeholders,
        String userAuthToken
    );

    byte[] generateDocument(String templateName, PlaceholderData placeholders);
}
