<#import '../macros/mailTo.ftl' as mailTo>

<#-- @ftlvariable name="technicalSupport" type="uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties" -->

<#macro technicalSupportContactDetails technicalSupportProperties>
  <p class="govuk-body">
    Email address:<br/>
    <@mailTo.mailToLink mailToEmailAddress=technicalSupport.emailAddress() />
  </p>
  <p class="govuk-body">
    Telephone:<br/>
    <strong class="govuk-!-font-weight-bold">
      ${technicalSupport.phoneNumber()}
    </strong>
  </p>
  <p class="govuk-body">
    Opening times:<br>
    <strong class="govuk-!-font-weight-bold">
      Monday Friday: ${technicalSupport.businessHoursStart()} to ${technicalSupport.businessHoursEnd()}
    </strong>
  </p>
</#macro>