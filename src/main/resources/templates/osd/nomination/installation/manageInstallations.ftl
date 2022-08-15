<#include '../../layout/layout.ftl'>
<#import '_installationNominationSummary.ftl' as installationNominationSummary>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="saveAndContinueUrl" type="String" -->
<#-- @ftlvariable name="breadcrumbsList" type="java.util.Map<String, String>" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
  breadcrumbsList=breadcrumbsList
>
  <@installationNominationSummary.installationNominationSummary
    installationInclusionView=installationInclusionView
    installationInclusionChangeUrl=installationInclusionChangeUrl
    nominatedInstallationDetailView=nominatedInstallationDetailView
    nominatedInstallationDetailChangeUrl=nominatedInstallationDetailChangeUrl
  />
  <@fdsAction.link linkText="Save and continue" linkUrl=springUrl(saveAndContinueUrl) linkClass="govuk-button"/>
</@defaultPage>