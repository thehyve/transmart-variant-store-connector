package nl.thehyve.transmartvariantstoreconnector.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.transmartproject.common.client.AggregateClient;
import org.transmartproject.common.dto.CategoricalValueAggregates;
import org.transmartproject.common.dto.ConstraintParameter;
import org.transmartproject.common.dto.Counts;
import org.transmartproject.common.dto.NumericalValueAggregates;
import org.transmartproject.proxy.service.AggregateClientService;

import java.util.Map;

@Primary
@Service
public class FilteringAggregateClientService extends AggregateClientService {

    private Logger log = LoggerFactory.getLogger(FilteringAggregateClientService.class);

    private BiomarkerConstraintTranslatorService biomarkerConstraintTranslator;

    FilteringAggregateClientService(AggregateClient aggregateClient, BiomarkerConstraintTranslatorService biomarkerConstraintTranslator) {
        super(aggregateClient);
        this.biomarkerConstraintTranslator = biomarkerConstraintTranslator;
        log.info("Filtering aggregate client service initialised.");
    }

    @Override
    public Counts fetchCounts(ConstraintParameter constraint) {
        log.info("Fetch counts for constraint: {}", constraint);
        return super.fetchCounts(biomarkerConstraintTranslator.apply(constraint));
    }

    @Override
    public Map<String, Counts> fetchCountsPerConcept(ConstraintParameter constraint) {
        return super.fetchCountsPerConcept(biomarkerConstraintTranslator.apply(constraint));
    }

    @Override
    public Map<String, Counts> fetchCountsPerStudy(ConstraintParameter constraint) {
        return super.fetchCountsPerStudy(biomarkerConstraintTranslator.apply(constraint));
    }

    @Override
    public Map<String, Map<String, Counts>> fetchCountsPerStudyAndConcept(ConstraintParameter constraint) {
        return super.fetchCountsPerStudyAndConcept(biomarkerConstraintTranslator.apply(constraint));
    }

    @Override
    public Map<String, NumericalValueAggregates> fetchNumericalAggregatesPerConcept(ConstraintParameter constraint) {
        return super.fetchNumericalAggregatesPerConcept(biomarkerConstraintTranslator.apply(constraint));
    }

    @Override
    public Map<String, CategoricalValueAggregates> fetchCategoricalAggregatesPerConcept(ConstraintParameter constraint) {
        return super.fetchCategoricalAggregatesPerConcept(biomarkerConstraintTranslator.apply(constraint));
    }

}
