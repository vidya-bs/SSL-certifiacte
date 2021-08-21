package com.itorix.mockserver.dto;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itorix.mockserver.common.model.ErrorObj;
import com.itorix.mockserver.common.model.ItorixException;

@ControllerAdvice
public class GenericControllerExceptionHandler {
    public static Logger logger = org.slf4j.LoggerFactory.getLogger(GenericControllerExceptionHandler.class);

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseEntity<ErrorObj> handleThrowableException(final Throwable ex, final HttpServletResponse response) {
        logger.error("inside handleThrowableException : {} ", ex);
        ErrorObj error = new ErrorObj();
        if (ex.getMessage() == null)
            error.setErrorMessage(ex.getCause().getMessage(), "General-1000");
        else
            error.setErrorMessage(ex.getMessage(), "General-1000");
        ResponseEntity<ErrorObj> responseEntity = new ResponseEntity<ErrorObj>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        return responseEntity;
    }

    @ExceptionHandler(ItorixException.class)
    @ResponseBody
    public ResponseEntity<ErrorObj> handleControllerException(final ItorixException ex,
            final HttpServletResponse response) throws IOException {
        logger.error("inside handleControllerException : {} ", ex);
        ErrorObj error = new ErrorObj();
        error.setErrorMessage(ex.getMessage(), ex.errorCode);
        response.setStatus(400);
        ResponseEntity<ErrorObj> responseEntity = new ResponseEntity<ErrorObj>(error, HttpStatus.BAD_REQUEST);
        return responseEntity;
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    @ResponseBody
    public ResponseEntity<ErrorObj> handleServletRequestBindingException(final ServletRequestBindingException ex,
            final HttpServletResponse response) throws IOException {
        logger.error("inside handleControllerException : {} ", ex);
        ErrorObj error = new ErrorObj();
        error.setErrorMessage("Missing request header JSESSIONID", "General-1001");
        ResponseEntity<ErrorObj> responseEntity = new ResponseEntity<ErrorObj>(error, HttpStatus.BAD_REQUEST);
        return responseEntity;
    }

}
