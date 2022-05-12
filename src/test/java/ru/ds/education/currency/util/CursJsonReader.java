package ru.ds.education.currency.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import ru.ds.education.currency.core.dto.CursDataJMSResponse;
import ru.ds.education.currency.model.Curs;
import ru.ds.education.currency.spec.DateSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class CursJsonReader {


    private CursJsonReader() {
    }

    private static String readFileFromResource(String s) {
        Resource resource = new ClassPathResource(s, Thread.currentThread().getContextClassLoader());
        try {
            return new String(
                    Files.readAllBytes(resource.getFile().toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Curs> readTestDataCurs() throws JsonProcessingException {
        String fileData = readFileFromResource("cursEntities.json");
        return Arrays.asList(getJsonMapperForCursData().readValue(fileData, Curs[].class));
    }

    public static CursDataJMSResponse readTestDataCursJMS() throws JsonProcessingException {
        String fileData = readFileFromResource("cursDataJMSResponseEntitiesCbr.json");
        return getJsonMapperForCursJMSResponse().readValue(fileData, CursDataJMSResponse.class);

    }

    public static ObjectMapper getJsonMapperForCursData() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT)));
        mapper.registerModule(javaTimeModule);
        mapper.findAndRegisterModules();
        return mapper;
    }

    public static ObjectMapper getJsonMapperForCursJMSResponse() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT_JMS_CBR)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT_JMS_CBR)));
        mapper.registerModule(javaTimeModule);
        mapper.findAndRegisterModules();
        return mapper;
    }
}
