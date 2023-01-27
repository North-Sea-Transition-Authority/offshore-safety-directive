package uk.co.nstauthority.offshoresafetydirective.file.virus;

import fi.solita.clamav.ClamAVClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ClamavClientProvider {

  @Bean
  ClamAVClient clamavClient(ClamavConfig clamavConfig) {
    return new ClamAVClient(clamavConfig.host(), clamavConfig.port(), clamavConfig.timeout());
  }
}
