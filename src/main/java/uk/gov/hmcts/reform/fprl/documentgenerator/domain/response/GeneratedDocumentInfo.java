package uk.gov.hmcts.reform.fprl.documentgenerator.domain.response;

import lombok.Data;

@Data
public class GeneratedDocumentInfo {
    private String url;
    private String mimeType;
    private String createdOn;
}
