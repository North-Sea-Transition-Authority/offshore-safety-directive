<#include "../../../fds/layout.ftl"/>

<#macro installationSummary installationSummaryView>
  <@fdsSummaryList.summaryListCard
    summaryListId=installationSummaryView.summarySectionDetails().summarySectionId().id()
    headingText=installationSummaryView.summarySectionDetails().summarySectionName().name()
    summaryListErrorMessage=(installationSummaryView.summarySectionError().errorMessage())!""
  >

      <#assign relatedToInstallation = installationSummaryView.installationRelatedToNomination()?has_content/>
      <#assign installationPhaseContent = installationSummaryView.installationForAllPhases()?has_content/>

      <@fdsSummaryList.summaryListRowNoAction keyText="In relation to installation operatorship">
          <#if relatedToInstallation>
            ${installationSummaryView.installationRelatedToNomination().related()?then("Yes", "No")}
          </#if>
      </@fdsSummaryList.summaryListRowNoAction>
      <#if relatedToInstallation && installationSummaryView.installationRelatedToNomination().related()>
          <@fdsSummaryList.summaryListRowNoAction keyText="Nominated installations">
              <ol class="govuk-list">
                <#list installationSummaryView.installationRelatedToNomination().relatedInstallations() as installationName>
                  <li>${installationName}</li>
                </#list>
            </ol>
          </@fdsSummaryList.summaryListRowNoAction>
      </#if>

      <#if relatedToInstallation && installationSummaryView.installationRelatedToNomination().related()>
        <@fdsSummaryList.summaryListRowNoAction keyText="Is this nomination for all installation activity phases?">
          <#if installationPhaseContent>
            ${installationSummaryView.installationForAllPhases().forAllPhases()?then("Yes", "No")}
          </#if>
        </@fdsSummaryList.summaryListRowNoAction>
        <#if installationPhaseContent && !installationSummaryView.installationForAllPhases().forAllPhases()>
          <@fdsSummaryList.summaryListRowNoAction keyText="Which installation activity phases is this nomination for?">
            <ol class="govuk-list">
              <#list installationSummaryView.installationForAllPhases().phases() as installationPhase>
                <li>${installationPhase}</li>
              </#list>
            </ol>
          </@fdsSummaryList.summaryListRowNoAction>
        </#if>
      </#if>

      <#if installationSummaryView.relatedLicenceReferences()?has_content>
          <@fdsSummaryList.summaryListRowNoAction keyText="Licences relevant to this nomination">
              <ol class="govuk-list">
                  <#list installationSummaryView.relatedLicenceReferences() as licence>
                      <li>${licence}</li>
                  </#list>
              </ol>
          </@fdsSummaryList.summaryListRowNoAction>
      </#if>

  </@fdsSummaryList.summaryListCard>
</#macro>