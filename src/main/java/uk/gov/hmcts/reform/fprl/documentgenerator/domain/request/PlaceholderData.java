package uk.gov.hmcts.reform.fprl.documentgenerator.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceholderData {

    @JsonProperty("caseDetails")
    private CaseDetails caseDetails;
}
