package nl.thehyve.transmartvariantstoreconnector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Case {
    @JsonProperty("id")
    private String identifier;
    @JsonProperty("Project_id")
    private String projectId;
}
