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
import org.springframework.security.core.parameters.P;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.transmartproject.common.client.ObservationClient;
import org.transmartproject.common.client.PatientSetClient;
import org.transmartproject.common.constraint.AndConstraint;
import org.transmartproject.common.constraint.Constraint;
import org.transmartproject.common.constraint.PatientSetConstraint;
import org.transmartproject.common.constraint.StudyNameConstraint;
import org.transmartproject.common.dto.*;
import org.transmartproject.common.type.Sex;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK, classes = TransmartVariantStoreConnectorApplication.class)
@AutoConfigureMockMvc
public class FilteringObservationClientServiceTests {

    @MockBean
    private CaseClient caseClient;

    @MockBean
    private PatientSetClient patientSetClient;

    @MockBean
    private ObservationClient observationClient;

    @Autowired
    private MockMvc mvc;

    private void setupMockData() {
        Constraint studyConstraint = StudyNameConstraint.builder().studyId("Study Z").build();
        // return observations for study constraint on /v2/observations
        Map<String, List<Object>> dimensionElements = new HashMap<>();
        dimensionElements.put("study", Arrays.asList("Study Z"));
        dimensionElements.put("patient", Arrays.asList(
            PatientDimensionElement.builder().id(5L).sex(Sex.Female).subjectIds(singletonMap("SUBJ_ID", "Subj 5")).build(),
            PatientDimensionElement.builder().id(8L).sex(Sex.Male).subjectIds(singletonMap("SUBJ_ID", "Subj 8")).build()));
        dimensionElements.put("concept", Arrays.asList(
            ConceptDimensionElement.builder().conceptCode("age").name("Age").conceptPath("\\Age").build(),
            ConceptDimensionElement.builder().conceptCode("gender").name("Gender").conceptPath("\\Gender").build()
        ));
        ResponseEntity<Hypercube> hypercubeResponse = ResponseEntity.ok(
            Hypercube.builder()
                .dimensionDeclarations(Arrays.asList(
                    DimensionDeclaration.builder().name("study").build(),
                    DimensionDeclaration.builder().name("patient").build(),
                    DimensionDeclaration.builder().name("concept").build()
                ))
                .cells(Arrays.asList(
                    Cell.builder().dimensionIndexes(Arrays.asList(0, 0, 0)).numericValue(new BigDecimal(71)).build(),
                    Cell.builder().dimensionIndexes(Arrays.asList(0, 0, 1)).stringValue("female").build(),
                    Cell.builder().dimensionIndexes(Arrays.asList(0, 1, 0)).numericValue(new BigDecimal(56)).build(),
                    Cell.builder().dimensionIndexes(Arrays.asList(0, 1, 1)).stringValue("male").build()
                ))
                .dimensionElements(dimensionElements)
                .build());
        doReturn(hypercubeResponse)
            .when(observationClient).query(Query.builder().constraint(studyConstraint).type("clinical").build());
        // return cases for /cases
        List<Case> casesResponse = Arrays.asList(
            Case.builder().identifier("Subj 5").build());
        doReturn(casesResponse)
            .when(caseClient).getCasesForVariant(ListingArguments.builder().chromosome("11").build());
        // return patient set for /v2/patient_sets
        ResponseEntity<PatientSetResult> patientSetResponse = ResponseEntity.status(HttpStatus.CREATED).body(
            PatientSetResult.builder().id(1234L).setSize(1L).build());
        doReturn(patientSetResponse)
            .when(patientSetClient).createPatientSet(
                "Biomarker subject set",
                true,
                PatientSetConstraint.builder().subjectIds(new HashSet<>(Arrays.asList("Subj 5"))).build()
            );
        // return hypercube for combination of biomarker and study constraint
        Map<String, List<Object>> restrictedDimensionElements = new HashMap<>();
        restrictedDimensionElements.put("study", dimensionElements.get("study"));
        restrictedDimensionElements.put("patient", Arrays.asList(
            PatientDimensionElement.builder().id(5L).sex(Sex.Female).subjectIds(singletonMap("SUBJ_ID", "Subj 5")).build()));
        restrictedDimensionElements.put("concept", dimensionElements.get("concept"));
        ResponseEntity<Hypercube> restrictedHypercubeResponse = ResponseEntity.ok(
            Hypercube.builder()
                .dimensionDeclarations(Arrays.asList(
                    DimensionDeclaration.builder().name("study").build(),
                    DimensionDeclaration.builder().name("patient").build(),
                    DimensionDeclaration.builder().name("concept").build()
                ))
                .cells(Arrays.asList(
                    Cell.builder().dimensionIndexes(Arrays.asList(0, 0, 0)).numericValue(new BigDecimal(71)).build(),
                    Cell.builder().dimensionIndexes(Arrays.asList(0, 0, 1)).stringValue("female").build()
                ))
                .dimensionElements(restrictedDimensionElements)
                .build());
        doReturn(restrictedHypercubeResponse)
            .when(observationClient).query(Query.builder()
                .constraint(AndConstraint.builder().args(Arrays.asList(studyConstraint, PatientSetConstraint.builder().patientSetId(1234L).build())).build())
                .type("clinical").build());
    }

    @WithMockUser(username="spring")
    @Test
    public void givenSimpleConstraint_whenQueryObservations_thenStatus200() throws Exception {
        setupMockData();
        mvc.perform(post("/v2/observations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"type\":\"clinical\",\"constraint\":{\"type\":\"study_name\",\"studyId\":\"Study Z\"}}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.dimensionDeclarations[0].name", is("study")))
            .andExpect(jsonPath("$.cells", hasSize(4)))
            .andExpect(jsonPath("$.cells[3].stringValue", is("male")));
    }

    @WithMockUser(username="spring")
    @Test
    public void givenBiomarkerConstraint_whenQueryObservations_thenStatus200() throws Exception {
        setupMockData();
        mvc.perform(post("/v2/observations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"type\":\"clinical\",\"constraint\":{\"type\":\"and\",\"args\":[" +
                "{\"type\":\"study_name\",\"studyId\":\"Study Z\"}," +
                "{\"type\":\"biomarker\",\"biomarkerType\":\"variant\",\"params\":{\"chromosome\":\"11\"}}" +
                "]}}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.dimensionDeclarations[0].name", is("study")))
            .andExpect(jsonPath("$.cells", hasSize(2)))
            .andExpect(jsonPath("$.cells[1].stringValue", is("female")));
    }

}
