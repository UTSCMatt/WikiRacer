package kappa.wikiracer.runner;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
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

  @Bean
  public EmbeddedServletContainerFactory servletContainer() {
    TomcatEmbeddedServletContainerFactory tomcat =
        new TomcatEmbeddedServletContainerFactory() {

          @Override
          protected void postProcessContext(Context context) {
            SecurityConstraint securityConstraint = new SecurityConstraint();
            securityConstraint.setUserConstraint("CONFIDENTIAL");
            SecurityCollection collection = new SecurityCollection();
            collection.addPattern("/*");
            securityConstraint.addCollection(collection);
            context.addConstraint(securityConstraint);
          }
        };
    tomcat.addAdditionalTomcatConnectors(createHttpConnector());
    return tomcat;
  }

  @Value("${server.port.http}")
  private int serverPortHttp;

  @Value("${server.port}")
  private int serverPortHttps;

  @Value("${server.port.publicHttps}")
  private int redirectPort;

  private Connector createHttpConnector() {
    Connector connector =
        new Connector("org.apache.coyote.http11.Http11NioProtocol");
    connector.setScheme("http");
    connector.setSecure(false);
    connector.setPort(serverPortHttp);
    connector.setRedirectPort(redirectPort);
    return connector;
  }

}
