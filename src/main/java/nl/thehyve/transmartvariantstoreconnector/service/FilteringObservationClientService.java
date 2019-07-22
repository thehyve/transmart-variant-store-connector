package nl.thehyve.transmartvariantstoreconnector.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.transmartproject.common.client.ObservationClient;
import org.transmartproject.common.dto.Hypercube;
import org.transmartproject.common.dto.Query;
import org.transmartproject.proxy.service.ObservationClientService;

@Primary
@Service
public class FilteringObservationClientService extends ObservationClientService {

    private Logger log = LoggerFactory.getLogger(FilteringObservationClientService.class);

    private BiomarkerConstraintTranslatorService biomarkerConstraintTranslator;

    FilteringObservationClientService(ObservationClient observationClient, BiomarkerConstraintTranslatorService biomarkerConstraintTranslator) {
        super(observationClient);
        this.biomarkerConstraintTranslator = biomarkerConstraintTranslator;
        log.info("Filtering observation client service initialised.");
    }

    @Override
    public Hypercube fetchObservations(Query query) {
        return super.fetchObservations(biomarkerConstraintTranslator.apply(query));
    }

}
