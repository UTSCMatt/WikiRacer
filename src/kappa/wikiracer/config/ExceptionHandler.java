package kappa.wikiracer.config;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler {

  @org.springframework.web.bind.annotation.ExceptionHandler(MultipartException.class)
  public ResponseEntity<?> multipartExceptionHandler(MultipartException ex) {
    return new ResponseEntity<>(JSONObject.quote("Image exceeds max size"), HttpStatus.BAD_REQUEST);
  }

}
