<#include '../../layout/layout.ftl'>
<#import '_timelineItem.ftl' as _timelineItem>
<#import '../termination/_appointmentDetailSummary.ftl' as _appointmentDetailSummary>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="timelineItemView" type="uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineItemView" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkWithBrowserBack=true
  showNavigationItems=(loggedInUser?has_content)
  allowSearchEngineIndexing=false
>

    <#assign modelProperties = timelineItemView.assetTimelineModelProperties().modelProperties/>

    <@_appointmentDetailSummary.appointmentDetailSummary
      appointedOperator=operatorName
      responsibleFromDate=modelProperties["appointmentFromDate"].formattedValue()
      phases=displayPhases
      createdBy=modelProperties["createdByReference"]
    />

    <#assign warningClass>
      <#if portalAssetType != "SUBAREA">${"govuk-!-margin-bottom-0"}</#if>
    </#assign>

    <@fdsWarning.warning>
      <p class="govuk-body ${warningClass}">
        Removing this appointment will result in no operator being appointed for ${assetName} during
        the appointment period.
      </p>
      <#if portalAssetType == "SUBAREA">
        <p class="govuk-body govuk-!-margin-bottom-0">
          Well appointments created by this forward area approval will remain in place. They will no longer be associated
          with the removed forward area approval.
        </p>
      </#if>
    </@fdsWarning.warning>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons
          primaryButtonText="Remove appointment"
          primaryButtonClass="govuk-button govuk-button--warning"
          linkSecondaryAction=true
          secondaryLinkText="Cancel"
          linkSecondaryActionUrl=springUrl(cancelUrl)
        />
    </@fdsForm.htmlForm>

</@defaultPage>