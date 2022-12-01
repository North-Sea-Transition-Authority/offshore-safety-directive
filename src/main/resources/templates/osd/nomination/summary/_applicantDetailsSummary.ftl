<#import "../../../fds/components/summaryList/summaryList.ftl" as fdsSummaryList/>

<#macro applicantDetailsSummary applicantDetailSummaryView>
  <@fdsSummaryList.summaryListCard
    summaryListId=applicantDetailSummaryView.summarySectionDetails().summarySectionId().id()
    headingText=applicantDetailSummaryView.summarySectionDetails().summarySectionName().name()
    summaryListErrorMessage=(applicantDetailSummaryView.summarySectionError().errorMessage())!""
  >

      <@fdsSummaryList.summaryListRowNoAction keyText="Applicant organisation">
        ${(applicantDetailSummaryView.applicantOrganisationUnitView().name().name())!""}
      </@fdsSummaryList.summaryListRowNoAction>

      <@fdsSummaryList.summaryListRowNoAction keyText="Applicant reference">
          ${(applicantDetailSummaryView.applicantReference().reference())!""}
      </@fdsSummaryList.summaryListRowNoAction>

  </@fdsSummaryList.summaryListCard>
</#macro>