package uk.gov.hmcts.reform.fprl.documentgenerator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Modifier;
import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestConsts {
    private static final String TEST_TEMPLATE_ID = "a-certain-template";
    public static final String TEST_TEMPLATE_NAME = "fileName.pdf";
    public static final String TEST_AUTH_TOKEN = "someToken";
    public static final String TEST_S2S_TOKEN = "s2s-token";
    public static final String TEST_URL = "someUrl";
    public static final String TEST_MIME_TYPE = "someMimeType";

    public static final byte[] TEST_GENERATED_DOCUMENT = new byte[]{1};

    public static final Date TEST_DATE = new Date();
}
