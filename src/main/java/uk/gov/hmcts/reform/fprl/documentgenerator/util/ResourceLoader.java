package uk.gov.hmcts.reform.fprl.documentgenerator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import uk.gov.hmcts.reform.fprl.documentgenerator.exception.ErrorLoadingTemplateException;

@SuppressWarnings("squid:S1118")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceLoader {

    public static byte[] loadResource(String path) {
        NullOrEmptyValidator.requireNonBlank(path);

        try {
            return IOUtils.toByteArray(ResourceLoader.class.getClassLoader().getResourceAsStream(path));
        } catch (Exception e) {
            throw new ErrorLoadingTemplateException(String.format("Couldn't load template with the name %s", path), e);
        }
    }
}
