package nl.thehyve.transmartvariantstoreconnector.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.transmartproject.common.client.PatientSetClient;
import org.transmartproject.common.constraint.Constraint;
import org.transmartproject.common.dto.PatientSetResult;
import org.transmartproject.proxy.service.PatientSetClientService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Primary
@Service
public class FilteringPatientSetClientService extends PatientSetClientService {

    private Logger log = LoggerFactory.getLogger(FilteringPatientSetClientService.class);

    private BiomarkerConstraintTranslatorService biomarkerConstraintTranslator;

    FilteringPatientSetClientService(PatientSetClient patientSetClient, BiomarkerConstraintTranslatorService biomarkerConstraintTranslator) {
        super(patientSetClient);
        this.biomarkerConstraintTranslator = biomarkerConstraintTranslator;
        log.info("Filtering patient set client service initialised.");
    }

    @Override
    public PatientSetResult createPatientSet(@NotBlank String name, Boolean reuse, @Valid @NotNull Constraint constraint) {
        Constraint translatedConstraint = biomarkerConstraintTranslator.build(constraint);
        log.info("Translated constraint: {}", translatedConstraint);
        return super.createPatientSet(name, reuse, translatedConstraint);
    }

}
