package fu.se.chapter15api;

import com.fasterxml.jackson.databind.ObjectMapper;
import fu.se.chapter15api.repositories.IOrchidRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrchidApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired IOrchidRepository repository;

    @BeforeEach
    void clearTestDatabase() { repository.deleteAll(); }

    private String payload(String name, boolean natural, boolean attractive) throws Exception {
        return mapper.writeValueAsString(Map.of(
                "orchidName", name, "natural", natural, "attractive", attractive,
                "orchidDescription", "Lan hồ điệp - mô tả Unicode",
                "orchidCategory", "Cattleya", "orchidURL", "https://example.com/orchid.jpg"));
    }

    @Test
    void crudPersistsEveryFieldAndPreservesGeneratedId() throws Exception {
        String response = mvc.perform(post("/orchids/").contentType(MediaType.APPLICATION_JSON)
                        .content(payload("Taichung Beauty", true, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.natural").value(true))
                .andExpect(jsonPath("$.attractive").value(true))
                .andReturn().getResponse().getContentAsString();
        int id = mapper.readTree(response).get("orchidId").asInt();
        assertThat(id).isPositive();
        assertThat(repository.findById(id).orElseThrow().getOrchidName()).isEqualTo("Taichung Beauty");

        mvc.perform(get("/orchids/"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/orchids/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orchidId").value(id))
                .andExpect(jsonPath("$.orchidDescription").value("Lan hồ điệp - mô tả Unicode"))
                .andExpect(jsonPath("$.orchidCategory").value("Cattleya"))
                .andExpect(jsonPath("$.orchidURL").value("https://example.com/orchid.jpg"));

        var updated = mapper.readTree(payload("Updated orchid", false, false));
        var updateObject = (com.fasterxml.jackson.databind.node.ObjectNode) updated;
        updateObject.put("orchidDescription", "Updated description");
        updateObject.put("orchidCategory", "Dendrobium");
        updateObject.put("orchidURL", "https://example.com/updated.jpg");
        mvc.perform(put("/orchids/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(updated.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orchidId").value(id))
                .andExpect(jsonPath("$.orchidName").value("Updated orchid"))
                .andExpect(jsonPath("$.natural").value(false))
                .andExpect(jsonPath("$.attractive").value(false));
        var saved = repository.findById(id).orElseThrow();
        assertThat(saved.getOrchidName()).isEqualTo("Updated orchid");
        assertThat(saved.isNatural()).isFalse();
        assertThat(saved.isAttractive()).isFalse();
        assertThat(saved.getOrchidDescription()).isEqualTo("Updated description");
        assertThat(saved.getOrchidCategory()).isEqualTo("Dendrobium");
        assertThat(saved.getOrchidURL()).isEqualTo("https://example.com/updated.jpg");

        mvc.perform(delete("/orchids/{id}", id)).andExpect(status().isNoContent());
        assertThat(repository.findById(id)).isEmpty();
        mvc.perform(get("/orchids/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void collectionAlsoSupportsReadmeUrlsWithoutTrailingSlash() throws Exception {
        mvc.perform(get("/orchids")).andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(post("/orchids").contentType(MediaType.APPLICATION_JSON)
                .content(payload("Readme URL", false, true))).andExpect(status().isCreated());
    }

    @Test
    void missingResourceReturns404ForReadUpdateAndDelete() throws Exception {
        mvc.perform(get("/orchids/999999")).andExpect(status().isNotFound());
        mvc.perform(put("/orchids/999999").contentType(MediaType.APPLICATION_JSON)
                .content(payload("Missing", true, true))).andExpect(status().isNotFound());
        mvc.perform(delete("/orchids/999999")).andExpect(status().isNotFound());
        assertThat(repository.count()).isZero();
    }

    @Test
    void suppliedCreateIdDoesNotOverwriteExistingRecord() throws Exception {
        String body = payload("Original", true, true);
        var response = mvc.perform(post("/orchids/").contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
        int originalId = mapper.readTree(response).get("orchidId").asInt();
        var input = mapper.readTree(payload("New record", false, false));
        ((com.fasterxml.jackson.databind.node.ObjectNode) input).put("orchidId", originalId);
        mvc.perform(post("/orchids/").contentType(MediaType.APPLICATION_JSON).content(input.toString()))
                .andExpect(status().isCreated());
        assertThat(repository.count()).isEqualTo(2);
        assertThat(repository.findById(originalId).orElseThrow().getOrchidName()).isEqualTo("Original");
    }

    @Test
    void invalidNameAndOversizedFieldsAreRejectedWithoutInsert() throws Exception {
        mvc.perform(post("/orchids/").contentType(MediaType.APPLICATION_JSON)
                .content(payload("   ", true, true))).andExpect(status().isBadRequest());
        mvc.perform(post("/orchids/").contentType(MediaType.APPLICATION_JSON)
                .content(payload("x".repeat(151), true, true))).andExpect(status().isBadRequest());
        assertThat(repository.count()).isZero();
    }

    @Test
    void unsupportedContentTypeReturns415() throws Exception {
        mvc.perform(post("/orchids/").contentType(MediaType.TEXT_PLAIN)
                .content(payload("Wrong header", true, true))).andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void corsAllowsViteAndRejectsOtherOrigins() throws Exception {
        mvc.perform(options("/orchids/").header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/orchids/").header("Origin", "http://localhost:9999")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
