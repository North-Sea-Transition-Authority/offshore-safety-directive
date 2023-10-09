package uk.co.nstauthority.offshoresafetydirective.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlAuthenticationUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationRepository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;

@DatabaseIntegrationTest
class AuditRevisionListenerTest {

  @Autowired
  private NominationRepository nominationRepository;

  @Test
  void verifyEntityAuditedWithUserIds() {

    // GIVEN an insert into the nominations table
    var insertRevisionUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(100L)
        .withProxyWuaId(null)
        .build();

    SamlAuthenticationUtil.Builder()
        .withUser(insertRevisionUser)
        .setSecurityContext();

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .withReference("first reference")
        .build();

    nominationRepository.save(nomination);

    // AND an update to the same entity with a proxy user
    var updatedRevisionUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(20L)
        .withProxyWuaId(30L)
        .build();

    SamlAuthenticationUtil.Builder()
        .withUser(updatedRevisionUser)
        .setSecurityContext();

    nomination.setReference("updated reference");

    nominationRepository.save(nomination);

    // AND a delete on the same entity by another user
    var deleteRevisionUser  = ServiceUserDetailTestUtil.Builder()
        .withWuaId(20L)
        .withProxyWuaId(null)
        .build();

    SamlAuthenticationUtil.Builder()
        .withUser(deleteRevisionUser)
        .setSecurityContext();

    nominationRepository.delete(nomination);

    // THEN we will have three audit entries with the relevant type and user details
    Iterator<Revision<Long, Nomination>> auditRevisionIterator = nominationRepository
        .findRevisions(nomination.getId())
        .iterator();

    checkNextRevision(
        auditRevisionIterator,
        RevisionMetadata.RevisionType.INSERT,
        "first reference",
        insertRevisionUser
    );

    checkNextRevision(
        auditRevisionIterator,
        RevisionMetadata.RevisionType.UPDATE,
        "updated reference",
        updatedRevisionUser
    );

    checkNextRevision(
        auditRevisionIterator,
        RevisionMetadata.RevisionType.DELETE,
        null,
        deleteRevisionUser
    );

    assertThat(auditRevisionIterator.hasNext()).isFalse();
  }

  private void checkNextRevision(Iterator<Revision<Long, Nomination>> revisionIterator,
                                 RevisionMetadata.RevisionType revisionType,
                                 String reference,
                                 ServiceUserDetail user) {
    assertThat(revisionIterator.hasNext()).isTrue();

    Revision<Long, Nomination> revision = revisionIterator.next();
    assertThat(revision.getEntity().getReference()).isEqualTo(reference);
    assertThat(revision.getMetadata().getRevisionType()).isEqualTo(revisionType);

    var auditRevisionEntity = (AuditRevision) revision.getMetadata().getDelegate();
    assertThat(auditRevisionEntity.getUpdatedByUserId()).isEqualTo(user.wuaId());
    assertThat(auditRevisionEntity.getUpdatedByProxyUserId()).isEqualTo(user.proxyWuaId());
  }
}
