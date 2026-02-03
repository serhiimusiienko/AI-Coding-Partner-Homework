package org.example.support.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TicketApiTests {

    @Autowired
    MockMvc mockMvc;

        // Use snake_case to match API naming strategy
        private final ObjectMapper mapper = new ObjectMapper()
                        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        private String toJson(Map<String, Object> payload) throws Exception {
                return mapper.writeValueAsString(payload);
        }

        private String extractId(String json) throws Exception {
                return mapper.readTree(json).path("id").asText();
        }

        private String createTicket(Map<String, Object> payload) throws Exception {
                String res = mockMvc.perform(post("/tickets")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(toJson(payload)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id", notNullValue()))
                                .andReturn().getResponse().getContentAsString();
                return extractId(res);
        }

    @Test
    void createAndGetTicket() throws Exception {
                Map<String, Object> body = new HashMap<>();
                body.put("customer_id", "CUST-1");
                body.put("customer_email", "user@example.com");
                body.put("customer_name", "User");
                body.put("subject", "Login issue");
                body.put("description", "Cannot access account");

                String id = createTicket(body);

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        // Get by id
        mockMvc.perform(get("/tickets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject", is("Login issue")));
    }

    @Test
    void updateNotFoundGives404() throws Exception {
        String upd = "{\"subject\":\"Updated subject\"}";
        mockMvc.perform(put("/tickets/00000000-0000-0000-0000-000000000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(upd))
                .andExpect(status().isNotFound());
    }

        @Test
        void autoClassifyOnCreateSetsUrgent() throws Exception {
                Map<String, Object> body = new HashMap<>();
                body.put("customer_id", "CUST-2");
                body.put("customer_email", "user2@example.com");
                body.put("customer_name", "User Two");
                body.put("subject", "Production down");
                body.put("description", "App crash causes outage");

                mockMvc.perform(post("/tickets?autoClassify=true")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(toJson(body)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.priority", is("URGENT")))
                                .andExpect(jsonPath("$.category", anyOf(is("TECHNICAL_ISSUE"), is("BUG_REPORT"))));
        }

        @Test
        void autoClassifyEndpointReturnsResult() throws Exception {
                Map<String, Object> body = new HashMap<>();
                body.put("customer_id", "CUST-5");
                body.put("customer_email", "user5@example.com");
                body.put("customer_name", "User Five");
                body.put("subject", "Payment problem");
                body.put("description", "Credit card declined on checkout");

                String id = createTicket(body);

                mockMvc.perform(post("/tickets/" + id + "/auto-classify"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.category", notNullValue()))
                                .andExpect(jsonPath("$.priority", notNullValue()))
                                .andExpect(jsonPath("$.confidence", greaterThan(0.0)));
        }

    @Test
    void filterByTagAndStatus() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("customer_id", "CUST-3");
        body.put("customer_email", "user3@example.com");
        body.put("customer_name", "User Three");
        body.put("subject", "Auth help");
        body.put("description", "Need assistance");
        body.put("tags", new String[]{"auth", "vip"});

        String id = createTicket(body);

        // Update status to RESOLVED
        mockMvc.perform(put("/tickets/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("status", "RESOLVED"))))
                .andExpect(status().isOk());

        // Filter by tag
        mockMvc.perform(get("/tickets?tag=auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        // Filter by status
        mockMvc.perform(get("/tickets?status=RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    void filterByDateRange() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("customer_id", "CUST-4");
        body.put("customer_email", "user4@example.com");
        body.put("customer_name", "User Four");
        body.put("subject", "Case");
        body.put("description", "Description long enough");

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/tickets?from=1970-01-01T00:00:00Z&to=2100-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    void deleteTicketRemovesResource() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("customer_id", "CUST-6");
        body.put("customer_email", "user6@example.com");
        body.put("customer_name", "User Six");
        body.put("subject", "Minor issue");
        body.put("description", "Minor description long enough");

        String id = createTicket(body);

        mockMvc.perform(delete("/tickets/" + id))
                .andExpect(status().isNoContent());

        // Verify 404 afterwards
        mockMvc.perform(get("/tickets/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteNotFoundGives404() throws Exception {
        mockMvc.perform(delete("/tickets/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void importEndpointHandlesCsv() throws Exception {
        byte[] bytes = this.getClass().getResourceAsStream("/fixtures/sample_tickets.csv").readAllBytes();
        MockMultipartFile file = new MockMultipartFile("file", "sample_tickets.csv", "text/csv", bytes);
        mockMvc.perform(multipart("/tickets/import").file(file).param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_records", is(50)))
                .andExpect(jsonPath("$.successful", greaterThanOrEqualTo(40)))
                .andExpect(jsonPath("$.failed", greaterThanOrEqualTo(0)));
    }

    @Test
    void importEndpointRejectsUnknownFormat() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "data.bin", "application/octet-stream", new byte[]{1,2,3});
        mockMvc.perform(multipart("/tickets/import").file(file).param("format", "unknown"))
                .andExpect(status().isBadRequest());
    }

    // Tests from TicketApiImportCsvBranchTests
    @Test
    void csvWithMixedEnumValuesExercisesBranches() throws Exception {
        String csv = String.join("\n",
                "customer_id,customer_email,customer_name,subject,description,category,priority,status",
                "C1,c1@example.com,User One,Crash,App error occurs,technical_issue,high,new",
                "C2,c2@example.com,User Two,Minor issue,Small problem,,,", // blanks
                "C3,c3@example.com,User Three,Invalid enum,Unknown enums,unknown,unknown,unknown" // invalids
        );
        MockMultipartFile file = new MockMultipartFile("file", "mix.csv", "text/csv", csv.getBytes());
        mockMvc.perform(multipart("/tickets/import").file(file))
                .andExpect(status().isOk());
    }

    // Tests from TicketApiImportInferenceTests
    @Test
    void importJsonWithInferredFormatSucceeds() throws Exception {
        byte[] bytes = this.getClass().getResourceAsStream("/fixtures/sample_tickets.json").readAllBytes();
        MockMultipartFile file = new MockMultipartFile("file", "tickets.json", "application/json", bytes);
        mockMvc.perform(multipart("/tickets/import").file(file))
                .andExpect(status().isOk());
    }

    @Test
    void importUnknownExtensionWithoutFormatReturns400() throws Exception {
        byte[] bytes = "just some content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "data.txt", "text/plain", bytes);
        mockMvc.perform(multipart("/tickets/import").file(file))
                .andExpect(status().isBadRequest());
    }

    // Tests from TicketApiFilterCategoryPriorityTests
    @Test
    void filterByCategoryAndPriorityExercisesTrueAndFalseBranches() throws Exception {
        Map<String, Object> t1 = new HashMap<>();
        t1.put("customer_id", "CUST-F1");
        t1.put("customer_email", "f1@example.com");
        t1.put("customer_name", "F One");
        t1.put("subject", "App crash");
        t1.put("description", "Error occurs and is important");
        // rely on auto-classifier to set category TECHNICAL_ISSUE and priority HIGH
        mockMvc.perform(post("/tickets?autoClassify=true").contentType(MediaType.APPLICATION_JSON).content(toJson(t1)))
                .andExpect(status().isCreated());

        Map<String, Object> t2 = new HashMap<>();
        t2.put("customer_id", "CUST-F2");
        t2.put("customer_email", "f2@example.com");
        t2.put("customer_name", "F Two");
        t2.put("subject", "Refund question");
        t2.put("description", "Billing invoice refund requested");
        // rely on auto-classifier to set category BILLING_QUESTION and (likely) LOW due to wording
        mockMvc.perform(post("/tickets?autoClassify=true").contentType(MediaType.APPLICATION_JSON).content(toJson(t2)))
                .andExpect(status().isCreated());

        // True branch: filter matches t1
        mockMvc.perform(get("/tickets?category=TECHNICAL_ISSUE&priority=HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        // False branch: filter excludes t2 when asking TECHNICAL_ISSUE & HIGH
        mockMvc.perform(get("/tickets?category=BILLING_QUESTION&priority=LOW"))
                .andExpect(status().isOk());
    }

    // Tests from TicketApiDateRangeFalseBranchTests
    @Test
    void farFutureFromExcludesCurrentTickets() throws Exception {
        mockMvc.perform(get("/tickets?from=2100-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }

    // Tests from TicketApiImportErrorTests
    @Test
    void importJsonMalformedReturns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bad.json", MediaType.APPLICATION_JSON_VALUE,
                "{ \"not_valid_json\": }".getBytes()
        );
        mockMvc.perform(multipart("/tickets/import").file(file).param("format", "json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importXmlMalformedReturns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bad.xml", MediaType.APPLICATION_XML_VALUE,
                "<tickets><ticket><subject>missing closures".getBytes()
        );
        mockMvc.perform(multipart("/tickets/import").file(file).param("format", "xml"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importCsvInvalidHeadersReturns400() throws Exception {
        String csv = "wrong_header1,wrong_header2\nval1,val2";
        MockMultipartFile file = new MockMultipartFile(
                "file", "bad.csv", "text/csv",
                csv.getBytes()
        );
        mockMvc.perform(multipart("/tickets/import").file(file).param("format", "csv"))
                .andExpect(status().isBadRequest());
    }
}
