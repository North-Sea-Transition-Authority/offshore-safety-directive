<#include '../../../layout/layout.ftl'>
<#import '_wellNominationSummary.ftl' as wellNominationSummary>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="saveAndContinueUrl" type="String" -->
<#-- @ftlvariable name="breadcrumbsList" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="wellSelectionSetupView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupView" -->

<@defaultPage
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
  breadcrumbsList=breadcrumbsList
>
  <@wellNominationSummary.wellNominationSummary
    wellSelectionSetupView=wellSelectionSetupView
    wellSelectionSetupChangeUrl=wellSelectionSetupChangeUrl
    nominatedWellDetailView=nominatedWellDetailView
    nominatedWellDetailViewChangeUrl=nominatedWellDetailViewChangeUrl
    nominatedBlockSubareaDetailView=nominatedBlockSubareaDetailView
    nominatedBlockSubareaDetailViewChangeUrl=nominatedBlockSubareaDetailViewChangeUrl
    excludedWellView=excludedWellView
    excludedWellChangeUrl=excludedWellChangeUrl
  />
  <@fdsAction.link linkText="Save and continue" linkUrl=springUrl(saveAndContinueUrl) linkClass="govuk-button"/>
</@defaultPage>