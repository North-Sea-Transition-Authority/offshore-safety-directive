<#import '../../../macros/mailTo.ftl' as mailTo>
<#import '../../../../fds/components/details/details.ftl' as fdsDetails>

<#-- @ftlvariable name="wonsEmail" type="String" -->
<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->

<#macro wonsContactGuidance detailsClass="">
    <@fdsDetails.summaryDetails summaryTitle="The licences shown are not the licences I expect" detailsClass=detailsClass>
        <p class="govuk-body">
            Contact the WONS team at <@mailTo.mailToLink mailToEmailAddress=wonsEmail subjectText="WONS licence query from ${serviceBranding.mnemonic()}"/> if you believe WONS data to be in error,
            making reference to ${serviceBranding.mnemonic()} in your email and its subject line
        </p>
    </@fdsDetails.summaryDetails>
</#macro>