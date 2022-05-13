package ru.ds.education.currency.mocks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import ru.ds.education.currency.config.properties.JMCConfigProperties;
import ru.ds.education.currency.core.dto.CursDataJMSRequest;
import ru.ds.education.currency.core.dto.CursDataJMSResponse;
import ru.ds.education.currency.util.CursJsonReader;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.text.SimpleDateFormat;

import static ru.ds.education.currency.spec.DateSpec.DATE_FORMAT_JMS_CBR;

@Service
@Slf4j
public class JmsCbrMicroserviceMock {

    @Autowired
    private JMCConfigProperties jmcConfigProperties;
    @Autowired
    private JmsTemplate jmsTemplate;

    @JmsListener(destination = "#{@JMCConfigProperties.requestQueue}")
    public void cbrMockSendAndListen(final TextMessage message) throws JMSException {
        try {
            CursDataJMSRequest request = getObjectMapper().readValue(message.getText(), CursDataJMSRequest.class);
            log.info("Parsed: {}", request);
            CursDataJMSResponse testData = loadTestData();
            if (!testData.getRates().isEmpty() && testData.getOnDate().equals(request.getOnDate())) {
                sendToQueue(testData, message.getJMSCorrelationID());
            }
        } catch (JsonProcessingException e) {
            log.error("Unparsable message received " + message.getText());
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }


    private CursDataJMSResponse loadTestData() throws JsonProcessingException {
        return CursJsonReader.readTestDataCursJMS();
    }

    private void sendToQueue(final CursDataJMSResponse response, final String correlationId) {
        jmsTemplate.send(jmcConfigProperties.getResponseQueue(), s -> {
            TextMessage message = new ActiveMQTextMessage();
            try {
                message.setText(getObjectMapper().writeValueAsString(response));
            } catch (JsonProcessingException e) {
                log.error("parse  object error " + e.getMessage());
            }
            message.setJMSCorrelationID(correlationId);
            return message;
        });
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT_JMS_CBR));
        return objectMapper;
    }

}
