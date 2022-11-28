<#import '_applicantDetailsSummary.ftl' as _applicantDetailsSummary/>

<#macro nominationSummary summaryView>
    <@_applicantDetailsSummary.applicantDetailsSummary applicantDetailSummaryView=summaryView.applicantDetailSummaryView()/>
</#macro>