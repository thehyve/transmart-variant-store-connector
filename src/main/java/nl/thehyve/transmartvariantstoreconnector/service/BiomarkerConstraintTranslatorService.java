package nl.thehyve.transmartvariantstoreconnector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.thehyve.transmartvariantstoreconnector.dto.Case;
import nl.thehyve.transmartvariantstoreconnector.dto.ListingArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.transmartproject.common.client.PatientSetClient;
import org.transmartproject.common.constraint.BiomarkerConstraint;
import org.transmartproject.common.constraint.Constraint;
import org.transmartproject.common.constraint.ConstraintRewriter;
import org.transmartproject.common.constraint.PatientSetConstraint;
import org.transmartproject.common.dto.ConstraintParameter;
import org.transmartproject.common.dto.PatientSetResult;
import org.transmartproject.common.dto.Query;
import org.transmartproject.proxy.service.ResponseEntityHelper;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Translates biomarker constraints into subject set constraints,
 * by executing the biomarker constraint as variant queries on the variant store
 * and saving the resulting {@link Case} sets as {@link PatientSetResult}
 * and replacing the {@link BiomarkerConstraint} with a {@link PatientSetConstraint}.
 */
@Service
public class BiomarkerConstraintTranslatorService extends ConstraintRewriter {

    private Logger log = LoggerFactory.getLogger(BiomarkerConstraintTranslatorService.class);

    private CaseClientService caseClientService;
    private PatientSetClient patientSetClient;

    BiomarkerConstraintTranslatorService(CaseClientService caseClientService,
                                         PatientSetClient patientSetClient) {
        log.info("Biomarker constraint translator initialised.");
        this.caseClientService = caseClientService;
        this.patientSetClient = patientSetClient;
    }

    @Override
    public Constraint build(BiomarkerConstraint constraint) {
        ListingArguments variantProperties = new ObjectMapper().convertValue(constraint.getParams(), ListingArguments.class);
        Set<String> subjectIds = caseClientService.fetchCases(variantProperties).stream()
            .map(Case::getIdentifier).collect(Collectors.toSet());
        PatientSetResult patientSet = ResponseEntityHelper.unwrap(patientSetClient.createPatientSet(
            "Biomarker subject set",
            true,
            PatientSetConstraint.builder().subjectIds(subjectIds).build()
            ));
        return PatientSetConstraint.builder()
            .patientSetId(patientSet.getId())
            .build();
    }

    /**
     * Apply the biomarker constraint translator to a {@link ConstraintParameter}.
     *
     * @param constraintParameter the input constraint parameter
     * @return a new constraint parameter with the translated constraint.
     */
    public ConstraintParameter apply(ConstraintParameter constraintParameter) {
        log.info("Translating constraint: {}", constraintParameter.getConstraint());
        ConstraintParameter result = ConstraintParameter.builder()
            .constraint(build(constraintParameter.getConstraint()))
            .build();
        log.info("Result: {}", result.getConstraint());
        return result;
    }

    /**
     * Apply the biomarker constraint translator to a {@link Query}.
     *
     * @param query the input query
     * @return a new query with the translated constraint.
     */
    public Query apply(Query query) {
        log.info("Translating constraint: {}", query.getConstraint());
        Query result = Query.builder()
            .type(query.getType())
            .constraint(build(query.getConstraint()))
            .sort(query.getSort())
            .build();
        log.info("Result: {}", result.getConstraint());
        return result;
    }

}
