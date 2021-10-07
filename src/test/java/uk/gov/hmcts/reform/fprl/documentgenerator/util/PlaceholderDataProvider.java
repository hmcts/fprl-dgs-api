package uk.gov.hmcts.reform.fprl.documentgenerator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.CaseDetails;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.PlaceholderData;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceholderDataProvider {

    public static PlaceholderData empty() {
        return withData(new HashMap<>());
    }

    public static PlaceholderData withData(Map<String, Object> data) {
        return PlaceholderData.builder().caseDetails(CaseDetailsProvider.withData(data)).build();
    }
}
