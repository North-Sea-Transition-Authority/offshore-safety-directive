<#import "../../../fds/components/summaryList/summaryList.ftl" as fdsSummaryList/>

<#macro activePortalReferences activePortalReferencesView>
    <@fdsSummaryList.summaryListCard
      summaryListId="active-portal-references"
      headingText="Related applications"
    >

        <@fdsSummaryList.summaryListRowNoAction keyText="Linked PEARS applications">
            <pre class="govuk-body">${(activePortalReferencesView.pearsReferences().references())!"Not provided"}</pre>
        </@fdsSummaryList.summaryListRowNoAction>

        <@fdsSummaryList.summaryListRowNoAction keyText="Linked WONS applications">
            <pre class="govuk-body">${(activePortalReferencesView.wonsReferences().references())!"Not provided"}</pre>
        </@fdsSummaryList.summaryListRowNoAction>

    </@fdsSummaryList.summaryListCard>
</#macro>