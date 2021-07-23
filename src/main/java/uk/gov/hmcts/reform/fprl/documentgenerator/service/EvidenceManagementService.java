package uk.gov.hmcts.reform.fprl.documentgenerator.service;

import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.FileUploadResponse;

public interface EvidenceManagementService {
    FileUploadResponse storeDocumentAndGetInfo(byte[] document, String authorizationToken, String fileName);
}
