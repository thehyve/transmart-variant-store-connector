package nl.thehyve.transmartvariantstoreconnector.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.math.BigInteger;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ListingArguments {
    @Pattern(regexp = "[1-22]|X|Y")
    private String chromosome;

    @Positive
    private BigInteger startPosition;

    @Positive
    private BigInteger endPosition;

    private String cancerEntity;

    private String sampleId;

    private String geneId;

    private String consequenceType;
}
