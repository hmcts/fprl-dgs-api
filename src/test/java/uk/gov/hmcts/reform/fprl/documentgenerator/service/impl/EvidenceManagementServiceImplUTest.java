package uk.gov.hmcts.reform.fprl.documentgenerator.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.FileUploadResponse;
import uk.gov.hmcts.reform.fprl.documentgenerator.exception.DocumentStorageException;
import uk.gov.hmcts.reform.fprl.documentgenerator.util.NullOrEmptyValidator;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PowerMockIgnore({"com.microsoft.applicationinsights.*","com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({EvidenceManagementServiceImpl.class, NullOrEmptyValidator.class})
public class EvidenceManagementServiceImplUTest {
    private static final String TEST_DEFAULT_NAME_FOR_PDF_FILE = "FPRLDocument.pdf";
    private static final String TEST_AUTH_TOKEN = "testAuthToken";
    private static final String TEST_SERVICE_TOKEN = "testServiceToken";
    private static final String TEST_CASE_TYPE_ID = "C100";
    private static final String TEST_JURISDICTION = "PRIVATELAW";
    private static final String TEST_DOCUMENT_LINK = "linkToDocument";
    private static final String TEST_HASH_TOKEN = "hashToken";
    private static final String TEST_MIME_TYPE = "mimeType";

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private AuthTokenGenerator serviceTokenGenerator;

    @InjectMocks
    private EvidenceManagementServiceImpl classUnderTest;

    @Test
    public void storeDocumentAndGetInfoShouldCallUploadDocumentManagementClient() {
        final byte[] data = new byte[] {1};

        UploadResponse uploadResponse = mock(UploadResponse.class);
        when(uploadResponse.getDocuments()).thenReturn(List.of(mockCaseDocsDocuments()));
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(caseDocumentClient.uploadDocuments(eq(TEST_AUTH_TOKEN),
            eq(TEST_SERVICE_TOKEN),
            eq(TEST_CASE_TYPE_ID),
            eq(TEST_JURISDICTION),
            any())).thenReturn(uploadResponse);

        FileUploadResponse actualUploadedResponse = classUnderTest.storeDocumentAndGetInfo(
            data,
            TEST_AUTH_TOKEN,
            TEST_DEFAULT_NAME_FOR_PDF_FILE);

        verify(caseDocumentClient, times(1))
            .uploadDocuments(eq(TEST_AUTH_TOKEN),
                eq(TEST_SERVICE_TOKEN),
                eq(TEST_CASE_TYPE_ID),
                eq(TEST_JURISDICTION),
                any());

        assertEquals(actualUploadedResponse.getFileUrl(), TEST_DOCUMENT_LINK);
    }

    @Test
    public void givenStoreDocumentAndGetInfo_IsSuccessful_ThenUploadResponseShouldBeMappedToFileUploadResponse() {
        final byte[] data = new byte[] {1};

        UploadResponse uploadResponse = mock(UploadResponse.class);
        when(uploadResponse.getDocuments()).thenReturn(List.of(mockCaseDocsDocuments()));
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(caseDocumentClient.uploadDocuments(eq(TEST_AUTH_TOKEN),
            eq(TEST_SERVICE_TOKEN),
            eq(TEST_CASE_TYPE_ID),
            eq(TEST_JURISDICTION),
            any())).thenReturn(uploadResponse);

        FileUploadResponse actualUploadedResponse = classUnderTest.storeDocumentAndGetInfo(
            data,
            TEST_AUTH_TOKEN,
            TEST_DEFAULT_NAME_FOR_PDF_FILE);

        assertEquals(actualUploadedResponse.getFileUrl(), TEST_DOCUMENT_LINK);
        assertEquals(actualUploadedResponse.getDocumentHash(), TEST_HASH_TOKEN);
        assertEquals(actualUploadedResponse.getFileName(), TEST_DEFAULT_NAME_FOR_PDF_FILE);
        assertEquals(actualUploadedResponse.getMimeType(), TEST_MIME_TYPE);
    }

    @Test(expected = DocumentStorageException.class)
    public void givenStoreDocumentAndGetInfoThrowsException_whenUnprocessableEntity_thenThrowDocumentStorageException() {
        final byte[] data = new byte[] {1};

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(caseDocumentClient.uploadDocuments(any(), eq(TEST_SERVICE_TOKEN), any(), any(), any()))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY));

        classUnderTest.storeDocumentAndGetInfo(data, TEST_AUTH_TOKEN, TEST_DEFAULT_NAME_FOR_PDF_FILE);
    }

    @Test(expected = DocumentStorageException.class)
    public void givenStoreDocumentAndGetInfoThrowsException_NoDocuments_thenThrowDocumentStorageException() {
        UploadResponse uploadResponse = mock(UploadResponse.class);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(caseDocumentClient.uploadDocuments(any(), eq(TEST_SERVICE_TOKEN), any(), any(), any()))
            .thenReturn(uploadResponse);

        classUnderTest.storeDocumentAndGetInfo(null, TEST_AUTH_TOKEN, TEST_DEFAULT_NAME_FOR_PDF_FILE);
    }

    private Document mockCaseDocsDocuments() {
        Document.Link link = new Document.Link();
        link.href = TEST_DOCUMENT_LINK;

        Document.Links links = new Document.Links();
        links.self = link;

        return Document.builder()
            .createdOn(new Date())
            .links(links)
            .hashToken(TEST_HASH_TOKEN)
            .mimeType(TEST_MIME_TYPE)
            .originalDocumentName(TEST_DEFAULT_NAME_FOR_PDF_FILE)
            .build();
    }
}
