<#import '_wellSelectionSetupSummary.ftl' as wellSelectionSetupSummary>
<#import '_nominatedWellDetailSummary.ftl' as nominatedWellDetailSummary>
<#import '_licenceBlockSubareaSummary.ftl' as licenceBlockSubareaSummary>
<#import '_excludedWellSummary.ftl' as excludedWellSummary>
<#import '_nominatedWellListSummary.ftl' as nominatedWellListSummary>

<#-- @ftlvariable name="wellSelectionSetupView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupView" -->
<#-- @ftlvariable name="nominatedWellDetailView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailView" -->
<#-- @ftlvariable name="nominatedBlockSubareaDetailView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailView" -->
<#-- @ftlvariable name="excludedWellView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellView" -->

<#macro wellNominationSummary
  wellSelectionSetupView
  nominatedWellDetailView
  nominatedBlockSubareaDetailView
  nominatedWellView
  excludedWellView
  nominatedSubareaWellsView
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
    <@nominatedWellListSummary.nominatedWellListSummary
      nominatedWellsView=nominatedWellView
      wellSelectionType=wellSelectionSetupView.wellSelectionType
    />
  </#if>

  <#if wellSelectionSetupView.wellSelectionType?has_content
    && wellSelectionSetupView.wellSelectionType == "LICENCE_BLOCK_SUBAREA"
  >
    <@licenceBlockSubareaSummary.licenceBlockSubareaSummary
      nominatedBlockSubareaDetailView=nominatedBlockSubareaDetailView
      changeUrl=nominatedBlockSubareaDetailViewChangeUrl
    />
    <#if nominatedBlockSubareaDetailView.licenceBlockSubareas?has_content>
      <@excludedWellSummary.excludedWellSummary
        excludedWellView=excludedWellView
        changeUrl=excludedWellChangeUrl
      />
      <@nominatedWellListSummary.nominatedWellListSummary
        nominatedWellsView=nominatedSubareaWellsView.nominatedSubareaWellbores()
        wellSelectionType=wellSelectionSetupView.wellSelectionType
      />
    </#if>
  </#if>
</#macro>