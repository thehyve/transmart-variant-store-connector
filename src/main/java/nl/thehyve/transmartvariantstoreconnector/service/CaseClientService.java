package nl.thehyve.transmartvariantstoreconnector.service;

import nl.thehyve.transmartvariantstoreconnector.client.CaseClient;
import nl.thehyve.transmartvariantstoreconnector.dto.Case;
import nl.thehyve.transmartvariantstoreconnector.dto.ListingArguments;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CaseClientService {

    private CaseClient caseClient;

    CaseClientService(CaseClient caseClient) {
        this.caseClient = caseClient;
    }

    public List<Case> fetchCases(ListingArguments variantProperties) {
        return caseClient.getCasesForVariant(variantProperties);
    }

}
