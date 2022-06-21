package uk.co.nstauthority.offshoresafetydirective;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OffshoreSafetyDirectiveApplication {

  public static void main(String[] args) {
    SpringApplication.run(OffshoreSafetyDirectiveApplication.class, args);
  }

}
