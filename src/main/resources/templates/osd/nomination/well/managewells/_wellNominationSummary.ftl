<#import '_wellSelectionSetupSummary.ftl' as wellSelectionSetupSummary>
<#import '_nominatedWellDetailSummary.ftl' as nominatedWellDetailSummary>

<#-- @ftlvariable name="wellSelectionSetupView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupView" -->

<#macro wellNominationSummary
  wellSelectionSetupView
  nominatedWellDetailView
  wellSelectionSetupChangeUrl=""
  nominatedWellDetailViewChangeUrl=""
>
  <@wellSelectionSetupSummary.wellSelectionSetupSummary wellSelectionSetupView=wellSelectionSetupView changeUrl=wellSelectionSetupChangeUrl/>
  <#if wellSelectionSetupView.wellSelectionType?has_content && wellSelectionSetupView.wellSelectionType == "SPECIFIC_WELLS">
    <@nominatedWellDetailSummary.nominatedWellDetailSummary nominatedWellDetailView=nominatedWellDetailView changeUrl=nominatedWellDetailViewChangeUrl/>
  </#if>
</#macro>