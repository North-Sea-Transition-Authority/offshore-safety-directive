<#import "../../../fds/components/summaryList/summaryList.ftl" as fdsSummaryList/>

<#macro activePortalReferences activePortalReferencesView>
    <@fdsSummaryList.summaryListCard
      summaryListId="active-portal-references"
      headingText="Related applications"
    >

        <@fdsSummaryList.summaryListRowNoAction keyText="Linked PEARS applications">
            <p class="govuk-body govuk-body__preserve-whitespace">${(activePortalReferencesView.pearsReferences().references())!"Not provided"}</p>
        </@fdsSummaryList.summaryListRowNoAction>

        <@fdsSummaryList.summaryListRowNoAction keyText="Linked WONS applications">
            <p class="govuk-body govuk-body__preserve-whitespace">${(activePortalReferencesView.wonsReferences().references())!"Not provided"}</p>
        </@fdsSummaryList.summaryListRowNoAction>

    </@fdsSummaryList.summaryListCard>
</#macro>