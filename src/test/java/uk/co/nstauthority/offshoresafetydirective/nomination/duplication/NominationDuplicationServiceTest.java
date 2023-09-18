package uk.co.nstauthority.offshoresafetydirective.nomination.duplication;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.nstauthority.offshoresafetydirective.file.FileDuplicationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    NominationDuplicationService.class
})
class NominationDuplicationServiceTest {

  @MockBean
  private FileDuplicationService fileDuplicationService;

  @MockBean
  private PrimaryTestDuplicatableService primaryTestDuplicatableService;

  @MockBean
  private SecondaryTestDuplicatableService secondaryTestDuplicatableService;

  @Autowired
  private NominationDuplicationService nominationDuplicationService;

  @Test
  void duplicateNominationDetailSections() {
    var oldNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var newNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    nominationDuplicationService.duplicateNominationDetailSections(oldNominationDetail, newNominationDetail);

    verify(primaryTestDuplicatableService).duplicate(oldNominationDetail, newNominationDetail);
    verify(secondaryTestDuplicatableService).duplicate(oldNominationDetail, newNominationDetail);
    verify(fileDuplicationService).duplicateFiles(oldNominationDetail, newNominationDetail);
  }

  @Service
  static class PrimaryTestDuplicatableService implements DuplicatableNominationService {
    @Override
    public void duplicate(NominationDetail oldNominationDetail, NominationDetail newNominationDetail) {
    }
  }

  @Service
  static class SecondaryTestDuplicatableService implements DuplicatableNominationService {
    @Override
    public void duplicate(NominationDetail oldNominationDetail, NominationDetail newNominationDetail) {
    }
  }
}