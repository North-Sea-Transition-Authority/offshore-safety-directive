<#include '../../layout/layout.ftl'>
<#import '_caseProcessingHeader.ftl' as _caseProcessingHeader/>

<#-- @ftlvariable name="headerInformation" type="uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingHeader" -->
<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->

<#assign pageTitle = headerInformation.nominationReference().reference() />

<#assign heading>
  ${pageTitle}
  <span class="govuk-caption-xl">
      ${headerInformation.applicantOrganisationUnitView().name().name()}
  </span>
</#assign>

<@defaultPage
    htmlTitle=pageTitle
    pageHeading=heading
    pageSize=PageSize.FULL_WIDTH
    backLinkUrl=springUrl(backLinkUrl!"")
    breadcrumbsList=breadcrumbsList
>

    <@_caseProcessingHeader.caseProcessingHeader headerInformation/>

</@defaultPage>