package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

import java.time.Instant;
import uk.co.fivium.energyportalmessagequeue.message.EpmqMessage;

public abstract class OsdEpmqMessage extends EpmqMessage {

  OsdEpmqMessage(String type, String correlationId, Instant createdInstant) {
    super("OSD", type, correlationId, createdInstant);
  }
}
