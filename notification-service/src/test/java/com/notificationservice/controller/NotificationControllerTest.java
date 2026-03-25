package com.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.junit4.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;
import com.notificationservice.config.AppConfig;
import com.notificationservice.config.MailConfig;
import com.notificationservice.dto.UserEventDTO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.mail.internet.MimeMessage;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты для NotificationController
 * Проверяют отправку email через API
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AppConfig.class, MailConfig.class})
@WebAppConfiguration
@ActiveProfiles("test")
public class NotificationControllerTest {
    
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(new ServerSetup(3025, null, ServerSetup.PROTOCOL_SMTP));
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        greenMail.start();
    }
    
    /**
     * Тест отправки уведомления о создании аккаунта
     */
    @Test
    public void testSendCreateNotification() throws Exception {
        String email = "test@example.com";
        
        mockMvc.perform(post("/api/notifications/create")
                .param("email", email))
                .andExpect(status().isOk());
        
        // Проверяем, что email был отправлен
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        
        String content = (String) messages[0].getContent();
        assertEquals("Welcome! Account Created", messages[0].getSubject());
        assertEquals(email, messages[0].getAllRecipients()[0].toString());
        assertEquals("Hello! Your account at our website has been successfully created.", content.trim());
    }
    
    /**
     * Тест отправки уведомления об удалении аккаунта
     */