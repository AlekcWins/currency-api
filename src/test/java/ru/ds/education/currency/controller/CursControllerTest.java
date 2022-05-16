package ru.ds.education.currency.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.ds.education.currency.BaseActiveMQContainer;
import ru.ds.education.currency.core.dto.CursDataJMSResponse;
import ru.ds.education.currency.core.dto.CursDto;
import ru.ds.education.currency.core.dto.mapper.CursMapper;
import ru.ds.education.currency.model.CurrencyType;
import ru.ds.education.currency.model.Curs;
import ru.ds.education.currency.model.CursRequestStatus;
import ru.ds.education.currency.repository.CurrencyTypesRepo;
import ru.ds.education.currency.repository.CursRepo;
import ru.ds.education.currency.repository.CursRequestRepo;
import ru.ds.education.currency.spec.DateSpec;
import ru.ds.education.currency.util.CursJsonReader;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.ds.education.currency.util.CursJsonReader.getJsonMapperForCursData;

@SpringBootTest()
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
class CursControllerTest extends BaseActiveMQContainer {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CursRepo cursRepo;

    @Autowired
    private CursRequestRepo cursRequestRepo;

    @Autowired
    private CursMapper cursMapper;
    @Autowired
    private CurrencyTypesRepo currencyTypesRepo;


    private ObjectMapper jsonMapper;


    @PostConstruct
    public void init() {
        jsonMapper = getJsonMapperForCursData();
    }


    @BeforeEach
    public void createDbData() throws Exception {
        cursRepo.deleteAll();
        cursRequestRepo.deleteAll();
        List<Curs> cursEntities = loadTestData();
        cursRepo.saveAll(cursEntities);
    }

    @Test
    @WithMockUser
    void getWithIdAndOkStatus() throws Exception {
        Curs curs = cursRepo.findAll().get(0);
        CursDto cursDto = cursMapper.map(curs, CursDto.class);


        mvc.perform(get(getUrl(String.valueOf(cursDto.getId()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cursWithId("$", cursDto));

    }

    @Test
    @WithMockUser
    void getWithIdAndBadStatus() throws Exception {
        long count = cursRepo.count();


        mvc.perform(get(getUrl(String.valueOf(++count))))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getAll() throws Exception {
        List<CursDto> testData = convertTestData(loadTestData());
        long count = cursRepo.count();
        List<CursDto> cursEntities = cursRepo.findAll()
                .stream().map(x -> cursMapper.map(x, CursDto.class))
                .collect(Collectors.toList());


        mvc.perform(get(getUrl()).contentType(MediaType.APPLICATION_JSON))
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

    }

    @Test
    @WithMockUser
    void getAllWithDateParameterWhenHaveDataInCursDatabase() throws Exception {
        List<CursDto> testData = convertTestData(loadTestData());
        LocalDate testDate = LocalDate.of(2022, 5, 14);
        List<CursDto> cursTestDataFilteredDateAndCurrency = testData.stream()
                .filter(x -> x.getDate().equals(testDate))
                .collect(Collectors.toList());
        List<CursDto> cursEntitiesFilteredDateAndCurrency = cursRepo.findAll().stream()
                .filter(x -> x.getDate().equals(testDate)).map(x -> cursMapper.map(x, CursDto.class))
                .collect(Collectors.toList());

        mvc.perform(
                        get(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("date", testDate.format(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(cursTestDataFilteredDateAndCurrency.size())))
                .andExpect(cursWithId("$[0]", cursEntitiesFilteredDateAndCurrency.get(0)))
                .andExpect(cursWithId("$[1]", cursEntitiesFilteredDateAndCurrency.get(1)))
                .andExpect(cursWithOutId("$[0]", cursTestDataFilteredDateAndCurrency.get(0)))
                .andExpect(cursWithOutId("$[1]", cursTestDataFilteredDateAndCurrency.get(1)));
    }

    @Test
    @WithMockUser
    void getAllWithDateParameterWhenHaveNotDataInCursDatabase() throws Exception {
        CursDataJMSResponse testDataCursJMS = CursJsonReader.readTestDataCursJMS();
        LocalDate testDate = LocalDate.of(2026, 2, 1);
        int id = 1;
        List<String> allTypesCurrency = currencyTypesRepo.findAll()
                .stream().map(CurrencyType::getCurrencyType).collect(Collectors.toList());

        List<CursDto> testData = testDataCursJMS.getRates().stream()
                .filter(x -> allTypesCurrency.contains(x.getCurrency()))
                .map(x -> new CursDto(id, x.getCurrency(), x.getCurs(), testDate))
                .collect(Collectors.toList());


        mvc.perform(
                        get(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("date", testDate.format(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT)))
                )
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").doesNotExist());

        TimeUnit.SECONDS.sleep(11);

        mvc.perform(
                        get(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("date", testDate.format(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(testData.size())))
                .andExpect(cursWithOutId("$[0]", testData.get(0)))
                .andExpect(cursWithOutId("$[1]", testData.get(1)));
        Assert.assertEquals(1, cursRequestRepo.count());
        Assert.assertEquals(CursRequestStatus.PROCESSED, cursRequestRepo.findAll().iterator().next().getStatus());

    }

    @Test
    @WithMockUser
    void getAllWithDateParameterWhenHaveNotDataInCursDatabaseFailedRequest() throws Exception {
        LocalDate testDate = LocalDate.of(2030, 2, 1);

        mvc.perform(
                        get(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("date", testDate.format(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT)))
                )
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").doesNotExist());

        TimeUnit.SECONDS.sleep(11);

        mvc.perform(
                        get(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("date", testDate.format(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT)))
                )
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").doesNotExist());

        Assert.assertEquals(1, cursRequestRepo.count());
        Assert.assertEquals(CursRequestStatus.FAILED, cursRequestRepo.findAll().iterator().next().getStatus());

    }

    @Test
    @WithMockUser
    void getAllWithCurrencyParameter() throws Exception {
        List<CursDto> testData = convertTestData(loadTestData());

        String testCurrency = "USD";

        List<CursDto> cursTestDataFilteredDateAndCurrency = testData.stream()
                .filter(x -> x.getCurrencyType().equals(testCurrency))
                .collect(Collectors.toList());

        List<CursDto> cursEntitiesFilteredDateAndCurrency = cursRepo.findAll().stream()
                .filter(x -> x.getCurrencyType().getCurrencyType().equals(testCurrency))
                .map(x -> cursMapper.map(x, CursDto.class))
                .collect(Collectors.toList());


        mvc.perform(
                        get(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("currency", testCurrency)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(cursTestDataFilteredDateAndCurrency.size())))
                .andExpect(cursWithId("$[0]", cursEntitiesFilteredDateAndCurrency.get(0)))
                .andExpect(cursWithId("$[1]", cursEntitiesFilteredDateAndCurrency.get(1)))
                .andExpect(cursWithOutId("$[0]", cursTestDataFilteredDateAndCurrency.get(0)))
                .andExpect(cursWithOutId("$[1]", cursTestDataFilteredDateAndCurrency.get(1)));
    }

    @Test
    @WithMockUser
    void getAllWithDateAndCurrencyParameters() throws Exception {
        List<CursDto> testData = convertTestData(loadTestData());
        List<CursDto> cursEntities = cursRepo.findAll().stream()
                .map(x -> cursMapper.map(x, CursDto.class))
                .collect(Collectors.toList());
        LocalDate testDate = LocalDate.of(2022, 5, 14);
        String testCurrency = "USD";
        List<CursDto> cursTestDataFilteredDateAndCurrency = testData.stream()
                .filter(x -> x.getDate().equals(testDate)).filter(x -> x.getCurrencyType().equals(testCurrency))
                .collect(Collectors.toList());
        List<CursDto> cursEntitiesFilteredDateAndCurrency = cursEntities.stream()
                .filter(x -> x.getDate().equals(testDate))
                .filter(x -> x.getCurrencyType().equals(testCurrency))
                .collect(Collectors.toList());


        mvc.perform(
                        get(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("date", testDate.format(DateTimeFormatter.ofPattern(DateSpec.DATE_FORMAT)))
                                .param("currency", testCurrency)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(cursTestDataFilteredDateAndCurrency.size())))
                .andExpect(cursWithId("$[0]", cursEntitiesFilteredDateAndCurrency.get(0)))
                .andExpect(cursWithOutId("$[0]", cursTestDataFilteredDateAndCurrency.get(0)));
    }

    @Test
    @WithMockUser
    void create() throws Exception {
        long count = cursRepo.findAll().size();
        long lastId = cursRepo.findAll().stream()
                .mapToLong(Curs::getId).max()
                .orElseThrow(NoSuchElementException::new);
        CursDto cursDto = CursDto.builder()
                .cursValue(new BigDecimal("3.5"))
                .currencyType("USD")
                .date(LocalDate.now())
                .build();
        String jsonData = jsonMapper.writeValueAsString(cursDto);


        mvc.perform(
                        post(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(jsonData)
                )
                .andExpect(status().isOk())
                .andExpect(cursWithOutId("$", cursDto))
                .andExpect(jsonPath("$.id").value(++lastId));
        Assert.assertEquals(++count, cursRepo.findAll().size());
    }

    @Test
    @WithMockUser
    void createBadRequest() throws Exception {
        long count = cursRepo.findAll().size();
        CursDto cursDto = CursDto.builder()
                .cursValue(new BigDecimal("3.5"))
                .currencyType("TEST")
                .date(LocalDate.now())
                .build();
        String jsonData = jsonMapper.writeValueAsString(cursDto);


        mvc.perform(
                        post(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(jsonData)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").doesNotExist());
        Assert.assertEquals(count, cursRepo.findAll().size());
    }

    @Test
    @WithMockUser
    void update() throws Exception {
        long count = cursRepo.findAll().size();
        long lastId = cursRepo.findAll().stream().mapToLong(Curs::getId).max().orElseThrow(NoSuchElementException::new);
        CursDto cursDto = CursDto.builder()
                .id(lastId)
                .cursValue(new BigDecimal("4.5"))
                .currencyType("EUR")
                .date(LocalDate.now())
                .build();
        String jsonData = jsonMapper.writeValueAsString(cursDto);


        mvc.perform(
                        put(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON).content(jsonData)
                )
                .andExpect(status().isOk())
                .andExpect(cursWithId("$", cursDto));
        Assert.assertEquals(count, cursRepo.findAll().size());
    }

    @Test
    @WithMockUser
    void updateBadRequest() throws Exception {
        long count = cursRepo.findAll().size();
        CursDto cursDto = CursDto.builder()
                .id(count)
                .cursValue(new BigDecimal("4.5"))
                .currencyType("TEST")
                .date(LocalDate.now())
                .build();
        String jsonData = jsonMapper.writeValueAsString(cursDto);

        mvc.perform(
                        put(getUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON).content(jsonData)
                )
                .andExpect(status().isBadRequest());
        Assert.assertEquals(count, cursRepo.findAll().size());
    }

    @Test
    @WithMockUser
    void delete() throws Exception {
        long count = cursRepo.findAll().size();
        Curs curs = cursRepo.findAll().get(0);

        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                                        getUrl(String.valueOf(curs.getId()))
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
        Assert.assertEquals(--count, cursRepo.findAll().size());
    }

    private static ResultMatcher cursWithOutId(String prefix, CursDto cursDto) {

        return result -> {
            jsonPath(prefix + ".currencyType").value(cursDto.getCurrencyType()).match(result);
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


    private List<Curs> loadTestData() throws JsonProcessingException {
        return CursJsonReader.readTestDataCurs();
    }

    private List<CursDto> convertTestData(List<Curs> curses) {
        return curses.stream()
                .map(x -> cursMapper.map(x, CursDto.class))
                .collect(Collectors.toList());
    }


}