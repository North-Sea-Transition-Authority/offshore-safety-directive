<#import '../../../../fds/layout.ftl' as fdsLayout>
<#import '../../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList>

<#macro excludedWellSummary excludedWellView changeUrl="">
  <@fdsSummaryList.summaryListWrapper
    headingText="Excluded wells"
    headingClass="govuk-heading-m"
    summaryListId="summary-list-excluded-wells"
    showSummaryListActions=changeUrl?has_content
    changeLinkUrl=fdsLayout.springUrl(changeUrl)
  >
    <@fdsSummaryList.summaryList>
      <@fdsSummaryList.summaryListRowNoAction keyText="Are any wells to be excluded from this nomination?">
        <#if excludedWellView.hasWellsToExclude()?has_content>
          ${excludedWellView.hasWellsToExclude()?then('Yes', 'No')}
        </#if>
      </@fdsSummaryList.summaryListRowNoAction>
      <#if excludedWellView.hasWellsToExclude()?has_content && excludedWellView.hasWellsToExclude()>
        <@fdsSummaryList.summaryListRowNoAction keyText="Excluded wells">
          <#if excludedWellView.hasWellsToExclude()?has_content>
            <ol class="govuk-list">
              <#list excludedWellView.excludedWells() as excludedWellRegistrationNumber>
                <li class="govuk-list__item">${excludedWellRegistrationNumber.value()}</li>
              </#list>
            </ol>
          </#if>
        </@fdsSummaryList.summaryListRowNoAction>
      </#if>
    </@fdsSummaryList.summaryList>
  </@fdsSummaryList.summaryListWrapper>
</#macro>