package uk.gov.hmcts.reform.fprl.documentgenerator.management.test.stub;

public interface DocumentDownloadService {
    byte[] getDocument(String fileName);
}
