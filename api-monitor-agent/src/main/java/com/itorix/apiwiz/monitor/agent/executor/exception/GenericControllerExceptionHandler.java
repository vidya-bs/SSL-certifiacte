package com.itorix.apiwiz.monitor.agent.executor.exception;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itorix.apiwiz.monitor.agent.executor.model.ErrorCodes;
import com.itorix.apiwiz.monitor.agent.executor.model.ErrorObj;

/**
 * 
 * 
 * 
 *         The {@link GenericControllerExceptionHandler} handles exceptions
 *         thrown in the controller layer. It catches the
 *         {@link GenericControllerException} exception and
 *
 */
@ControllerAdvice
public class GenericControllerExceptionHandler{
	public static Logger logger = org.slf4j.LoggerFactory.getLogger(GenericControllerExceptionHandler.class);

	@ExceptionHandler(Throwable.class)
	@ResponseBody
	public ResponseEntity<ErrorObj> handleThrowableException(final Throwable ex,
			final HttpServletResponse response) {
		logger.error("inside handleThrowableException : {} ", ex);
		
		ErrorObj error = new ErrorObj();
		error.setErrorMessage(ErrorCodes.errorMessage.get("General-1000"), "General-1000");
		ResponseEntity<ErrorObj> responseEntity = new ResponseEntity<ErrorObj>(error,
				HttpStatus.INTERNAL_SERVER_ERROR);
		return responseEntity;
	}

	@ExceptionHandler(ItorixException.class)
	@ResponseBody
	public ResponseEntity<ErrorObj> handleControllerException(final ItorixException ex,
			final HttpServletResponse response) throws IOException {
		logger.error("inside handleControllerException : {} ", ex);
	
		ErrorObj error = new ErrorObj();
		error.setErrorMessage(ex.getMessage(), ex.errorCode);
		response.setStatus(ErrorCodes.responseCode.get(ex.errorCode));
		ResponseEntity<ErrorObj> responseEntity =  new ResponseEntity<ErrorObj>(error, HttpStatus.valueOf(ErrorCodes.responseCode.get(ex.errorCode)));
		return responseEntity;
	}
	
	@ExceptionHandler(ServletRequestBindingException.class)
	@ResponseBody
	public ResponseEntity<ErrorObj> handleServletRequestBindingException(final ServletRequestBindingException ex,
			final HttpServletResponse response) throws IOException {
		logger.error("inside handleControllerException : {} ", ex);
		/*loggerService.logException("GenericControllerExceptionHandler", "handleControllerException",
				System.currentTimeMillis(), ex.getError().getHTTPStatusCode(), ex.getError().getCode(),
				ex.getError().getUserMessage());*/
		ErrorObj error = new ErrorObj();
		error.setErrorMessage("Missing request header JSESSIONID", "General-1001");
		ResponseEntity<ErrorObj> responseEntity = new ResponseEntity<ErrorObj>(error,
				HttpStatus.BAD_REQUEST);
		return responseEntity;
	}
	
	/*@ExceptionHandler(Exception.class)
	  public final ResponseEntity<ErrorObj> handleAllExceptions(Exception ex, WebRequest request) {
		ErrorObj errorDetails = new ErrorObj("", ex.getMessage());
	    return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
	  }

	  @ExceptionHandler(ItorixException.class)
	  public final ResponseEntity<ErrorObj> handleUserNotFoundException(ItorixException ex, WebRequest request) {
		  ErrorObj errorDetails = new ErrorObj("", ex.getMessage());
	    return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	  }*/

}
