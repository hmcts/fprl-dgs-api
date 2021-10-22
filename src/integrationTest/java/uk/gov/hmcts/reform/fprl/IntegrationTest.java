package uk.gov.hmcts.reform.fprl;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.assertj.core.util.Strings;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.UUID;
import javax.annotation.PostConstruct;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public abstract class IntegrationTest {

    private static final String GENERIC_PASSWORD = "Nagoya0102";

    @Value("${document.generator.base.uri}")
    protected String fprlDocumentGeneratorBaseURI;

    @Value("${fprl.document.generator.uri}")
    protected String fprlDocumentGeneratorURI;

    @Value("${fprl.document.generateDraft.uri}")
    protected String fprlDocumentGenerateDraftURI;

    @Value("${document.management.store.baseUrl}")
    protected String documentManagementURL;

    @Value("${http.proxy:#{null}}")
    protected String httpProxy;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamUtils idamTestSupportUtil;

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration;

    private static String userToken = null;
    private String username;

    public IntegrationTest() {
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }

    @PostConstruct
    public void init() {
        if (!Strings.isNullOrEmpty(httpProxy)) {
            configProxyHost();
        }
    }

    public Response callprlDocumentGenerator(String requestBody) {
        return DocumentGeneratorUtil.generatePDF(requestBody,
            fprlDocumentGeneratorURI,
            getUserToken());
    }

    private synchronized String getUserToken() {
        username = "fprl_caseworker_solicitor@mailinator.com";

        if (userToken == null) {
            idamTestSupportUtil.createCaseworkerUserInIdam(username, GENERIC_PASSWORD);

            userToken = idamTestSupportUtil.generateUserTokenWithNoRoles(username, GENERIC_PASSWORD);
        }

        return userToken;
    }

    private void configProxyHost() {
        try {
            URL proxy = new URL(httpProxy);
            if (InetAddress.getByName(proxy.getHost()).isReachable(2000)) {
                System.setProperty("http.proxyHost", proxy.getHost());
                System.setProperty("http.proxyPort", Integer.toString(proxy.getPort()));
                System.setProperty("https.proxyHost", proxy.getHost());
                System.setProperty("https.proxyPort", Integer.toString(proxy.getPort()));
            } else {
                throw new IOException();
            }
        } catch (IOException e) {
            log.error("Error setting up proxy - are you connected to the VPN?", e);
            throw new RuntimeException(e);
        }
    }
}
