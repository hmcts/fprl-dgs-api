package uk.gov.hmcts.reform.fprl.documentgenerator.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.request.GenerateDocumentRequest;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.fprl.documentgenerator.service.DocumentManagementService;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/version/1")
@Api(value = "Document Generation", tags = {"Document Generation"})
@RequiredArgsConstructor
@Slf4j
public class DocumentGeneratorController {

    private final DocumentManagementService documentManagementService;

    @ApiOperation(value = "Generate PDF document based on the supplied template name and placeholder texts and saves "
        + "it in the evidence management.", tags = {"Document Generation"})
    @ApiResponses({
        @ApiResponse(code = 200, message = "PDF was generated successfully and stored in the evidence management."
            + " Returns the url to the stored document.", response = String.class),
        @ApiResponse(code = 400, message = "Returned when input parameters are invalid or template not found",
            response = String.class),
        @ApiResponse(code = 500, message = "Returned when there is an unknown server error",
            response = String.class),
        @ApiResponse(code = 503, message = "Returned when the PDF Service or Evidence Management Client Api "
            + "cannot be reached", response = String.class)
    })
    @PostMapping("/generatePDF")
    public GeneratedDocumentInfo generateAndUploadPdf(
        @RequestHeader(value = "Authorization") String userAuthToken,
        @RequestBody @Valid GenerateDocumentRequest request) {
        log.info("Document generation [{}], placeholders map of size[{}]",
            request.getTemplate(), request.getValues().getCaseDetails().getCaseData().size());

        return documentManagementService.generateAndStoreDocument(
            request.getTemplate(),
            request.getValues(),
            userAuthToken
        );
    }

    @ApiOperation(value = "Generate draft PDF document based on the supplied template name and placeholder texts and "
        + "saves it in the evidence management.", tags = {"Document Generation"})
    @ApiResponses({
        @ApiResponse(code = 200, message = "PDF was generated successfully and stored in the evidence management."
            + " Returns the url to the stored document.", response = String.class),
        @ApiResponse(code = 400, message = "Returned when input parameters are invalid or template not found",
            response = String.class),
        @ApiResponse(code = 500, message = "Returned when there is an unknown server error",
            response = String.class),
        @ApiResponse(code = 503, message = "Returned when the PDF Service or Evidence Management Client Api "
            + "cannot be reached", response = String.class)
    })
    @PostMapping("/generateDraftPDF")
    public GeneratedDocumentInfo generateAndUploadDraftPdf(
        @RequestHeader(value = "Authorization") String userAuthToken,
        @RequestBody @Valid GenerateDocumentRequest request) {
        log.info("Draft document generation [{}], placeholders map of size[{}]",
            request.getTemplate(), request.getValues().getCaseDetails().getCaseData().size());

        return documentManagementService.generateAndStoreDraftDocument(
            request.getTemplate(),
            request.getValues(),
            userAuthToken
        );
    }
}
