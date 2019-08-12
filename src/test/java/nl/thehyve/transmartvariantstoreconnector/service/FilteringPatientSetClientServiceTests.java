package nl.thehyve.transmartvariantstoreconnector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.transmartproject.common.client.PatientSetClient;
import org.transmartproject.common.constraint.*;
import org.transmartproject.common.dto.*;
import org.transmartproject.common.type.DataType;
import org.transmartproject.common.type.Operator;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK, classes = TransmartVariantStoreConnectorApplication.class)
@AutoConfigureMockMvc
public class FilteringPatientSetClientServiceTests {

    private @MockBean CaseClient caseClient;

    private @MockBean PatientSetClient patientSetClient;

    private @Autowired MockMvc mvc;

    private @Autowired ObjectMapper objectMapper;

    private void setupMockData() throws JsonProcessingException {
        Constraint heartRateConstraint = AndConstraint.builder().args(Arrays.asList(
            ConceptConstraint.builder().conceptCode("Heart rate").build(),
            ValueConstraint.builder().valueType(DataType.Numeric).operator(Operator.Greater_than).value(140).build()
        )).build();
        // return patient set for hearth rate constraint on /v2/observations
        ResponseEntity<PatientSetResult> heartRatePatientSetResponse = ResponseEntity.status(HttpStatus.CREATED).body(
            PatientSetResult.builder()
                .id(3456L)
                .setSize(50L)
                .requestConstraints(objectMapper.writeValueAsString(heartRateConstraint))
                .name("Heart rate above 140")
                .build());
        doReturn(heartRatePatientSetResponse)
            .when(patientSetClient).createPatientSet("Heart rate above 140", true, heartRateConstraint);
        // return cases for /cases
        List<Case> casesResponse = Arrays.asList(
            Case.builder().identifier("Subj 5").build());
        doReturn(casesResponse)
            .when(caseClient).getCasesForVariant(ListingArguments.builder().chromosome("X").build());
        // return patient set for /v2/patient_sets
        ResponseEntity<PatientSetResult> patientSetResponse = ResponseEntity.status(HttpStatus.CREATED).body(
            PatientSetResult.builder().id(1234L).setSize(1L).build());
        doReturn(patientSetResponse)
            .when(patientSetClient).createPatientSet(
                "Biomarker subject set",
                true,
                PatientSetConstraint.builder().subjectIds(new HashSet<>(Arrays.asList("Subj 5"))).build()
            );
        // return patient set for combination of biomarker and heart rate constraint
        Constraint combinationConstraint = AndConstraint.builder().args(Arrays.asList(
            heartRateConstraint,
            PatientSetConstraint.builder().patientSetId(1234L).build()
        )).build();
        ResponseEntity<PatientSetResult> combinationPatientSetResponse = ResponseEntity.status(HttpStatus.CREATED).body(
            PatientSetResult.builder()
                .id(7890L)
                .setSize(25L)
                .requestConstraints(objectMapper.writeValueAsString(heartRateConstraint))
                .name("Heart rate + chrom X")
                .build());
        doReturn(combinationPatientSetResponse)
            .when(patientSetClient).createPatientSet("Heart rate + chrom X", true, combinationConstraint);
    }

    @WithMockUser(username="spring")
    @Test
    public void givenSimpleConstraint_whenCreatePatientSet_thenStatus201() throws Exception {
        setupMockData();
        mvc.perform(post("/v2/patient_sets?name=Heart rate above 140&reuse=true")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"type\":\"and\",\"args\":[" +
                "{\"type\":\"concept\",\"conceptCode\":\"Heart rate\"}," +
                "{\"type\":\"value\",\"valueType\":\"NUMERIC\",\"operator\":\">\",\"value\":140}" +
                "]}"))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name", is("Heart rate above 140")))
            .andExpect(jsonPath("$.setSize", is(50)));
    }

    @WithMockUser(username="spring")
    @Test
    public void givenBiomarkerConstraint_whenCreatePatientSet_thenStatus201() throws Exception {
        setupMockData();
        mvc.perform(post("/v2/patient_sets?name=Heart rate + chrom X&reuse=true")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"type\":\"and\",\"args\":[" +
                "{\"type\":\"and\",\"args\":[" +
                    "{\"type\":\"concept\",\"conceptCode\":\"Heart rate\"}," +
                    "{\"type\":\"value\",\"valueType\":\"NUMERIC\",\"operator\":\">\",\"value\":140}" +
                "]}," +
                "{\"type\":\"biomarker\",\"biomarkerType\":\"variant\",\"params\":{\"chromosome\":\"X\"}}" +
                "]}"))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name", is("Heart rate + chrom X")))
            .andExpect(jsonPath("$.setSize", is(25)));
    }

}
