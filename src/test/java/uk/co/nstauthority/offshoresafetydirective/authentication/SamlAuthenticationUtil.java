package uk.co.nstauthority.offshoresafetydirective.authentication;

import java.util.Collection;
import java.util.HashSet;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class SamlAuthenticationUtil {

  public SamlAuthenticationUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static ContextBuilder ContextBuilder() {
    return new ContextBuilder();
  }

  public static class ContextBuilder {

    private ContextBuilder() {
    }

    private Long wuaId = 1L;
    private Long personId = 2L;
    private String forename = "Forename";
    private String surname = "Surname";
    private String emailAddress = "test.user@test.com";
    private Collection<GrantedAuthority> grantedAuthorities = new HashSet<>();

    public ServiceUserDetail buildAndApply() {
      var samlUser = new ServiceUserDetail(wuaId, personId, forename, surname, emailAddress);

      var authentication = new ServiceSaml2Authentication(samlUser, grantedAuthorities);
      SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

      return samlUser;
    }

    public ContextBuilder withWuaId(Long wuaId) {
      this.wuaId = wuaId;
      return this;
    }

    public ContextBuilder withPersonId(Long personId) {
      this.personId = personId;
      return this;
    }

    public ContextBuilder withForename(String forename) {
      this.forename = forename;
      return this;
    }

    public ContextBuilder withSurname(String surname) {
      this.surname = surname;
      return this;
    }

    public ContextBuilder withtEmailAddress(String emailAddress) {
      this.emailAddress = emailAddress;
      return this;
    }

    public ContextBuilder withGrantedAuthorities(Collection<GrantedAuthority> grantedAuthorities) {
      this.grantedAuthorities.addAll(grantedAuthorities);
      return this;
    }

    public ContextBuilder withGrantedAuthority(GrantedAuthority grantedAuthority) {
      this.grantedAuthorities.add(grantedAuthority);
      return this;
    }

    public ContextBuilder withGrantedAuthority(String grantedAuthority) {
      this.grantedAuthorities.add(new SimpleGrantedAuthority(grantedAuthority));
      return this;
    }
  }

}
