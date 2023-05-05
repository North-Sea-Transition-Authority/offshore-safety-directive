<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="startActionUrl" type="java.lang.String" -->
<#-- @ftlvariable name="backLinkUrl" type="java.lang.String" -->
<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties" -->
<#-- @ftlvariable name="reasonForUpdate" type="java.lang.String" -->

<#assign pageTitle = "Update nomination" />

<@defaultPage
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsStartPage.startPage
    startActionUrl=startActionUrl
    startActionText="Start update"
  >
    <p class="govuk-body">
      Update your nomination with the information requested by ${customerBranding.mnemonic()}. This may involve correcting details of the nomination or providing updated supporting documentation.
    </p>
    <@fdsDetails.summaryDetails summaryTitle="What information have I been asked to update?">
      <p class="govuk-body">
        ${reasonForUpdate}
      </p>
    </@fdsDetails.summaryDetails>
  </@fdsStartPage.startPage>
</@defaultPage>