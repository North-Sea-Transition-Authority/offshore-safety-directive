<#import "../../../fds/components/summaryList/summaryList.ftl" as fdsSummaryList/>

<#macro nomineeDetailsSummary nomineeDetailSummaryView>
  <@fdsSummaryList.summaryListCard
    summaryListId=nomineeDetailSummaryView.summarySectionDetails().summarySectionId().id()
    headingText=nomineeDetailSummaryView.summarySectionDetails().summarySectionName().name()
    summaryListErrorMessage=(nomineeDetailSummaryView.summarySectionError().errorMessage())!""
  >

      <@fdsSummaryList.summaryListRowNoAction keyText="Nominated organisation">
          ${(nomineeDetailSummaryView.nominatedOrganisationUnitView().name().name())!""}
      </@fdsSummaryList.summaryListRowNoAction>

      <@fdsSummaryList.summaryListRowNoAction keyText="Reason for the nomination">
          ${(nomineeDetailSummaryView.nominationReason().reason())!""}
      </@fdsSummaryList.summaryListRowNoAction>

      <@fdsSummaryList.summaryListRowNoAction keyText="Planned appointment date">
          ${(nomineeDetailSummaryView.appointmentPlannedStartDate().plannedStartDate())!""}
      </@fdsSummaryList.summaryListRowNoAction>

      <@fdsSummaryList.summaryListRowNoAction keyText="Accepted all declarations">
          ${((nomineeDetailSummaryView.nomineeDetailConditionsAccepted().accepted())!false)?then("Yes", "")}
      </@fdsSummaryList.summaryListRowNoAction>

  </@fdsSummaryList.summaryListCard>
</#macro>