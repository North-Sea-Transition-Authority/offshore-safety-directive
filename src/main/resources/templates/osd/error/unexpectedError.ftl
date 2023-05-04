<#include '../layout/layout.ftl'/>
<#import '_technicalSupport.ftl' as technicalSupportContact>

<#-- @ftlvariable name="technicalSupport" type="uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties" -->
<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->

<@defaultErrorPage pageHeading="Sorry, there is a problem with the service">
  <p class="govuk-body">
    Try again later.
  </p>
  <p class="govuk-body">
    If you continue to experience this problem, contact the ${technicalSupport.name()} using the
    details below.
  </p>
  <@technicalSupportContact.technicalSupportContactDetails technicalSupportProperties=technicalSupport/>
</@defaultErrorPage>