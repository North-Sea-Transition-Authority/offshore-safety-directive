<#include '../../layout/layout.ftl'>

<#macro appointmentDetailSummary
    appointedOperator
    responsibleFromDate
    phases
    createdBy
    responsibleToDate=""
>
    <@fdsSummaryList.summaryListWrapper
        summaryListId="review-appointment-details"
    >
        <@fdsSummaryList.summaryList>
            <@fdsSummaryList.summaryListRowNoAction keyText="Appointed operator">
                ${appointedOperator}
            </@fdsSummaryList.summaryListRowNoAction>
            <@fdsSummaryList.summaryListRowNoAction keyText="Responsible from date">
                ${responsibleFromDate}
            </@fdsSummaryList.summaryListRowNoAction>
            <#if responsibleToDate?has_content>
                <@fdsSummaryList.summaryListRowNoAction keyText="Responsible to date">
                    ${responsibleToDate}
                </@fdsSummaryList.summaryListRowNoAction>
            </#if>
            <@fdsSummaryList.summaryListRowNoAction keyText="Phases">
                <#if phases?has_content>
                    <ol class="govuk-list">
                        <#list phases as phase>
                            <div>${phase.value()}</div>
                        </#list>
                    </ol>
                </#if>
            </@fdsSummaryList.summaryListRowNoAction>
            <@fdsSummaryList.summaryListRowNoAction keyText="Created by">
                ${createdBy}
            </@fdsSummaryList.summaryListRowNoAction>
        </@fdsSummaryList.summaryList>
    </@fdsSummaryList.summaryListWrapper>
</#macro>