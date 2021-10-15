package uk.gov.hmcts.reform.fprl;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.gov.hmcts.reform.fprl.documentgenerator.category.SmokeTest;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;

@Category(SmokeTest.class)
public class DocumentGeneratorSmokeTest extends IntegrationTest {

    @Test
    public void shouldHaveHealthyService() {
        when().get(fprlDocumentGeneratorBaseURI + "/health")
            .then().statusCode(200).body("status", is("UP"));
    }

}
