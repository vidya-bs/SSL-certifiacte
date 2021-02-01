package com.itorix.apiwiz.identitymanagement.logging;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;

/**
 *
 *         The {@link GenericControllerExceptionHandler} handles exceptions
 *         thrown in the controller layer. It catches the
 *         {@link GenericControllerException} exception and
 *
 */
@ControllerAdvice
public class GenericControllerExceptionHandler{
	public static Logger logger = org.slf4j.LoggerFactory.getLogger(GenericControllerExceptionHandler.class);

	@Autowired
	LoggerService loggerService;

	@ExceptionHandler(Throwable.class)
	@ResponseBody
	public ResponseEntity<ErrorObj> handleThrowableException(final Throwable ex,
			final HttpServletResponse response, final HttpServletRequest request) {
		logger.error(ex.getMessage(),ex);
		loggerService.logException("GenericControllerExceptionHandler", "handleThrowableException",
				System.currentTimeMillis(), HttpStatus.INTERNAL_SERVER_ERROR,
				"General-1000", ErrorCodes.errorMessage.get("General-1000"), response, request);
//		ex.printStackTrace();
		ErrorObj error = new ErrorObj();
		error.setErrorMessage(ErrorCodes.errorMessage.get("General-1000"), "General-1000");
		ResponseEntity<ErrorObj> responseEntity = new ResponseEntity<ErrorObj>(error,
				HttpStatus.INTERNAL_SERVER_ERROR);
		return responseEntity;
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseBody
	public ResponseEntity<ErrorObj> processValidationError(MethodArgumentNotValidException ex, 
			final HttpServletResponse response, final HttpServletRequest request) {
	    BindingResult result = ex.getBindingResult();
	    List<FieldError> fieldErrors = result.getFieldErrors();
	    StringBuilder str = new StringBuilder(); 
	    for (FieldError fieldError : fieldErrors) {
	    	str.append(fieldError.getDefaultMessage() + " ");
	    }
	    ErrorObj error = new ErrorObj();
	    error.setErrorMessage(str.toString(), "General-1000");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(MissingServletRequestParameterException.class)
	@ResponseBody
	public ResponseEntity<ErrorObj> processValidationError(MissingServletRequestParameterException ex,
			final HttpServletResponse response, final HttpServletRequest request) {
	    ErrorObj error = new ErrorObj();
	    error.setErrorMessage(ex.getMessage(), "General-1000");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseBody
    public final ResponseEntity<ErrorObj> handleConstraintViolation(ConstraintViolationException ex,
                                            WebRequest request)
    {
        List<String> details = ex.getConstraintViolations().parallelStream() .map(e -> e.getMessage())
                                    .collect(Collectors.toList());
        ErrorObj error = new ErrorObj();
		error.setErrorMessage(details.toString(), "General-1000");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

	@ExceptionHandler(ItorixException.class)
	@ResponseBody
	public ResponseEntity<ErrorObj> handleControllerException(final ItorixException ex,
			final HttpServletResponse response, final HttpServletRequest request) throws IOException {
		loggerService.logException("GenericControllerExceptionHandler", "handleControllerException",
				System.currentTimeMillis(), HttpStatus.valueOf(ErrorCodes.responseCode.get(ex.errorCode)),
				ex.errorCode, ErrorCodes.errorMessage.get(ex.errorCode), response, request);
		ErrorObj error = new ErrorObj();
		error.setErrorMessage(ex.getMessage(), ex.errorCode);
		response.setStatus(ErrorCodes.responseCode.get(ex.errorCode));
		ResponseEntity<ErrorObj> responseEntity =  new ResponseEntity<ErrorObj>(error, HttpStatus.valueOf(ErrorCodes.responseCode.get(ex.errorCode)));
		return responseEntity;
	}

	@ExceptionHandler(ServletRequestBindingException.class)
	@ResponseBody
	public ResponseEntity<ErrorObj> handleServletRequestBindingException(final ServletRequestBindingException ex,
			final HttpServletResponse response, final HttpServletRequest request) throws IOException {
		logger.error("inside handleControllerException : {} ", ex);
		loggerService.logException("GenericControllerExceptionHandler", "handleControllerException",
				System.currentTimeMillis(), HttpStatus.valueOf(ErrorCodes.responseCode.get("General-1001")),
				"General-1001", ErrorCodes.errorMessage.get("General-1001"), response, request);
		ErrorObj error = new ErrorObj();
		error.setErrorMessage(ex.getMessage(), "General-1001");
		ResponseEntity<ErrorObj> responseEntity = new ResponseEntity<ErrorObj>(error,
				HttpStatus.BAD_REQUEST);
		return responseEntity;
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseBody
	public ResponseEntity<ErrorObj> handleAccessDeniedException(final ItorixException ex,
			final HttpServletResponse response, final HttpServletRequest request) throws IOException {
		logger.error("inside handleControllerException : {} ", ex);
		loggerService.logException("GenericControllerExceptionHandler", "handleControllerException",
				System.currentTimeMillis(), HttpStatus.valueOf(ErrorCodes.responseCode.get("IDENTITY-1015")),
				"General-1001", ErrorCodes.errorMessage.get("IDENTITY-1015"), response, request);
		ErrorObj error = new ErrorObj();
		error.setErrorMessage(ErrorCodes.errorMessage.get("IDENTITY-1015"), "IDENTITY-1015");
		response.setStatus(ErrorCodes.responseCode.get("IDENTITY-1015"));
		ResponseEntity<ErrorObj> responseEntity =  new ResponseEntity<ErrorObj>(error, HttpStatus.valueOf(ErrorCodes.responseCode.get("IDENTITY-1015")));


		return responseEntity;
	}
}