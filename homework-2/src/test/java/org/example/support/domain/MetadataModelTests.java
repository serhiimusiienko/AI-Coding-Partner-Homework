package org.example.support.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetadataModelTests {

    @Test
    void gettersAndSettersCovered() {
        Metadata m = new Metadata();
        m.setBrowser("Safari");
        m.setSource(Metadata.Source.EMAIL);
        m.setDeviceType(Metadata.DeviceType.MOBILE);

        assertEquals("Safari", m.getBrowser());
        assertEquals(Metadata.Source.EMAIL, m.getSource());
        assertEquals(Metadata.DeviceType.MOBILE, m.getDeviceType());
    }
}
