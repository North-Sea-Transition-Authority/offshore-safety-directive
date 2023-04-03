package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

import uk.co.fivium.energyportalmessagequeue.message.EpmqMessage;

public abstract class OsdEpmqMessage extends EpmqMessage {

  OsdEpmqMessage(String type) {
    super("OSD", type);
  }
}
