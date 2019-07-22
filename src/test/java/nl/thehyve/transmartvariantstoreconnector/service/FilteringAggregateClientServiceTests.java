package nl.thehyve.transmartvariantstoreconnector.service;

import nl.thehyve.transmartvariantstoreconnector.TransmartVariantStoreConnectorApplication;
import nl.thehyve.transmartvariantstoreconnector.client.CaseClient;
import nl.thehyve.transmartvariantstoreconnector.dto.Case;
import nl.thehyve.transmartvariantstoreconnector.dto.ListingArguments;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.transmartproject.common.client.AggregateClient;
import org.transmartproject.common.client.PatientSetClient;
import org.transmartproject.common.constraint.ConceptConstraint;
import org.transmartproject.common.constraint.Constraint;
import org.transmartproject.common.constraint.PatientSetConstraint;
import org.transmartproject.common.dto.*;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK, classes = TransmartVariantStoreConnectorApplication.class)
@AutoConfigureMockMvc
public class FilteringAggregateClientServiceTests {

    @MockBean
    private CaseClient caseClient;

    @MockBean
    private PatientSetClient patientSetClient;

    @MockBean
    private AggregateClient aggregateClient;

    @Autowired
    private MockMvc mvc;

    private void setupMockData() {
        Constraint conceptConstraint = ConceptConstraint.builder().conceptCode("Dummy").build();
        // return counts for concept constraint on /v2/observations/counts
        ResponseEntity<Counts> countsResponse = ResponseEntity.ok(
            Counts.builder().patientCount(15).build());
        doReturn(countsResponse)
            .when(aggregateClient).counts(new ConstraintParameter(conceptConstraint));
        // return cases for /cases
        List<Case> casesResponse = Arrays.asList(
            Case.builder().identifier("SUBJ1").build());
        doReturn(casesResponse)
            .when(caseClient).getCasesForVariant(ListingArguments.builder().chromosome("12").build());
        // return patient set for /v2/patient_sets
        ResponseEntity<PatientSetResult> patientSetResponse = ResponseEntity.status(HttpStatus.CREATED).body(
            PatientSetResult.builder().id(1234L).setSize(1L).build());
        doReturn(patientSetResponse)
            .when(patientSetClient).createPatientSet(
                "Biomarker subject set",
                true,
                PatientSetConstraint.builder().subjectIds(new HashSet<>(Arrays.asList("SUBJ1"))).build()
            );
        // return counts for concept constraint on /v2/observations/counts
        ResponseEntity<Counts> patientSetCountsResponse = ResponseEntity.ok(
            Counts.builder().patientCount(15).build());
        doReturn(patientSetCountsResponse)
            .when(aggregateClient).counts(new ConstraintParameter(
                PatientSetConstraint.builder().patientSetId(1234L).build()
        ));
        // return counts for concept constraint on /v2/observations/counts_per_concept
        Map<String, Counts> countsMap = new HashMap<>();
        countsMap.put("ABC", Counts.builder().patientCount(7).build());
        countsMap.put("XYZ", Counts.builder().patientCount(5).build());
        ResponseEntity<Map<String, Counts>> patientSetCountsPerConceptResponse = ResponseEntity.ok(countsMap);
        doReturn(patientSetCountsPerConceptResponse)
            .when(aggregateClient).countsPerConcept(new ConstraintParameter(
            PatientSetConstraint.builder().patientSetId(1234L).build()
        ));
        // return counts for concept constraint on /v2/observations/counts_per_study
        ResponseEntity<Map<String, Counts>> patientSetCountsPerStudyResponse = ResponseEntity.ok(countsMap);
        doReturn(patientSetCountsPerStudyResponse)
            .when(aggregateClient).countsPerStudy(new ConstraintParameter(
            PatientSetConstraint.builder().patientSetId(1234L).build()
        ));
        // return counts for concept constraint on /v2/observations/counts_per_study_and_concept
        Map<String, Map<String, Counts>> countsMapMap = new HashMap<>();
        countsMapMap.put("STUDY", countsMap);
        ResponseEntity<Map<String, Map<String, Counts>>> patientSetCountsPerStudyAndConceptResponse =
            ResponseEntity.ok(countsMapMap);
        doReturn(patientSetCountsPerStudyAndConceptResponse)
            .when(aggregateClient).countsPerStudyAndConcept(new ConstraintParameter(
            PatientSetConstraint.builder().patientSetId(1234L).build()
        ));
        // return counts for concept constraint on /v2/observations/numerical_aggregates_per_concept
        Map<String, NumericalValueAggregates> numericalAggregates = new HashMap<>();
        numericalAggregates.put("Num 1", NumericalValueAggregates.builder().avg(12.3).build());
        ResponseEntity<Map<String, NumericalValueAggregates>> patientSetNumericalAggregatesResponse =
            ResponseEntity.ok(numericalAggregates);
        doReturn(patientSetNumericalAggregatesResponse)
            .when(aggregateClient).numericalValueAggregatesPerConcept(new ConstraintParameter(
            PatientSetConstraint.builder().patientSetId(1234L).build()
        ));
        // return counts for concept constraint on /v2/observations/categorical_aggregates_per_concept
        Map<String, CategoricalValueAggregates> categoricalAggregates = new HashMap<>();
        Map<String, Integer> valueCounts = new HashMap<>();
        valueCounts.put("A", 123);
        valueCounts.put("B", 456);
        categoricalAggregates.put("Cat A", CategoricalValueAggregates.builder().valueCounts(valueCounts).build());
        ResponseEntity<Map<String, CategoricalValueAggregates>> patientSetCategoricalAggregatesResponse =
            ResponseEntity.ok(categoricalAggregates);
        doReturn(patientSetCategoricalAggregatesResponse)
            .when(aggregateClient).categoricalValueAggregatesPerConcept(new ConstraintParameter(
            PatientSetConstraint.builder().patientSetId(1234L).build()
        ));
    }

    @WithMockUser(username="spring")
    @Test
    public void givenSimpleConstraint_whenGetCounts_thenStatus200() throws Exception {
        setupMockData();
        mvc.perform(post("/v2/observations/counts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"constraint\":{\"type\":\"concept\",\"conceptCode\":\"Dummy\"}}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.patientCount", is(15)))
            .andExpect(jsonPath("$.observationCount", is(-1)));
    }

    @WithMockUser(username="spring")
    @Test
    public void givenBiomarkerConstraint_whenGetCounts_thenStatus200() throws Exception {
        setupMockData();
        mvc.perform(post("/v2/observations/counts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"constraint\":{\"type\":\"biomarker\",\"biomarkerType\":\"variant\",\"params\":{\"chromosome\":\"12\"}}}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.patientCount", is(15)))
            .andExpect(jsonPath("$.observationCount", is(-1)));
    }

    @WithMockUser(username="spring")
    @Test
    public void givenBiomarkerConstraint_whenGetCountsPerConcept_thenStatus200() throws Exception {
        setupMockData();
        mvc.perform(post("/v2/observations/counts_per_concept")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"constraint\":{\"type\":\"biomarker\",\"biomarkerType\":\"variant\",\"params\":{\"chromosome\":\"12\"}}}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.ABC.patientCount", is(7)))
            .andExpect(jsonPath("$.ABC.observationCount", is(-1)));
    }

    @WithMockUser(username="spring")
    @Test
    public void givenBiomarkerConstraint_whenGetCountsPerStudy_thenStatus200() throws Exception {
        setupMockData();
        mvc.perform(post("/v2/observations/counts_per_study")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"constraint\":{\"type\":\"biomarker\",\"biomarkerType\":\"variant\",\"params\":{\"chromosome\":\"12\"}}}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.XYZ.patientCount", is(5)))
            .andExpect(jsonPath("$.XYZ.observationCount", is(-1)));
    }

    @WithMockUser(username="spring")
    @Test
    public void givenBiomarkerConstraint_whenGetCountsPerStudyAndConcept_thenStatus200() throws Exception {
        setupMockData();
        mvc.perform(post("/v2/observations/counts_per_study_and_concept")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"constraint\":{\"type\":\"biomarker\",\"biomarkerType\":\"variant\",\"params\":{\"chromosome\":\"12\"}}}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.STUDY.ABC.patientCount", is(7)));
    }

    @WithMockUser(username="spring")
    @Test
    public void givenBiomarkerConstraint_whenGetNumericalAggregatesPerConcept_thenStatus200() throws Exception {
        setupMockData();
        mvc.perform(post("/v2/observations/aggregates_per_numerical_concept")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"constraint\":{\"type\":\"biomarker\",\"biomarkerType\":\"variant\",\"params\":{\"chromosome\":\"12\"}}}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.['Num 1'].avg", is(12.3)));
    }

    @WithMockUser(username="spring")
    @Test
    public void givenBiomarkerConstraint_whenGetCategoricalAggregatesPerConcept_thenStatus200() throws Exception {
        setupMockData();
        mvc.perform(post("/v2/observations/aggregates_per_categorical_concept")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"constraint\":{\"type\":\"biomarker\",\"biomarkerType\":\"variant\",\"params\":{\"chromosome\":\"12\"}}}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.['Cat A'].valueCounts.A", is(123)));
    }

}
