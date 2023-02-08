<#import '_wellSelectionSetupSummary.ftl' as wellSelectionSetupSummary>
<#import '_nominatedWellDetailSummary.ftl' as nominatedWellDetailSummary>
<#import '_licenceBlockSubareaSummary.ftl' as licenceBlockSubareaSummary>
<#import '_excludedWellSummary.ftl' as excludedWellSummary>

<#-- @ftlvariable name="wellSelectionSetupView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupView" -->
<#-- @ftlvariable name="nominatedWellDetailView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailView" -->
<#-- @ftlvariable name="nominatedBlockSubareaDetailView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailView" -->
<#-- @ftlvariable name="excludedWellView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellView" -->

<#macro wellNominationSummary
  wellSelectionSetupView
  nominatedWellDetailView
  nominatedBlockSubareaDetailView
  excludedWellView
  wellSelectionSetupChangeUrl=""
  nominatedWellDetailViewChangeUrl=""
  nominatedBlockSubareaDetailViewChangeUrl=""
  excludedWellChangeUrl=""
>
  <@wellSelectionSetupSummary.wellSelectionSetupSummary
    wellSelectionSetupView=wellSelectionSetupView
    changeUrl=wellSelectionSetupChangeUrl
  />

  <#if wellSelectionSetupView.wellSelectionType?has_content
    && wellSelectionSetupView.wellSelectionType == "SPECIFIC_WELLS"
  >
    <@nominatedWellDetailSummary.nominatedWellDetailSummary
      nominatedWellDetailView=nominatedWellDetailView
      changeUrl=nominatedWellDetailViewChangeUrl
    />
  </#if>

  <#if wellSelectionSetupView.wellSelectionType?has_content
    && wellSelectionSetupView.wellSelectionType == "LICENCE_BLOCK_SUBAREA"
  >
    <@licenceBlockSubareaSummary.licenceBlockSubareaSummary
      nominatedBlockSubareaDetailView=nominatedBlockSubareaDetailView
      changeUrl=nominatedBlockSubareaDetailViewChangeUrl
    />
    <@excludedWellSummary.excludedWellSummary
      excludedWellView=excludedWellView
      changeUrl=excludedWellChangeUrl
    />
  </#if>
</#macro>