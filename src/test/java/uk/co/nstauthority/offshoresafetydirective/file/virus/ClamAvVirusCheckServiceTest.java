package uk.co.nstauthority.offshoresafetydirective.file.virus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fi.solita.clamav.ClamAVClient;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClamAvVirusCheckServiceTest {

  @Mock
  private ClamAVClient client;
  private ClamAvVirusCheckService clamAvVirusCheckService;

  @BeforeEach
  void setUp() {
    clamAvVirusCheckService = new ClamAvVirusCheckService(client);
  }

  @Test
  void hasVirus_VirusNotFound() throws IOException {
    var inputStream = Mockito.mock(InputStream.class);
    var reply = "OK".getBytes();

    when(client.scan(inputStream)).thenReturn(reply);

    boolean hasVirus = clamAvVirusCheckService.hasVirus(inputStream);

    assertThat(hasVirus).isFalse();
  }

  @Test
  void hasVirus_VirusFound() throws IOException {
    var inputStream = Mockito.mock(InputStream.class);
    var reply = "FOUND".getBytes();

    when(client.scan(inputStream)).thenReturn(reply);

    boolean hasVirus = clamAvVirusCheckService.hasVirus(inputStream);

    assertThat(hasVirus).isTrue();
  }
}