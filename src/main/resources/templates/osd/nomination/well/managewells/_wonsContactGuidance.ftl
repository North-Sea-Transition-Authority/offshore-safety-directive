<#import '../../../macros/mailTo.ftl' as mailTo>
<#import '../../../../fds/components/details/details.ftl' as fdsDetails>

<#-- @ftlvariable name="wonsEmail" type="String" -->

<#macro wonsContactGuidance detailsClass="">
    <@fdsDetails.summaryDetails summaryTitle="The licences shown are not the licences I expect" detailsClass=detailsClass>
        <p class="govuk-body">
            Contact the WONS team at <@mailTo.mailToLink mailToEmailAddress=wonsEmail/> if you believe WONS data to be in error
        </p>
    </@fdsDetails.summaryDetails>
</#macro>