<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="operatorSearchUrl" type="String" -->
<#-- @ftlvariable name="installationSearchUrl" type="String" -->
<#-- @ftlvariable name="wellSearchUrl" type="String" -->
<#-- @ftlvariable name="forwardAreaApprovalSearchUrl" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->

<#assign pageTitle = "Well and installation operator appointments" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=[]
  pageSize=PageSize.TWO_THIRDS_COLUMN
  showNavigationItems=(loggedInUser?has_content)
  allowSearchEngineIndexing=false
  phaseBanner=(loggedInUser?has_content)
>
  <ul class="govuk-list">
    <li class="govuk-list__item">
      <@_systemOrRecordLandingLink
        linkText="View appointments by operator"
        linkUrl=springUrl(operatorSearchUrl)
        ariaDescribedByHintId="operator-appointments-hint"
      />
      <div class="govuk-hint" id="operator-appointments-hint">
        Includes appointments for wells, installations and forward area approvals to cover
        future wells drilled
      </div>
    </li>
    <li class="govuk-list__item">
      <@_systemOrRecordLandingLink
        linkText="View appointments for installations"
        linkUrl=springUrl(installationSearchUrl)
        ariaDescribedByHintId="installation-appointments-hint"
      />
      <div class="govuk-hint" id="installation-appointments-hint">
        Includes all installations that may require an installation operator appointment even where one is not in place
      </div>
    </li>
    <li class="govuk-list__item">
      <@_systemOrRecordLandingLink
        linkText="View appointments for wells"
        linkUrl=springUrl(wellSearchUrl)
        ariaDescribedByHintId="well-appointments-hint"
      />
      <div class="govuk-hint" id="well-appointments-hint">
        Includes all wells that may require a well operator appointment even where one is not in place
      </div>
    </li>
    <li class="govuk-list__item">
      <@_systemOrRecordLandingLink
        linkText="View appointments for forward area approvals"
        linkUrl=springUrl(forwardAreaApprovalSearchUrl)
        ariaDescribedByHintId="forward-area-approvals-appointments-hint"
      />
      <div class="govuk-hint" id="forward-area-approvals-appointments-hint">
        Includes all subareas on extant licences showing if they have an appointment to cover
        future wells drilled within the subarea
      </div>
    </li>
  </ul>
</@defaultPage>

<#macro _systemOrRecordLandingLink linkText, linkUrl, ariaDescribedByHintId>
  <@fdsAction.link
    linkUrl=linkUrl
    linkText=linkText
    linkClass="govuk-link govuk-link--no-visited-state govuk-!-font-size-24 govuk-!-font-weight-bold"
    ariaDescribedBy=ariaDescribedByHintId
  />
</#macro>