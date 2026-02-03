package org.example.support.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TicketApiImportErrorTests {

    @Autowired
    MockMvc mockMvc;

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
