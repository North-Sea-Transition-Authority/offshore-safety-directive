<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="businessSupport" type="uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties" -->
<#-- @ftlvariable name="technicalSupport" type="uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties" -->
<#-- @ftlvariable name="pageName" type="java.lang.String" -->

<@defaultPage
  htmlTitle=pageName
  pageHeading=pageName
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkWithBrowserBack=true
  showNavigationItems=false
  phaseBanner=(loggedInUser?has_content)
>

  <div class="govuk-body">
    <div>
      <h2 class="govuk-heading-m">Business support</h2>
      <p>For example, to report a issue with well or installation operators, or questions about filling in your nomination.</p>
      <ul class="govuk-list">
        <li>${businessSupport.mnemonic()}</li>
        <li>Email:  <@mailTo.mailToLink
          linkText=businessSupport.businessEmailAddress()
          mailToEmailAddress=businessSupport.businessEmailAddress()
          />
        </li>
      </ul>
    </div>
    <div>
      <h2 class="govuk-heading-m">Technical support</h2>
      <p>For example, unexpected problems using the service or system errors being received.</p>
      <ul class="govuk-list">
        <li>${technicalSupport.name()}</li>
        <li>Telephone: ${technicalSupport.phoneNumber()}</li>
        <li>Email:  <@mailTo.mailToLink
          linkText=technicalSupport.emailAddress()
          mailToEmailAddress=technicalSupport.emailAddress()
          />
        </li>
      </ul>
    </div>
  </div>
</@defaultPage>