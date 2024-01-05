<#import "../../../fds/components/summaryList/summaryList.ftl" as fdsSummaryList/>

<#macro applicantDetailsSummary applicantDetailSummaryView submissionSummaryView>
  <@fdsSummaryList.summaryListCard
    summaryListId=applicantDetailSummaryView.summarySectionDetails().summarySectionId().id()
    headingText=applicantDetailSummaryView.summarySectionDetails().summarySectionName().name()
    summaryListErrorMessage=(applicantDetailSummaryView.summarySectionError().errorMessage())!""
  >

      <@fdsSummaryList.summaryListRowNoAction keyText="Applicant organisation">
          ${(applicantDetailSummaryView.applicantOrganisationUnitView().displayName())!""}
      </@fdsSummaryList.summaryListRowNoAction>

      <@fdsSummaryList.summaryListRowNoAction keyText="Applicant reference">
          ${(applicantDetailSummaryView.applicantReference().reference())!""}
      </@fdsSummaryList.summaryListRowNoAction>

      <#if submissionSummaryView?has_content>
          <@fdsSummaryList.summaryListRowNoAction keyText="Has licensee authority to nominate">
              ${submissionSummaryView.confirmedAuthority()?then("Yes", "No")}
          </@fdsSummaryList.summaryListRowNoAction>
      </#if>

  </@fdsSummaryList.summaryListCard>
</#macro>