<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="nominatedInstallationDetailView" type="uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationDetailView" -->

<#macro nominatedInstallationDetailSummary nominatedInstallationDetailView changeUrl="">
  <@fdsSummaryList.summaryListWrapper
    headingText="Installation nominations"
    summaryListId="summary-list-nominated-installation-detail"
    showSummaryListActions=changeUrl?has_content
    changeLinkUrl=springUrl(changeUrl)
  >
    <@fdsSummaryList.summaryList>
      <@fdsSummaryList.summaryListRowNoAction keyText="Installations">
        <#list nominatedInstallationDetailView.installations as installation>
          <div>${installation.name()}</div>
        </#list>
      </@fdsSummaryList.summaryListRowNoAction>
      <@fdsSummaryList.summaryListRowNoAction keyText="Is this nomination for all installation phases?">
        <#if nominatedInstallationDetailView.forAllInstallationPhases?has_content>
          ${nominatedInstallationDetailView.forAllInstallationPhases?then('Yes', 'No')}
        </#if>
      </@fdsSummaryList.summaryListRowNoAction>
      <#if nominatedInstallationDetailView.forAllInstallationPhases?has_content && !nominatedInstallationDetailView.forAllInstallationPhases>
        <@fdsSummaryList.summaryListRowNoAction keyText="Which installations phases is this nomination for?">
          <#list nominatedInstallationDetailView.installationPhases as phase>
            <div>${phase.screenDisplayText}</div>
          </#list>
        </@fdsSummaryList.summaryListRowNoAction>
      </#if>
    </@fdsSummaryList.summaryList>
  </@fdsSummaryList.summaryListWrapper>
</#macro>