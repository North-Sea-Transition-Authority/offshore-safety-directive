<#include '../../layout/layout.ftl'>
<#import '../summary/nominationSummary.ftl' as nominationSummary/>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="organisationUrl" type="String" -->
<#-- @ftlvariable name="isSubmittable" type="Boolean" -->
<#-- @ftlvariable name="userCanSubmitNominations" type="Boolean" -->
<#-- @ftlvariable name="summaryView" type="uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryView" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->

<#assign pageHeading = "Check your answers before submitting your nomination" />

<#assign notificationBannerContent>
  <#if organisationUrl??>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Information">
      <@fdsNotificationBanner.notificationBannerContent>
        You must ask a nomination submitter within your <a class="govuk-notification-banner__link" href="${springUrl(organisationUrl)}">organisation</a>
        to submit the nomination on your behalf
      </@fdsNotificationBanner.notificationBannerContent>
    </@fdsNotificationBanner.notificationBannerInfo>
  </#if>
</#assign>

<@defaultPageWithSubNavigation pageHeading=pageHeading >

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
    singleErrorMessage=isSubmittable?then("", "The nomination cannot be submitted until all sections shown on the task list are completed")
    notificationBannerContent=notificationBannerContent
    errorItems=errorList![]
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

      <#if isSubmittable && userCanSubmitNominations>

          <@fdsForm.htmlForm>
            <div class="govuk-form-group">
              <@fdsCheckbox.checkbox path="form.confirmedAuthority" labelText=confirmAuthorityPrompt fieldsetHeadingText="Confirm authority" fieldsetHeadingClass="govuk-visually-hidden"/>
            </div>

            <#if isSubmittable && isFastTrackNomination>
                <@fdsTextarea.textarea
                  path="form.reasonForFastTrack.inputValue"
                  labelText="Provide a robust justification for why the appointment needs to take effect within 3 months of the nomination"
                  hintText="Your justification should explain the exceptional circumstances as to why this is needed. The ${serviceBranding.regulationNameLong()} allow the Licensing Authority 3 months for a decision to be made."
                />
            </#if>

            <@fdsAction.submitButtons
              linkSecondaryAction=true
              linkSecondaryActionUrl=springUrl(backLinkUrl)
              primaryButtonText="Submit"
              secondaryLinkText="Back to task list"
            />
          </@fdsForm.htmlForm>
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