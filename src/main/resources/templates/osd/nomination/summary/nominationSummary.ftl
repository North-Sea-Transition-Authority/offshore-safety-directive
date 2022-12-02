<#import '_applicantDetailsSummary.ftl' as _applicantDetailsSummary/>
<#import '_nomineeDetailsSummary.ftl' as _nomineeDetailsSummary/>
<#import '_relatedInformationSummary.ftl' as _relatedInformationSummary/>

<#macro nominationSummary summaryView>
    <@_applicantDetailsSummary.applicantDetailsSummary applicantDetailSummaryView=summaryView.applicantDetailSummaryView()/>
    <@_nomineeDetailsSummary.nomineeDetailsSummary nomineeDetailSummaryView=summaryView.nomineeDetailSummaryView()/>
    <@_relatedInformationSummary.relatedInformationSummary relatedInformationSummaryView=summaryView.relatedInformationSummaryView()/>
</#macro>