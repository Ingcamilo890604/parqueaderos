package com.prueba.tecnica.parqueaderos.general.exception;

public class TransactionConflictException extends  RuntimeException{
    public TransactionConflictException(String message) {
        super(message);
    }
}
