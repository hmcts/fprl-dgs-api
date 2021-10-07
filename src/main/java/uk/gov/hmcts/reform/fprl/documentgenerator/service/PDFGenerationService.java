package uk.gov.hmcts.reform.fprl.documentgenerator.service;

import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.PlaceholderData;

import java.util.Map;

public interface PDFGenerationService {

    byte[] generate(String templateName, PlaceholderData placeholders);
}
