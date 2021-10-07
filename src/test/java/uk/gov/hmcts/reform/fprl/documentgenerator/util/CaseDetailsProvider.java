package uk.gov.hmcts.reform.fprl.documentgenerator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.CaseDetails;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.PlaceholderData;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseDetailsProvider {

    public static CaseDetails empty() {
        return withData(new HashMap<>());
    }

    public static CaseDetails withData(Map<String, Object> data) {
        return CaseDetails.builder().caseData(data).build();
    }
}
