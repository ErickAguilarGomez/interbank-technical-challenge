package com.interbank.batch.reader;

import com.interbank.common.dto.TransactionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlTransactionReaderTest {

    @Test
    @DisplayName("Debe parsear correctamente un archivo XML válido similar a myXMLFile0.xml")
    void testParseValidXml() throws Exception {
        String xml = """
                <root>
                  <person
                  firstname="Nonnah"
                  lastname="Waite"
                  city="Minsk"
                  country="France"
                  firstname2="Myriam"
                  lastname2="Kronfeld"
                  email="Myriam.Kronfeld@yopmail.com"
                  />
                  <random>22</random>
                  <random_float>17.059</random_float>
                  <bool>true</bool>
                  <date>1998-12-11</date>
                  <regEx>hello to you</regEx>
                  <enum>generator</enum>
                  <elt>Jaime</elt><elt>Kimberley</elt>
                  <Myriam>
                    <age>56</age>
                  </Myriam>
                </root>
                """;

        XmlTransactionReader reader = new XmlTransactionReader();
        reader.setResource(new ByteArrayResource(xml.getBytes(StandardCharsets.UTF_8), "myXMLFile0.xml"));

        TransactionDto dto = reader.read();
        assertNotNull(dto);
        assertEquals("Nonnah", dto.getFirstname());
        assertEquals("Waite", dto.getLastname());
        assertEquals("France", dto.getCountry());
        assertEquals("Myriam.Kronfeld@yopmail.com", dto.getEmail());
        assertEquals(22, dto.getRandomInt());
        assertEquals(17.059, dto.getRandomFloat());
        assertTrue(dto.getBoolFlag());
        assertEquals(LocalDate.of(1998, 12, 11), dto.getOperationDate());
        assertEquals("generator", dto.getChannelEnum());
        assertEquals(2, dto.getElements().size());
        assertEquals(56, dto.getAge());

        assertNull(reader.read());
    }

    @Test
    @DisplayName("Debe sanitizar y parsear exitosamente XMLs con comillas no escapadas como Cote D\"Ivoire")
    void testParseXmlWithUnescapedQuotes() throws Exception {
        String xml = """
                <root>
                  <person
                  firstname="Alisha"
                  lastname="Brenn"
                  city="Conakry"
                  country="Cote D"Ivoire"
                  firstname2="Fidelia"
                  lastname2="Ledah"
                  email="Fidelia.Ledah@yopmail.com"
                  />
                  <random>84</random>
                  <random_float>3.211</random_float>
                  <bool>true</bool>
                  <date>1982-01-15</date>
                  <regEx>hello</regEx>
                  <enum>online</enum>
                  <Fidelia>
                    <age>23</age>
                  </Fidelia>
                </root>
                """;

        XmlTransactionReader reader = new XmlTransactionReader();
        reader.setResource(new ByteArrayResource(xml.getBytes(StandardCharsets.UTF_8), "myXMLFile1027.xml"));

        TransactionDto dto = reader.read();
        assertNotNull(dto);
        assertEquals("Alisha", dto.getFirstname());
        assertEquals("Cote D'Ivoire", dto.getCountry());
        assertEquals(23, dto.getAge());
    }
}

