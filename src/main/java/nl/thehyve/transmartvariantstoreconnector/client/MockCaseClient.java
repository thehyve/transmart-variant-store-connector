package nl.thehyve.transmartvariantstoreconnector.client;

import nl.thehyve.transmartvariantstoreconnector.dto.Case;
import nl.thehyve.transmartvariantstoreconnector.dto.ListingArguments;

import java.util.Arrays;
import java.util.List;

public class MockCaseClient implements CaseClient {

    @Override
    public List<Case> getCasesForVariant(ListingArguments variantProperties) {
        return Arrays.asList(
            Case.builder().identifier("SUBJ1").projectId("EHR").build(),
            Case.builder().identifier("SUBJ2").projectId("EHR").build()
        );
    }
}
