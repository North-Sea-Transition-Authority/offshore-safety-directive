<#import '_applicantDetailsSummary.ftl' as _applicantDetailsSummary/>
<#import '_nomineeDetailsSummary.ftl' as _nomineeDetailsSummary/>

<#macro nominationSummary summaryView>
    <@_applicantDetailsSummary.applicantDetailsSummary applicantDetailSummaryView=summaryView.applicantDetailSummaryView()/>
    <@_nomineeDetailsSummary.nomineeDetailsSummary nomineeDetailSummaryView=summaryView.nomineeDetailSummaryView()/>
</#macro>