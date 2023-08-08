package com.prueba.tecnica.parqueaderos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MessagesService {

    private  MessageSource messageSource;
    public MessagesService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    public String getCannotCastMessage() {
        return messageSource.getMessage("cannot_cast_message", null, LocaleContextHolder.getLocale());
    }
}
