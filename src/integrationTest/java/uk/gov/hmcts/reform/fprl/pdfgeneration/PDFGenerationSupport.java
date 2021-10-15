package uk.gov.hmcts.reform.fprl.pdfgeneration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PDFGenerationSupport {

    static List<Object[]> getTestScenarios() {
        List<String> basicTestData = Arrays.asList(
            "DA-granted-letter" // Test document as POC for generations, remove when real templates are added.
        );

        return basicTestData.stream()
            .map(s -> new Object[]{s})
            .collect(Collectors.toList());
    }
}
