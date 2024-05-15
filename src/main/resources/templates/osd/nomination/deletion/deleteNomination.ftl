<#include '../../layout/layout.ftl'>
<#import '../summary/nominationSummary.ftl' as nominationSummary>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="deleteButtonPrompt" type="String" -->
<#-- @ftlvariable name="deleteSummaryLinkText" type="String" -->

<@defaultPage
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=springUrl(cancelUrl!"")
>

  <@fdsDetails.summaryDetails summaryTitle=deleteSummaryLinkText>
    <@nominationSummary.nominationSummary summaryView=nominationSummaryView/>
  </@fdsDetails.summaryDetails>

  <@fdsForm.htmlForm actionUrl=springUrl(deleteUrl)>
    <@fdsAction.submitButtons
      primaryButtonText=deleteButtonPrompt
      primaryButtonClass="govuk-button govuk-button--warning"
      linkSecondaryAction=true
      secondaryLinkText="Cancel"
      linkSecondaryActionUrl=springUrl(cancelUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>