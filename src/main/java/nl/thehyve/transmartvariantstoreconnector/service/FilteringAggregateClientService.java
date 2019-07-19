package nl.thehyve.transmartvariantstoreconnector.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.transmartproject.common.client.AggregateClient;
import org.transmartproject.common.dto.ConstraintParameter;
import org.transmartproject.common.dto.Counts;
import org.transmartproject.proxy.service.AggregateClientService;

@Primary
@Service
public class FilteringAggregateClientService extends AggregateClientService {

    private Logger log = LoggerFactory.getLogger(FilteringAggregateClientService.class);

    private CaseClientService caseClientService;

    FilteringAggregateClientService(AggregateClient aggregateClient, CaseClientService caseClientService) {
        super(aggregateClient);
        this.caseClientService = caseClientService;
        log.info("Filtering aggregate client service initialised.");
    }

    @Override
    public Counts fetchCounts(ConstraintParameter constraint) {
        log.info("Fetch counts for constraint: {}", constraint);
        return super.fetchCounts(constraint);
    }

}
