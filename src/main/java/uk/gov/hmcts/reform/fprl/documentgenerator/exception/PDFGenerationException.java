package uk.gov.hmcts.reform.fprl.documentgenerator.exception;

public class PDFGenerationException extends RuntimeException {

    public PDFGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
