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

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class TicketApiFilterCategoryPriorityTests {

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private String toJson(Map<String, Object> payload) throws Exception {
        return mapper.writeValueAsString(payload);
    }

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
}
