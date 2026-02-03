package org.example.support.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TicketApiImportCsvBranchTests {

    @Autowired
    MockMvc mockMvc;

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
}
