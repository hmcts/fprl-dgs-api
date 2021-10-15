package uk.gov.hmcts.reform.fprl.documentgenerator.domain;

import lombok.Data;

@Data
public class CcdCollectionMember<T> {
    private String id;
    private T value;
}
