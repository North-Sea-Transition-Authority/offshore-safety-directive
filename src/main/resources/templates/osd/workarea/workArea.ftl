<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="startNominationUrl" type="String" -->

<#assign pageTitle = "Work area" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
>
  <@fdsAction.link
    linkText="Create nomination"
    linkUrl=springUrl(startNominationUrl)
    linkClass="govuk-button"
  />
</@defaultPage>