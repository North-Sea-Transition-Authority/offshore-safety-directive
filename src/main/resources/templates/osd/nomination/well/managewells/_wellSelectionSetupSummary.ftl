<#include '../../../layout/layout.ftl'>

<#-- @ftlvariable name="wellSelectionSetupView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupView" -->

<#macro wellSelectionSetupSummary wellSelectionSetupView changeUrl="">
  <@fdsSummaryList.summaryListWrapper
    headingText="Well nomination type"
    headingClass="govuk-heading-m"
    summaryListId="summary-list-well-nomination-type"
    showSummaryListActions=changeUrl?has_content
    changeLinkUrl=springUrl(changeUrl)
  >
    <@fdsSummaryList.summaryList>
      <@fdsSummaryList.summaryListRowNoAction keyText="Is this nomination in relation to well operatorship?">
        ${(wellSelectionSetupView.wellSelectionType.screenDisplayText)!""}
      </@fdsSummaryList.summaryListRowNoAction>
    </@fdsSummaryList.summaryList>
  </@fdsSummaryList.summaryListWrapper>
</#macro>