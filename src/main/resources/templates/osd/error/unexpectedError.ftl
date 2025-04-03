<#include '../layout/layout.ftl'/>
<#import '../technicalSupport/_technicalSupport.ftl' as technicalSupportContact>

<#-- @ftlvariable name="technicalSupport" type="uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties" -->
<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->
<#-- @ftlvariable name="errorReference" type="String" -->
<#-- @ftlvariable name="canShowStackTrace" type="Boolean" -->
<#-- @ftlvariable name="stackTrace" type="String" -->

<@defaultErrorPage pageHeading="Sorry, there is a problem with the service">
  <p class="govuk-body">
    Try again later.
  </p>
  <p class="govuk-body">
    If you continue to experience this problem, contact the ${technicalSupport.name()} using the
    details below. Be sure to include the error reference below in any correspondence,
    along with a description of what you were trying to do and, if relevant, the
    reference number for the activity or information you were working on.
  </p>
  <#if errorReference?has_content>
    <p class="govuk-body">
      Error reference:
      <span class="govuk-!-font-weight-bold">
        ${errorReference}
      </span>
    </p>
  </#if>
  <@technicalSupportContact.technicalSupportContactDetails
    technicalSupportProperties=technicalSupport
    technicalSupportEmailSubject="WIOS error: ${errorReference}"
  />
  <#if canShowStackTrace && stackTrace?has_content>
    <h2 class="govuk-heading-l">Stack trace</h2>
    <pre class="govuk-body">
      <code>${stackTrace}</code>
    </pre>
  </#if>
</@defaultErrorPage>
