<#include '../../../layout/layout.ftl'>

<#-- @ftlvariable name="nominatedWellDetailView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailView" -->

<#macro nominatedWellDetailSummary nominatedWellDetailView changeUrl="">
  <@fdsSummaryList.summaryListWrapper
    headingText="Specific well nominations"
    headingClass="govuk-heading-m"
    summaryListId="summary-list-well-nomination-type"
    showSummaryListActions=changeUrl?has_content
    changeLinkUrl=springUrl(changeUrl)
  >
    <@fdsSummaryList.summaryList>
      <@fdsSummaryList.summaryListRowNoAction keyText="Wells">
        <#list nominatedWellDetailView.wells as well>
          <div>${well.name()}</div>
        </#list>
      </@fdsSummaryList.summaryListRowNoAction>
      <@fdsSummaryList.summaryListRowNoAction keyText="Is this nomination for all well phases?">
        <#if nominatedWellDetailView.isNominationForAllWellPhases?has_content>
          ${nominatedWellDetailView.isNominationForAllWellPhases?then('Yes', 'No')}
        </#if>
      </@fdsSummaryList.summaryListRowNoAction>
      <#if nominatedWellDetailView.isNominationForAllWellPhases?has_content && !nominatedWellDetailView.isNominationForAllWellPhases>
        <@fdsSummaryList.summaryListRowNoAction keyText="Which well phases is this nomination for?">
          <#list nominatedWellDetailView.wellPhases as phase>
            <div>${phase.screenDisplayText}</div>
          </#list>
        </@fdsSummaryList.summaryListRowNoAction>
      </#if>
    </@fdsSummaryList.summaryList>
  </@fdsSummaryList.summaryListWrapper>
</#macro>