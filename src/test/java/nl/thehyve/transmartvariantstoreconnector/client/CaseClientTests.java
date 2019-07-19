package nl.thehyve.transmartvariantstoreconnector.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import nl.thehyve.transmartvariantstoreconnector.TransmartVariantStoreConnectorApplication;
import nl.thehyve.transmartvariantstoreconnector.dto.Case;
import nl.thehyve.transmartvariantstoreconnector.dto.ListingArguments;
import nl.thehyve.transmartvariantstoreconnector.service.CaseClientService;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK, classes = TransmartVariantStoreConnectorApplication.class,
    properties = {"variant-store-client.variant-store-url=http://localhost:9070"})
public class CaseClientTests {

    private @Autowired CaseClientService caseClientService;
    private @Autowired ObjectMapper mapper;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(9070));

    @Test
    public void whenCallingGetCases_thenClientMakesCorrectCall() throws JsonProcessingException {
        // Prepare mock response
        List<Case> mockCases = Arrays.asList(
            Case.builder().identifier("SUBJ1").projectId("EHR").build(),
            Case.builder().identifier("SUBJ2").projectId("EHR").build()
        );
        stubFor(get(urlMatching("/cases\\?[a-zA-Z0-9=&]+"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                .withBody(mapper.writeValueAsString(mockCases))));

        // Use case client
        ListingArguments arguments = ListingArguments.builder()
            .chromosome("8")
            .startPosition(new BigInteger("10000"))
            .endPosition(new BigInteger("12000"))
            .build();
        List<Case> cases = this.caseClientService.fetchCases(arguments);

        // Check call to mock server
        verify(1, getRequestedFor(urlMatching("/cases\\?[a-zA-Z0-9=&]+")));
        // Check response
        Assert.assertNotNull(cases);
        assertThat(cases).isNotEmpty();
        Assert.assertEquals("SUBJ1", cases.get(0).getIdentifier());
    }

}
