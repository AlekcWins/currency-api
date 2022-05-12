package ru.ds.education.currency.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.ds.education.currency.core.dto.CursDataJMSRequest;
import ru.ds.education.currency.core.dto.CursDataJMSResponse;
import ru.ds.education.currency.core.dto.CursDto;
import ru.ds.education.currency.model.CurrencyType;
import ru.ds.education.currency.model.CursRequestStatus;
import ru.ds.education.currency.repository.CurrencyTypesRepo;

import javax.jms.Queue;
import javax.jms.TextMessage;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class JMSCursRequestService {

    private final JmsTemplate jmsTemplate;

    private final JmsTemplate receiveTemplate;

    private final CursRequestService cursRequestService;

    private final CurrencyTypesRepo currencyTypesRepo;
    private final CursService cursService;
    private final ObjectMapper mapper;
    private final Queue responseQueue;
    private final Queue requestQueue;


    @Async
    public void sendAndReceive(LocalDate date, String correlationId) {
        try {
            jmsTemplate.send(requestQueue, s -> {
                TextMessage message = new ActiveMQTextMessage();
                try {
                    CursDataJMSRequest cursDataJMSRequest = new CursDataJMSRequest(date);
                    message.setText(mapper.writeValueAsString(cursDataJMSRequest));
                } catch (JsonProcessingException e) {
                    cursRequestService.updateStatus(date, CursRequestStatus.FAILED);
                }
                message.setJMSCorrelationID(correlationId);
                cursRequestService.updateStatus(date, CursRequestStatus.SENT);
                return message;
            });
            CursDataJMSResponse response = (CursDataJMSResponse) receiveTemplate
                    .receiveSelectedAndConvert(responseQueue, "JMSCorrelationID='" + correlationId + "'");
            cursService.deleteCursByDate(date);
            if (response == null) {
                cursRequestService.updateStatus(date, CursRequestStatus.FAILED);
                return;
            }
            List<String> allTypesCurrency = currencyTypesRepo.findAll()
                    .stream().map(CurrencyType::getCurrencyType).collect(Collectors.toList());

            response.getRates()
                    .forEach(curs ->
                            {
                                if (!Objects.isNull(curs.getCurrency()) && allTypesCurrency.contains(curs.getCurrency()))
                                    cursService.create(
                                            CursDto.builder()
                                                    .cursValue(curs.getCurs())
                                                    .currencyType(curs.getCurrency())
                                                    .date(date)
                                                    .build()
                                    );
                            }
                    );
            cursRequestService.updateStatus(date, CursRequestStatus.PROCESSED);
        } catch (JmsException e) {
            cursRequestService.updateStatus(date, CursRequestStatus.FAILED);
        }

    }
}


