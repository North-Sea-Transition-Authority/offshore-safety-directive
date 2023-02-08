<#include '../../../layout/layout.ftl'>

<#-- @ftlvariable name="nominatedBlockSubareaDetailView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailView" -->

<#macro licenceBlockSubareaSummary nominatedBlockSubareaDetailView changeUrl="">
  <@fdsSummaryList.summaryListWrapper
    headingText="Licence block subarea nominations"
    headingClass="govuk-heading-m"
    summaryListId="summary-list-subarea-nomination-type"
    showSummaryListActions=changeUrl?has_content
    changeLinkUrl=springUrl(changeUrl)
  >
    <@fdsSummaryList.summaryList>
      <@fdsSummaryList.summaryListRowNoAction keyText="Licence block subareas">
        <#list nominatedBlockSubareaDetailView.licenceBlockSubareas as subarea>
          <p class="govuk-!-margin-top-0">${subarea.displayName()}</p>
        </#list>
      </@fdsSummaryList.summaryListRowNoAction>
      <@fdsSummaryList.summaryListRowNoAction keyText="Is this nomination for future wells drilled in the selected subareas?">
        <#if nominatedBlockSubareaDetailView.validForFutureWellsInSubarea?has_content>
          ${nominatedBlockSubareaDetailView.validForFutureWellsInSubarea?then('Yes', 'No')}
        </#if>
      </@fdsSummaryList.summaryListRowNoAction>
      <@fdsSummaryList.summaryListRowNoAction keyText="Is this nomination for all well phases?">
        <#if nominatedBlockSubareaDetailView.forAllWellPhases?has_content>
          ${nominatedBlockSubareaDetailView.forAllWellPhases?then('Yes', 'No')}
        </#if>
      </@fdsSummaryList.summaryListRowNoAction>
      <#if nominatedBlockSubareaDetailView.forAllWellPhases?has_content && !nominatedBlockSubareaDetailView.forAllWellPhases>
        <@fdsSummaryList.summaryListRowNoAction keyText="Which well phases is this nomination for?">
          <#list nominatedBlockSubareaDetailView.wellPhases as phase>
            <div>${phase.screenDisplayText}</div>
          </#list>
        </@fdsSummaryList.summaryListRowNoAction>
      </#if>
    </@fdsSummaryList.summaryList>
  </@fdsSummaryList.summaryListWrapper>
</#macro>