package nl.thehyve.transmartvariantstoreconnector.service;

import nl.thehyve.transmartvariantstoreconnector.TransmartVariantStoreConnectorApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.transmartproject.common.client.AggregateClient;
import org.transmartproject.common.constraint.ConceptConstraint;
import org.transmartproject.common.constraint.Constraint;
import org.transmartproject.common.dto.ConstraintParameter;
import org.transmartproject.common.dto.Counts;

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
    private AggregateClient aggregateClient;

    @Autowired
    private MockMvc mvc;

    private void setupMockData() {
        Constraint conceptConstraint = ConceptConstraint.builder().conceptCode("Dummy").build();
        // return counts for /v2/observations/counts
        ResponseEntity<Counts> countsResponse = ResponseEntity.ok(
            Counts.builder().patientCount(15).build());
        doReturn(countsResponse)
            .when(aggregateClient).counts(new ConstraintParameter(conceptConstraint));
    }

    @WithMockUser(username="spring")
    @Test
    public void givenAvailableClient_whenGetCounts_thenStatus200() throws Exception {
        setupMockData();
        mvc.perform(post("/v2/observations/counts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"constraint\":{\"type\":\"concept\",\"conceptCode\":\"Dummy\"}}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.patientCount", is(15)))
            .andExpect(jsonPath("$.observationCount", is(-1)));
    }

}
