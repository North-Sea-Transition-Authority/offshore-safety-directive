<#import "../../../fds/components/summaryList/summaryList.ftl" as fdsSummaryList/>
<#import '../../files/fileSummary.ftl' as fileSummary>

<#macro nomineeDetailsSummary nomineeDetailSummaryView submissionSummaryView>
  <@fdsSummaryList.summaryListCard
    summaryListId=nomineeDetailSummaryView.summarySectionDetails().summarySectionId().id()
    headingText=nomineeDetailSummaryView.summarySectionDetails().summarySectionName().name()
    summaryListErrorMessage=(nomineeDetailSummaryView.summarySectionError().errorMessage())!""
  >

      <@fdsSummaryList.summaryListRowNoAction keyText="Nominated operator">
          ${(nomineeDetailSummaryView.nominatedOrganisationUnitView().displayName())!""}
      </@fdsSummaryList.summaryListRowNoAction>

      <@fdsSummaryList.summaryListRowNoAction keyText="Reason for the nomination">
          <p class="govuk-body govuk-!-margin-top-0 -body__preserve-whitespace">
              ${(nomineeDetailSummaryView.nominationReason().reason())!""}
          </p>
      </@fdsSummaryList.summaryListRowNoAction>

      <@fdsSummaryList.summaryListRowNoAction keyText="Planned appointment date">
          ${(nomineeDetailSummaryView.appointmentPlannedStartDate().plannedStartDateString())!""}
      </@fdsSummaryList.summaryListRowNoAction>

      <#if submissionSummaryView?has_content && submissionSummaryView.fastTrackReason()?has_content>
        <@fdsSummaryList.summaryListRowNoAction keyText="Reason that this submission was planned with less than 3 months">
            <p class="govuk-body govuk-body__preserve-whitespace">${submissionSummaryView.fastTrackReason()}</p>
        </@fdsSummaryList.summaryListRowNoAction>
      </#if>

      <@fdsSummaryList.summaryListRowNoAction keyText="Accepted all declarations">
          ${((nomineeDetailSummaryView.nomineeDetailConditionsAccepted().accepted())!false)?then("Yes", "")}
      </@fdsSummaryList.summaryListRowNoAction>

      <@fdsSummaryList.summaryListRowNoAction keyText="Appendix C and associated documents">
          <#if nomineeDetailSummaryView.appendixDocuments()?has_content>
              <@fileSummary.fileSummary nomineeDetailSummaryView.appendixDocuments().documents()/>
          </#if>
      </@fdsSummaryList.summaryListRowNoAction>

  </@fdsSummaryList.summaryListCard>
</#macro>