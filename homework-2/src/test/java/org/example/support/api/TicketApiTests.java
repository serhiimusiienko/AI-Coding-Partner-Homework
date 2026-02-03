package org.example.support.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TicketApiTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createAndGetTicket() throws Exception {
        String body = "{" +
                "\"customer_id\":\"CUST-1\"," +
                "\"customer_email\":\"user@example.com\"," +
                "\"customer_name\":\"User\"," +
                "\"subject\":\"Login issue\"," +
                "\"description\":\"Cannot access account\"}";

        String location = mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getContentAsString();

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
                String body = "{" +
                                "\"customer_id\":\"CUST-2\"," +
                                "\"customer_email\":\"user2@example.com\"," +
                                "\"customer_name\":\"User Two\"," +
                                "\"subject\":\"Production down\"," +
                                "\"description\":\"App crash causes outage\"}";

                mockMvc.perform(post("/tickets?autoClassify=true")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(body))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.priority", is("URGENT")))
                                .andExpect(jsonPath("$.category", anyOf(is("TECHNICAL_ISSUE"), is("BUG_REPORT"))));
        }
}
