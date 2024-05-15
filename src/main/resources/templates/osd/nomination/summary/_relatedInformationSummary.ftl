<#include "../../../fds/layout.ftl"/>

<#macro relatedInformationSummary relatedInformationSummaryView>
  <@fdsSummaryList.summaryListCard
    summaryListId=relatedInformationSummaryView.summarySectionDetails().summarySectionId().id()
    headingText=relatedInformationSummaryView.summarySectionDetails().summarySectionName().name()
    summaryListErrorMessage=(relatedInformationSummaryView.summarySectionError().errorMessage())!""
  >

      <#assign relatedToAnyFields = relatedInformationSummaryView.relatedToAnyFields()?has_content/>
      <#assign relatedToPearsApplications = relatedInformationSummaryView.relatedToPearsApplications()?has_content/>
      <#assign relatedToWonsApplications = relatedInformationSummaryView.relatedToWonsApplications()?has_content/>

      <!-- Fields -->
      <@fdsSummaryList.summaryListRowNoAction keyText="Related to any fields">
          <#if relatedToAnyFields>
            ${relatedInformationSummaryView.relatedToAnyFields().related()?then("Yes", "No")}
          </#if>
      </@fdsSummaryList.summaryListRowNoAction>
      <#if relatedToAnyFields && relatedInformationSummaryView.relatedToAnyFields().related()>
          <@fdsSummaryList.summaryListRowNoAction keyText="Related fields">
              <ol class="govuk-list">
                <#list relatedInformationSummaryView.relatedToAnyFields().fieldNames() as fieldName>
                  <li>${fieldName}</li>
                </#list>
            </ol>
          </@fdsSummaryList.summaryListRowNoAction>
      </#if>

      <!-- PEARS applications -->
      <@fdsSummaryList.summaryListRowNoAction keyText="Related to any PEARS applications">
          <#if relatedToAnyFields>
              ${relatedInformationSummaryView.relatedToPearsApplications().related()?then("Yes", "No")}
          </#if>
      </@fdsSummaryList.summaryListRowNoAction>
      <#if relatedToPearsApplications && relatedInformationSummaryView.relatedToPearsApplications().related()>
          <@fdsSummaryList.summaryListRowNoAction keyText="Related PEARS applications">
              <p class="govuk-body govuk-body__preserve-whitespace">
                ${relatedInformationSummaryView.relatedToPearsApplications().applications()}
              </p>
          </@fdsSummaryList.summaryListRowNoAction>
      </#if>

      <!-- WONS applications -->
      <@fdsSummaryList.summaryListRowNoAction keyText="Related to any WONS applications">
          <#if relatedToAnyFields>
              ${relatedInformationSummaryView.relatedToWonsApplications().related()?then("Yes", "No")}
          </#if>
      </@fdsSummaryList.summaryListRowNoAction>
      <#if relatedToWonsApplications && relatedInformationSummaryView.relatedToWonsApplications().related()>
          <@fdsSummaryList.summaryListRowNoAction keyText="Related WONS applications">
              <p class="govuk-body govuk-body__preserve-whitespace">
                  ${relatedInformationSummaryView.relatedToWonsApplications().applications()}
              </p>
          </@fdsSummaryList.summaryListRowNoAction>
      </#if>

  </@fdsSummaryList.summaryListCard>
</#macro>