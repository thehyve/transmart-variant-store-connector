package nl.thehyve.transmartvariantstoreconnector.client;

import feign.QueryMap;
import feign.RequestLine;
import nl.thehyve.transmartvariantstoreconnector.dto.Case;
import nl.thehyve.transmartvariantstoreconnector.dto.ListingArguments;

import javax.validation.Valid;
import java.util.List;

public interface CaseClient {

    /**
     * GET  /cases?chromosome={}&startPosition={}&endPosition={}&cancerEntity={}&sampleId={}&geneId={}&consequenceType={}
     *
     * Get the cases with a variant that matches the criteria in variantProperties.
     *
     * @param variantProperties the variant properties to match.
     * @return the ResponseEntity with status 200 (OK) and the list of matching cases.
     */
    @RequestLine("GET /cases")
    List<Case> getCasesForVariant(@Valid @QueryMap ListingArguments variantProperties);

}
