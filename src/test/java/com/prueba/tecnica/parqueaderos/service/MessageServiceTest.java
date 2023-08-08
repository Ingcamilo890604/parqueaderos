package com.prueba.tecnica.parqueaderos.service;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {
    @Mock
    private MessageSource messageSource;
    @InjectMocks()
    private MessagesService messagesService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        messagesService = new MessagesService(messageSource);
    }
    @Test
    public void testGetCannotCastMessage() {
        String expectedMessage = "Cannot cast message";
        when(messageSource.getMessage("cannot_cast_message", null, LocaleContextHolder.getLocale())).thenReturn(expectedMessage);
        String actualMessage = messagesService.getCannotCastMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}
