package ru.ds.education.currency.config;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import ru.ds.education.currency.config.properties.JMCConfigProperties;
import ru.ds.education.currency.core.dto.CursDataJMSResponse;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.Queue;
import java.text.SimpleDateFormat;

import static ru.ds.education.currency.spec.DateSpec.DATE_FORMAT_JMS_CBR;

@Configuration
@EnableJms
@Slf4j
@RequiredArgsConstructor
public class JMSConfig {

    private final JMCConfigProperties jmcConfigProperties;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT_JMS_CBR));
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        return objectMapper;
    }

    @Bean
    public Queue responseQueue() {
        return new ActiveMQQueue(jmcConfigProperties.getResponseQueue());
    }

    @Bean
    public Queue requestQueue() {
        return new ActiveMQQueue(jmcConfigProperties.getRequestQueue());
    }

    @Bean
    public JmsTemplate receiveTemplate(ConnectionFactory connectionFactory, MessageConverter responseMessageConverter) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setReceiveTimeout(10000);
        jmsTemplate.setMessageConverter(responseMessageConverter);
        return jmsTemplate;
    }


    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

    @Bean
    public MessageConverter responseMessageConverter(ObjectMapper objectMapper) {

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter() {
            @Override
            protected JavaType getJavaTypeForMessage(Message message) {
                return TypeFactory.defaultInstance().constructType(CursDataJMSResponse.class);
            }
        };

        converter.setObjectMapper(objectMapper);
        converter.setTargetType(MessageType.TEXT);
        return converter;
    }


}
