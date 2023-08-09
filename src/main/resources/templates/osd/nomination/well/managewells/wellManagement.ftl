<#include '../../../layout/layout.ftl'>
<#import '_wellNominationSummary.ftl' as wellNominationSummary>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="saveAndContinueUrl" type="String" -->
<#-- @ftlvariable name="breadcrumbsList" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="wellSelectionSetupView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupView" -->
<#-- @ftlvariable name="nominatedSubareaWellsView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellsView" -->
<#-- @ftlvariable name="nominatedWellDetailView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailView" -->
<#-- @ftlvariable name="nominatedWellDetailViewChangeUrl" type="String" -->
<#-- @ftlvariable name="nominatedBlockSubareaDetailView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailView" -->
<#-- @ftlvariable name="nominatedBlockSubareaDetailViewChangeUrl" type="String" -->
<#-- @ftlvariable name="excludedWellChangeUrl" type="String" -->
<#-- @ftlvariable name="excludedWellView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellView" -->

<@defaultPage
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
  breadcrumbsList=breadcrumbsList
>
  <@wellNominationSummary.wellNominationSummary
    wellSelectionSetupView=wellSelectionSetupView
    wellSelectionSetupChangeUrl=wellSelectionSetupChangeUrl
    nominatedWellDetailView=nominatedWellDetailView
    nominatedWellView=nominatedWellDetailView.wells
    nominatedWellDetailViewChangeUrl=nominatedWellDetailViewChangeUrl
    nominatedBlockSubareaDetailView=nominatedBlockSubareaDetailView
    nominatedBlockSubareaDetailViewChangeUrl=nominatedBlockSubareaDetailViewChangeUrl
    excludedWellView=excludedWellView
    excludedWellChangeUrl=excludedWellChangeUrl
    nominatedSubareaWellsView=nominatedSubareaWellsView
  />
  <@fdsAction.link linkText="Save and continue" linkUrl=springUrl(saveAndContinueUrl) linkClass="govuk-button"/>
</@defaultPage>