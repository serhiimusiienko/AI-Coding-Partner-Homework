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
class TicketApiImportInferenceTests {

    @Autowired
    MockMvc mockMvc;

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
}
