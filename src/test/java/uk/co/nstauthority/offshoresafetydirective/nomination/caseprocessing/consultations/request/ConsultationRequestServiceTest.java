package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request;

import static org.mockito.BDDMockito.then;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

@ExtendWith(MockitoExtension.class)
class ConsultationRequestServiceTest {

  @Mock
  private CaseEventService caseEventService;

  @Mock
  private ConsultationRequestEventPublisher consultationRequestEventPublisher;

  @InjectMocks
  private ConsultationRequestService consultationRequestService;

  @Test
  void requestConsultation_verifyInteractions() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    consultationRequestService.requestConsultation(nominationDetail);

    then(caseEventService)
        .should(onlyOnce())
        .createSentForConsultationEvent(nominationDetail);

    then(consultationRequestEventPublisher)
        .should(onlyOnce())
        .publish(new NominationId(nominationDetail.getNomination().getId()));
  }

}