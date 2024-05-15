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
          <#if subarea.isExtant()>
            <p class="govuk-!-margin-top-0">${subarea.displayName()}</p>
          <#else>
            <p class="govuk-!-margin-top-0">${subarea.subareaName().value()}</p>
            <strong class="govuk-tag govuk-tag--blue">No longer exists</strong>
          </#if>
        </#list>
      </@fdsSummaryList.summaryListRowNoAction>
      <@fdsSummaryList.summaryListRowNoAction
        keyText="Will this nomination cover future wells that may be drilled in the selected subareas?"
      >
        <#if nominatedBlockSubareaDetailView.validForFutureWellsInSubarea?has_content>
          ${nominatedBlockSubareaDetailView.validForFutureWellsInSubarea?then('Yes', 'No')}
        </#if>
      </@fdsSummaryList.summaryListRowNoAction>
      <@fdsSummaryList.summaryListRowNoAction keyText="Is this nomination for all well activity phases?">
        <#if nominatedBlockSubareaDetailView.forAllWellPhases?has_content>
          ${nominatedBlockSubareaDetailView.forAllWellPhases?then('Yes', 'No')}
        </#if>
      </@fdsSummaryList.summaryListRowNoAction>
      <#if nominatedBlockSubareaDetailView.forAllWellPhases?has_content && !nominatedBlockSubareaDetailView.forAllWellPhases>
        <@fdsSummaryList.summaryListRowNoAction keyText="Which well activity phases is this nomination for?">
          <#list nominatedBlockSubareaDetailView.wellPhases as phase>
            <div>${phase.screenDisplayText}</div>
          </#list>
        </@fdsSummaryList.summaryListRowNoAction>
      </#if>
    </@fdsSummaryList.summaryList>
  </@fdsSummaryList.summaryListWrapper>
</#macro>