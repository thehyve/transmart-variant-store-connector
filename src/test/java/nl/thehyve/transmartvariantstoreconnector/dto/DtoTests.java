package nl.thehyve.transmartvariantstoreconnector.dto;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.transmartproject.common.dto.Counts;

public class DtoTests {
    @Test
    public void testCaseEquals() {
        EqualsVerifier
            .forClass(Case.class)
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    public void testListingArgumentsEquals() {
        EqualsVerifier
            .forClass(ListingArguments.class)
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
            .verify();
    }
}
