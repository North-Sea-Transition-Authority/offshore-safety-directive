<#import '_applicantDetailsSummary.ftl' as _applicantDetailsSummary/>
<#import '_nomineeDetailsSummary.ftl' as _nomineeDetailsSummary/>
<#import '_relatedInformationSummary.ftl' as _relatedInformationSummary/>
<#import '_installationSummary.ftl' as _installationSummary/>
<#import '_wellSummary.ftl' as _wellSummary/>

<#macro nominationSummary summaryView>
  <@_applicantDetailsSummary.applicantDetailsSummary
    applicantDetailSummaryView=summaryView.applicantDetailSummaryView()
  />
  <@_nomineeDetailsSummary.nomineeDetailsSummary
    nomineeDetailSummaryView=summaryView.nomineeDetailSummaryView()
  />
  <@_relatedInformationSummary.relatedInformationSummary
    relatedInformationSummaryView=summaryView.relatedInformationSummaryView()
  />
  <@_installationSummary.installationSummary
    installationSummaryView=summaryView.installationSummaryView()
  />
  <@_wellSummary.wellSummary
    wellSummaryView=summaryView.wellSummaryView()
  />
</#macro>