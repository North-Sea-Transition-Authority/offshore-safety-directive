<#include '../../layout/layout.ftl'>
<#import '../summary/nominationSummary.ftl' as nominationSummary/>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="isSubmittable" type="Boolean" -->
<#-- @ftlvariable name="summaryView" type="uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryView" -->

<#assign pageHeading = "Check your answers before submitting your nomination" />

<@defaultPageWithSubNavigation pageHeading=pageHeading>

  <@fdsLeftSubNavPageTemplateSubNav smallSubnav=true>
    <@fdsSubNavigation.subNavigation>
      <@fdsSubNavigation.subNavigationSection>
        <@subNavigationLink summarySectionDetail=summaryView.applicantDetailSummaryView().summarySectionDetails() />
        <@subNavigationLink summarySectionDetail=summaryView.nomineeDetailSummaryView().summarySectionDetails() />
        <@subNavigationLink summarySectionDetail=summaryView.relatedInformationSummaryView().summarySectionDetails() />
        <@subNavigationLink summarySectionDetail=summaryView.wellSummaryView().summarySectionDetail />
        <@subNavigationLink summarySectionDetail=summaryView.installationSummaryView().summarySectionDetails() />
      </@fdsSubNavigation.subNavigationSection>
    </@fdsSubNavigation.subNavigation>
  </@fdsLeftSubNavPageTemplateSubNav>

  <@fdsBackToTop.backToTop/>

  <@fdsLeftSubNavPageTemplateContent
    pageHeading=pageHeading
    singleErrorMessage=isSubmittable?then("", "You cannot submit your nomination until all sections shown on the task list are completed")
  >
    <@fdsForm.htmlForm actionUrl=springUrl(actionUrl)>

      <#if reasonForUpdate?has_content>
          <@fdsDetails.summaryDetails summaryTitle="What information have I been asked to update?">
            <p class="govuk-body govuk-body__preserve-whitespace">${reasonForUpdate}</p>
          </@fdsDetails.summaryDetails>
      </#if>

      <@nominationSummary.nominationSummary summaryView=summaryView/>

      <#if hasLicenceBlockSubareas>
        <@fdsWarning.warning>
          The wells included in your nomination may have changed since you last checked as they are based on the
          wells within the selected subareas at the time of submission.
          Ensure that the included wells in the above summary are correct before you submit your nomination.
        </@fdsWarning.warning>
      </#if>

      <#if isSubmittable>
        <@fdsAction.submitButtons
          linkSecondaryAction=true
          linkSecondaryActionUrl=springUrl(backLinkUrl)
          primaryButtonText="Submit"
          secondaryLinkText="Back to task list"
        />
      <#else>
        <p class="govuk-body">
          <@fdsAction.link linkText="Back to task list" linkUrl=springUrl(backLinkUrl)/>
        </p>
      </#if>

    </@fdsForm.htmlForm>
  </@fdsLeftSubNavPageTemplateContent>
</@defaultPageWithSubNavigation>

<#macro subNavigationLink summarySectionDetail>
  <@fdsSubNavigation.subNavigationNestedLink
    linkText=summarySectionDetail.summarySectionName().name()
    linkUrl="#${summarySectionDetail.summarySectionId().id()}"
  />
</#macro>