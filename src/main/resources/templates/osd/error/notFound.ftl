<#include '../layout/layout.ftl'/>
<#import '_technicalSupport.ftl' as technicalSupportContact>

<#-- @ftlvariable name="technicalSupport" type="uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties" -->
<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->

<@defaultErrorPage pageHeading="Page not found">
  <p class="govuk-body">
    If you typed the web address, check it is correct.
  </p>
  <p class="govuk-body">
    If you pasted the web address, check you copied the entire address.
  </p>
  <p class="govuk-body">
    If the web address is correct or you selected a link or button, contact the ${technicalSupport.name()}
    using the details below:
  </p>
  <@technicalSupportContact.technicalSupportContactDetails technicalSupportProperties=technicalSupport/>
</@defaultErrorPage>