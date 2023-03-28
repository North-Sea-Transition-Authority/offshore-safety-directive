<#include '../../layout/layout.ftl'>
<#import '../summary/nominationSummary.ftl' as nominationSummary>

<#assign pageTitle = "Are you sure you want to delete this draft nomination?" />

<@defaultPage
    htmlTitle=pageTitle
    pageHeading=pageTitle
    pageSize=PageSize.TWO_THIRDS_COLUMN
    backLinkUrl=springUrl(cancelUrl!"")
>

    <@fdsDetails.summaryDetails summaryTitle="View the draft nomination to be deleted">
        <@nominationSummary.nominationSummary summaryView=nominationSummaryView/>
    </@fdsDetails.summaryDetails>

    <@fdsForm.htmlForm actionUrl=springUrl(deleteUrl)>
        <@fdsAction.submitButtons
            primaryButtonText="Delete nomination"
            primaryButtonClass="govuk-button govuk-button--warning"
            linkSecondaryAction=true
            secondaryLinkText="Cancel"
            linkSecondaryActionUrl=springUrl(cancelUrl)
        />
    </@fdsForm.htmlForm>

</@defaultPage>