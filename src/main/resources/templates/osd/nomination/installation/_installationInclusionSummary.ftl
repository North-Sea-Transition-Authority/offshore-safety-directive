<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="installationInclusionView" type="uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionView" -->

<#macro installationInclusionSummary installationInclusionView changeUrl="">
  <@fdsSummaryList.summaryListWrapper
    headingText="Nomination for installations"
    summaryListId="summary-list-installation-advice"
    showSummaryListActions=changeUrl?has_content
    changeLinkUrl=springUrl(changeUrl)
  >
    <@fdsSummaryList.summaryList>
       <@fdsSummaryList.summaryListRowNoAction keyText="Is this nomination in relation to installation operatorship?">
         <#if installationInclusionView.includeInstallationsInNomination?has_content>
           ${installationInclusionView.includeInstallationsInNomination?then('Yes', 'No')}
         </#if>
       </@fdsSummaryList.summaryListRowNoAction>
    </@fdsSummaryList.summaryList>
  </@fdsSummaryList.summaryListWrapper>
</#macro>