<#import '_wellSelectionSetupSummary.ftl' as wellSelectionSetupSummary>
<#import '_nominatedWellDetailSummary.ftl' as nominatedWellDetailSummary>
<#import '_licenceBlockSubareaSummary.ftl' as licenceBlockSubareaSummary>

<#-- @ftlvariable name="wellSelectionSetupView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupView" -->

<#macro wellNominationSummary
  wellSelectionSetupView
  nominatedWellDetailView
  nominatedBlockSubareaDetailView
  wellSelectionSetupChangeUrl=""
  nominatedWellDetailViewChangeUrl=""
  nominatedBlockSubareaDetailViewChangeUrl=""
>
  <@wellSelectionSetupSummary.wellSelectionSetupSummary wellSelectionSetupView=wellSelectionSetupView changeUrl=wellSelectionSetupChangeUrl/>
  <#if wellSelectionSetupView.wellSelectionType?has_content && wellSelectionSetupView.wellSelectionType == "SPECIFIC_WELLS">
    <@nominatedWellDetailSummary.nominatedWellDetailSummary nominatedWellDetailView=nominatedWellDetailView changeUrl=nominatedWellDetailViewChangeUrl/>
  </#if>
  <#if wellSelectionSetupView.wellSelectionType?has_content && wellSelectionSetupView.wellSelectionType == "LICENCE_BLOCK_SUBAREA">
    <@licenceBlockSubareaSummary.licenceBlockSubareaSummary nominatedBlockSubareaDetailView=nominatedBlockSubareaDetailView changeUrl=nominatedBlockSubareaDetailViewChangeUrl/>
  </#if>
</#macro>