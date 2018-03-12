package kappa.wikiracer.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kappa.wikiracer.dao.TestDao;
import kappa.wikiracer.wiki.LinkRequest;

@RestController
public class Api {
  
  @Value("${spring.datasource.username}")
  private String dbUsername;
  
  @Value("${spring.datasource.url}")
  private String dbUrl;
  
  @Value("${spring.datasource.password}")
  private String dbPassword;

  @RequestMapping(value = "/api/test/", method = RequestMethod.GET)
  public String test() {
    return new TestDao(dbUrl, dbUsername, dbPassword).test();
  }

  @RequestMapping(value = "/api/test/wiki", method = RequestMethod.GET)
  public String wiki(
      @RequestParam(value = "title", defaultValue = "Albert Einstein") String title) {
    return LinkRequest.sendRequest(title);
  }
}
