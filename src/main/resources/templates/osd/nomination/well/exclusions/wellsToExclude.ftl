<#include '../../../layout/layout.ftl'>
<#import '_wellExclusionList.ftl' as exclusions>
<#import '../_listWellbores.ftl' as _listWellbores>
<#import '../_wonsContactGuidance.ftl' as wonsContactGuidance>

<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties" -->

<#assign pageTitle = "Are any wells to be excluded from this nomination?" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=""
  errorItems=errorList
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsForm.htmlForm actionUrl=springUrl(actionUrl)>

    <@fdsRadio.radioGroup
      path="form.hasWellsToExclude"
      labelText=pageTitle
      fieldsetHeadingSize="h1"
      fieldsetHeadingClass="govuk-fieldset__legend--l"
      hiddenContent=true
    >
      <@fdsRadio.radioYes path="form.hasWellsToExclude">
        <#if wellbores?has_content>
          <@exclusions._wellsExcludeList
            wellbores=wellbores
            excludedWellsFormPath="form.excludedWells"
          />
        <#else>
          <p class="govuk-body">
            None of the subareas included in this nomination contain any wells.
            If you know that wells exist in these subareas then contact
            <@mailTo.mailToLink
              linkText=customerBranding.mnemonic()
              mailToEmailAddress=customerBranding.businessEmailAddress()
            />.
          </p>
        </#if>
      </@fdsRadio.radioYes>
      <@fdsRadio.radioNo path="form.hasWellsToExclude" />
    </@fdsRadio.radioGroup>

    <@fdsDetails.summaryDetails summaryTitle="What wells are part of this nomination?">
      <p class="govuk-body">
        A well is included in the list below if its origin or total depth (TD) location is within the spatial
        area for a subarea on the nomination.
      </p>
      <p class="govuk-body">
        Examples of where you may choose to exclude a well include those
        with Abandoned Phase 3 mechanical status or those drilled under a previous licence according to WONS.
      </p>
      <@_listWellbores.listWellbores wellbores/>
      <@wonsContactGuidance.wonsContactGuidance/>
    </@fdsDetails.summaryDetails>

    <@fdsAction.button buttonText="Save and continue"/>

  </@fdsForm.htmlForm>
</@defaultPage>