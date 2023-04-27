<#import '../../../fds/components/details/details.ftl' as fdsDetails/>
<#import '../../../fds/components/button/button.ftl' as fdsAction/>

<#-- @ftlvariable name="accidentRegulatorBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties" -->

<#macro wellActivityPhaseGuidance accidentRegulatorBranding>
  <@fdsDetails.summaryDetails summaryTitle="What are the well activity phases?">
    <p class="govuk-body">
      Well activity phases include exploration and appraisal, development and decommissioning, as defined in the
      <@fdsAction.link
        linkText="${accidentRegulatorBranding.name()}'s Appendix C guidance"
        linkUrl=accidentRegulatorBranding.consultationGuidanceUrl()
        openInNewTab=true
      />
    </p>
  </@fdsDetails.summaryDetails>
</#macro>