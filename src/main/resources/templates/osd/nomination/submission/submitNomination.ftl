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

      <@nominationSummary.nominationSummary summaryView=summaryView />

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