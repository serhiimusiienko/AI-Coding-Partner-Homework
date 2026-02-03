package org.example.support.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.empty;

@SpringBootTest
@AutoConfigureMockMvc
class TicketApiDateRangeFalseBranchTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void farFutureFromExcludesCurrentTickets() throws Exception {
        mockMvc.perform(get("/tickets?from=2100-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }
}
