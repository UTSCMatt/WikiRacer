package kappa.wikiracer.runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"kappa.wikiracer.api", "kappa.wikiracer.config",
    "kappa.wikiracer.util"})
public class Runner {

  /**
   * Run the spring web app.
   */
  public static void main(String[] args) {
    System.setProperty("spring.devtools.restart.enabled", "true");
    SpringApplication.run(Runner.class, args);

  }

}
