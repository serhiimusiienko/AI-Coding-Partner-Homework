package org.example.support.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

                createTicket(body);

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
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
}
