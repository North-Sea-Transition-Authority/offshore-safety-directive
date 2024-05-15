<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="startActionUrl" type="String" -->
<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties" -->

<#assign pageTitle = "Submit a nomination for a well or installation operator" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsStartPage.startPage
    startActionUrl=startActionUrl
    startActionText="Start now"
  >
    <p class="govuk-body">
      Use this service to submit a nomination for a well or installation operator.
    </p>
    <p class="govuk-body">
      The appointment cannot take effect until the licensing authority has confirmed they have no objection or 3 months have elapsed and the licensing authority has not objected to the appointment.
    </p>
    <p class="govuk-body">
      Guidance can be found on the
      <@fdsAction.link linkText="${customerBranding.mnemonic()} website" linkUrl=customerBranding.guidanceUrl() openInNewTab=true/>.
    </p>
  </@fdsStartPage.startPage>
</@defaultPage>