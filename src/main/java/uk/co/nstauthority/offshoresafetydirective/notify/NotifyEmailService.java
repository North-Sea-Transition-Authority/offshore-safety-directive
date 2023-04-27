package uk.co.nstauthority.offshoresafetydirective.notify;

public interface NotifyEmailService {

  /**
   * Method to email a single recipient.
   * @param notifyEmail The properties for the mail merge fields in the email template
   * @param toEmailAddress The email address to send the email too
   */
  void sendEmail(NotifyEmail notifyEmail, String toEmailAddress);

  /**
   * Method to email a single recipient.
   * @param notifyEmail The properties for the mail merge fields in the email template
   * @param toEmailAddress The email address to send the email too
   * @param reference Identifies a single unique notification or a batch of notifications
   * @param emailReplyToId Specified email ID to receive replies from the users
   */
  void sendEmail(NotifyEmail notifyEmail,
                    String toEmailAddress,
                    String reference,
                    String emailReplyToId);
}
