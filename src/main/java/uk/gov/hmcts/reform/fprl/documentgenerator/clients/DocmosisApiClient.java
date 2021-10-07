package uk.gov.hmcts.reform.fprl.documentgenerator.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.PdfDocumentRequest;

@FeignClient(name = "docmosis-api-client", url = "${docmosis.service.pdf-service.baseUrl}")
public interface DocmosisApiClient {

    @PostMapping(
        value = "${docmosis.service.pdf-service.renderEndpoint}",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<byte[]> generatePdf(@RequestBody final PdfDocumentRequest docmosisRequest);
}
