package ru.ds.education.currency.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.ds.education.currency.BasePostgresSQLContainer;
import ru.ds.education.currency.core.dto.CursDto;
import ru.ds.education.currency.core.dto.mapper.CursMapper;
import ru.ds.education.currency.model.CurrencyType;
import ru.ds.education.currency.model.Curs;
import ru.ds.education.currency.repository.CursRepo;
import ru.ds.education.currency.spec.DateSpec;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
class CursControllerTest extends BasePostgresSQLContainer {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CursRepo cursRepo;

    @Autowired
    private CursMapper cursMapper;

    private ObjectMapper jsonMapper;


    @PostConstruct
    public void init() {
        jsonMapper = configureJsonMapper();
    }


    @BeforeEach
    public void createDbData() throws Exception {
        cursRepo.deleteAll();
        List<CursDto> cursEntities = loadTestData();
        cursRepo.saveAll(cursEntities.stream()
                .map(x -> cursMapper.map(x, Curs.class)).collect(Collectors.toList()));
    }

    @Test
    void get() throws Exception {
        long count = cursRepo.count();
        Curs curs = cursRepo.findAll().get(0);
        CursDto cursDto = cursMapper.map(curs, CursDto.class);
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                getUrl()
                        )
                )
                .andDo(print())
                .andExpect(status().isOk());


        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                getUrl(String.valueOf(cursDto.getId())))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cursWithId("$", cursDto));

        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                getUrl(String.valueOf(++count)))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll() throws Exception {
        List<CursDto> testData = loadTestData();
        long count = cursRepo.count();
        List<CursDto> cursEntities = cursRepo.findAll().stream()
                .map(x -> cursMapper.map(x, CursDto.class)).collect(Collectors.toList());
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(getUrl())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize((int) count)))
                .andExpect(cursWithId("$[0]", cursEntities.get(0)))
                .andExpect(cursWithId("$[1]", cursEntities.get(1)))
                .andExpect(cursWithId("$[2]", cursEntities.get(2)))
                .andExpect(cursWithId("$[3]", cursEntities.get(3)))

                .andExpect(cursWithOutId("$[0]", testData.get(0)))
                .andExpect(cursWithOutId("$[1]", testData.get(1)))
                .andExpect(cursWithOutId("$[2]", testData.get(2)))
                .andExpect(cursWithOutId("$[3]", testData.get(3)));


        LocalDate testDate = LocalDate.of(2022, 5, 14);
        CurrencyType testCurrency = CurrencyType.USD;
        List<CursDto> cursTestDataFilteredDateAndCurrency = testData.stream()
                .filter(x -> x.getDate().equals(testDate))
                .filter(x -> x.getCurrencyType().equals(testCurrency))
                .collect(Collectors.toList());
        List<CursDto> cursEntitiesFilteredDateAndCurrency = cursEntities.stream()
                .filter(x -> x.getDate().equals(testDate))
                .filter(x -> x.getCurrencyType().equals(testCurrency))
                .collect(Collectors.toList());
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("date", testDate.format(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT)))
                                .param("currency", testCurrency.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(cursTestDataFilteredDateAndCurrency.size())))
                .andExpect(cursWithId("$[0]", cursEntitiesFilteredDateAndCurrency.get(0)))
                .andExpect(cursWithOutId("$[0]", cursTestDataFilteredDateAndCurrency.get(0)));

        cursTestDataFilteredDateAndCurrency = testData.stream()
                .filter(x -> x.getDate().equals(testDate))
                .collect(Collectors.toList());
        cursEntitiesFilteredDateAndCurrency = cursEntities.stream()
                .filter(x -> x.getDate().equals(testDate))
                .collect(Collectors.toList());
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("date", testDate.format(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(cursTestDataFilteredDateAndCurrency.size())))
                .andExpect(cursWithId("$[0]", cursEntitiesFilteredDateAndCurrency.get(0)))
                .andExpect(cursWithId("$[1]", cursEntitiesFilteredDateAndCurrency.get(1)))
                .andExpect(cursWithOutId("$[0]", cursTestDataFilteredDateAndCurrency.get(0)))
                .andExpect(cursWithOutId("$[1]", cursTestDataFilteredDateAndCurrency.get(1)));


        cursTestDataFilteredDateAndCurrency = testData.stream()
                .filter(x -> x.getCurrencyType().equals(testCurrency))
                .collect(Collectors.toList());
        cursEntitiesFilteredDateAndCurrency = cursEntities.stream()
                .filter(x -> x.getCurrencyType().equals(testCurrency))
                .collect(Collectors.toList());
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("currency", testCurrency.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(cursTestDataFilteredDateAndCurrency.size())))
                .andExpect(cursWithId("$[0]", cursEntitiesFilteredDateAndCurrency.get(0)))
                .andExpect(cursWithId("$[1]", cursEntitiesFilteredDateAndCurrency.get(1)))
                .andExpect(cursWithOutId("$[0]", cursTestDataFilteredDateAndCurrency.get(0)))
                .andExpect(cursWithOutId("$[1]", cursTestDataFilteredDateAndCurrency.get(1)));
    }

    @Test
    void create() throws Exception {
        long count = cursRepo.findAll().size();
        long lastId = cursRepo.findAll().stream().mapToLong(Curs::getId)
                .max().orElseThrow(NoSuchElementException::new);
        CursDto cursDto = CursDto.builder()
                .cursValue(new BigDecimal("3.5"))
                .currencyType(CurrencyType.USD)
                .date(LocalDate.now())
                .build();
        String jsonData = jsonMapper.writeValueAsString(cursDto);
        mvc.perform(post(getUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(jsonData))
                .andExpect(status().isOk())
                .andExpect(cursWithOutId("$", cursDto))
                .andExpect(jsonPath("$.id").value(++lastId));
        Assert.assertEquals(++count, cursRepo.findAll().size());
    }

    @Test
    void update() throws Exception {
        long count = cursRepo.findAll().size();
        long lastId = cursRepo.findAll().stream().mapToLong(Curs::getId)
                .max().orElseThrow(NoSuchElementException::new);
        CursDto cursDto = CursDto.builder()
                .id(lastId)
                .cursValue(new BigDecimal("4.5"))
                .currencyType(CurrencyType.EUR)
                .date(LocalDate.now())
                .build();
        String jsonData = jsonMapper.writeValueAsString(cursDto);
        mvc.perform(put(getUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(jsonData))
                .andExpect(status().isOk())
                .andExpect(cursWithId("$", cursDto));
        Assert.assertEquals(count, cursRepo.findAll().size());
    }

    @Test
    void delete() throws Exception {
        long count = cursRepo.findAll().size();
        Curs curs = cursRepo.findAll().get(0);
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                                        getUrl(String.valueOf(curs.getId()))
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Assert.assertEquals(--count, cursRepo.findAll().size());
    }

    private static ResultMatcher cursWithOutId(String prefix, CursDto cursDto) {

        return result -> {
            jsonPath(prefix + ".currencyType").value(cursDto.getCurrencyType().toString()).match(result);
            jsonPath(prefix + ".cursValue").value(cursDto.getCursValue()).match(result);
            jsonPath(prefix + ".date").value(cursDto.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).match(result);
        };
    }

    private static ResultMatcher cursWithId(String prefix, CursDto cursDto) {
        return result -> {
            jsonPath(prefix + ".id").value(cursDto.getId()).match(result);
            cursWithOutId(prefix, cursDto).match(result);
        };
    }


    private String getUrl(String path) {
        return String.format("%s/%s", getUrl(), path);
    }

    private String getUrl() {
        return "/" + CursController.ROUTE_PATH;
    }


    private List<CursDto> loadTestData() throws JsonProcessingException {
        String content = readFileFromResource("cursEntities.json");
        return Arrays.asList(jsonMapper.readValue(content, CursDto[].class));
    }

    private ObjectMapper configureJsonMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT)));
        mapper.registerModule(javaTimeModule);
        mapper.findAndRegisterModules();
        return mapper;
    }

}