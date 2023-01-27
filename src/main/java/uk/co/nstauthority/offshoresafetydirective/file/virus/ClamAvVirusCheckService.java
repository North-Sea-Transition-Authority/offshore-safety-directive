package uk.co.nstauthority.offshoresafetydirective.file.virus;

import fi.solita.clamav.ClamAVClient;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClamAvVirusCheckService implements VirusCheckService {
  private final ClamAVClient client;

  @Autowired
  public ClamAvVirusCheckService(ClamAVClient client) {
    this.client = client;
  }

  @Override
  public boolean hasVirus(InputStream inputStream) throws IOException {
    var reply = client.scan(inputStream);

    return !ClamAVClient.isCleanReply(reply);
  }
}
