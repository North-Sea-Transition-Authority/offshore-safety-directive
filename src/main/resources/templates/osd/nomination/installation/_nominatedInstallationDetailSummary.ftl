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
      <@fdsSummaryList.summaryListRowNoAction keyText="Is this nomination for all installation activity phases?">
        <#if nominatedInstallationDetailView.forAllInstallationPhases?has_content>
          ${nominatedInstallationDetailView.forAllInstallationPhases?then('Yes', 'No')}
        </#if>
      </@fdsSummaryList.summaryListRowNoAction>
      <#if
        nominatedInstallationDetailView.forAllInstallationPhases?has_content &&
        !nominatedInstallationDetailView.forAllInstallationPhases
      >
        <@fdsSummaryList.summaryListRowNoAction keyText="Which installation activity phases is this nomination for?">
          <#list nominatedInstallationDetailView.installationPhases as phase>
            <div>${phase.screenDisplayText}</div>
          </#list>
        </@fdsSummaryList.summaryListRowNoAction>
      </#if>

      <#if nominatedInstallationDetailView.licences?has_content>
       <@fdsSummaryList.summaryListRowNoAction keyText="Licences relevant to this nomination">
          <ol class="govuk-list">
              <#list nominatedInstallationDetailView.licences as licence>
                  <li>${licence.licenceReference().value()}</li>
              </#list>
          </ol>
        </@fdsSummaryList.summaryListRowNoAction>
      </#if>
    </@fdsSummaryList.summaryList>
  </@fdsSummaryList.summaryListWrapper>
</#macro>