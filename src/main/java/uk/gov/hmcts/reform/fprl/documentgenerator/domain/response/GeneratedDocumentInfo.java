package uk.gov.hmcts.reform.fprl.documentgenerator.domain.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeneratedDocumentInfo {
    private String url;
    private String mimeType;
    private String createdOn;
}
