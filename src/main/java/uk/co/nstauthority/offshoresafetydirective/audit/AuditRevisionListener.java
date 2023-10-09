package uk.co.nstauthority.offshoresafetydirective.audit;

import org.hibernate.envers.RevisionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;

@Service
public class AuditRevisionListener implements RevisionListener {

  private final UserDetailService userDetailService;

  @Autowired
  public AuditRevisionListener(UserDetailService userDetailService) {
    this.userDetailService = userDetailService;
  }

  @Override
  public void newRevision(Object revision) {
    var auditRevision = (AuditRevision) revision;

    userDetailService.getOptionalUserDetail().ifPresent(user -> {
      auditRevision.setUpdatedByUserId(user.wuaId());
      auditRevision.setUpdatedByProxyUserId(user.proxyWuaId());
    });
  }
}
