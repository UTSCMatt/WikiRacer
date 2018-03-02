package kappa.wikiracer.runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"kappa.wikiracer.api"})
public class Runner {

  public static void main(String[] args) {
    SpringApplication.run(Runner.class, args);

  }

}
