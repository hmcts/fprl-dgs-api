package uk.gov.hmcts.reform.fprl.documentgenerator.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.pdf.ByteArrayMultipartFile;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.FileUploadResponse;
import uk.gov.hmcts.reform.fprl.documentgenerator.exception.DocumentStorageException;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.EvidenceManagementService;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_PDF;

@Service
@Slf4j
@AllArgsConstructor
@ConditionalOnProperty(value = "evidence-management-api.service.stub.enabled", havingValue = "false")
public class EvidenceManagementServiceImpl implements EvidenceManagementService {

    private final CaseDocumentClient caseDocumentClient;

    private final AuthTokenGenerator authTokenGenerator;

    @Override
    public FileUploadResponse storeDocumentAndGetInfo(byte[] documentByte, String authorizationToken, String fileName) {
        ByteArrayMultipartFile file = ByteArrayMultipartFile.builder().content(documentByte).name(fileName)
            .contentType(APPLICATION_PDF).build();

        try {
            String serviceAuthorisation = authTokenGenerator.generate();
            UploadResponse caseDocsUploadResponse = caseDocumentClient.uploadDocuments(
                authorizationToken,
                serviceAuthorisation,
                "C100",
                "PRIVATELAW",
                singletonList(file));

            return createFileUploadResponse(caseDocsUploadResponse.getDocuments().get(0));
        } catch (Exception e) {
            log.error("Case Docs service failed to upload documents... {}", e.getMessage());
            if (null != file) {
                log.info("Case Docs file Name: {}", file.getName());
                log.info("Case Docs file OriginalName: {}", file.getOriginalFilename());
            }

            throw new DocumentStorageException("Case Docs Failed to store document", e);
        }
    }

    private FileUploadResponse createFileUploadResponse(Document document) {
        FileUploadResponse fileUploadResponse = new FileUploadResponse(HttpStatus.OK);

        fileUploadResponse.setFileUrl(document.links.self.href);
        fileUploadResponse.setFileName(document.originalDocumentName);
        fileUploadResponse.setCreatedOn(document.createdOn.toString());
        fileUploadResponse.setMimeType(document.mimeType);
        fileUploadResponse.setDocumentHash(document.hashToken);

        return fileUploadResponse;
    }
}
