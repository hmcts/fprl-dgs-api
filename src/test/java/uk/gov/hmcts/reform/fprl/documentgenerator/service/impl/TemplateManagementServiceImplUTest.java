package uk.gov.hmcts.reform.fprl.documentgenerator.service.impl;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.hmcts.reform.fprl.documentgenerator.exception.ErrorLoadingTemplateException;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.TemplateManagementService;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

public class TemplateManagementServiceImplUTest {

    private static final String TEST_TEMPLATE_NAME = "testtemplate";
    private static final String RESOURCE_PATH = "data/templates/testtemplate.html";

    private TemplateManagementService classUnderTest = new TemplateManagementServiceImpl();

    @Test
    @Ignore
    public void shouldReturnExistingTemplateAsBytes() throws IOException {
        // DALEC - NOT NEEDED? TESTING HTML
        try (InputStream resourceAsStream = TemplateManagementServiceImplUTest.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
            byte[] testTemplateBytes = IOUtils.toByteArray(resourceAsStream);

            byte[] actualTemplateBytes = classUnderTest.getTemplateByName(TEST_TEMPLATE_NAME);

            assertThat(actualTemplateBytes, is(notNullValue()));
            assertThat(actualTemplateBytes.length, is(greaterThan(0)));
            assertThat(actualTemplateBytes, equalTo(testTemplateBytes));
        }
    }

    @Test
    public void shouldThrowExceptionWhenTemplateNameDoesNotExist() {
        ErrorLoadingTemplateException errorLoadingTemplateException = assertThrows(ErrorLoadingTemplateException.class, () -> {
            classUnderTest.getTemplateByName("non-existent-template");
        });

        assertThat(errorLoadingTemplateException.getMessage(), containsString("non-existent-template"));
    }

}
