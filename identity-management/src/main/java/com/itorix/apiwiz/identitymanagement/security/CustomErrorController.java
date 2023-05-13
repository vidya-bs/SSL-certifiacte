package com.itorix.apiwiz.identitymanagement.security;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController  implements ErrorController {

  @GetMapping("/error")
  public String handleError(HttpServletRequest request) {
    String errorPage = "404"; // default

    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

    if (status != null) {
      Integer statusCode = Integer.valueOf(status.toString());
      if (statusCode == HttpStatus.NOT_FOUND.value()) {
        // handle HTTP 404 Not Found error
        errorPage = "404.html";
      }
    }

    return errorPage;
  }

  public String getErrorPath() {
    return "/error";
  }
}