<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="isSubmittable" type="Boolean" -->

<#assign pageHeading = "Check your answers before submitting your nomination" />

<@defaultPageWithSubNavigation
  pageHeading=pageHeading
>
    <@fdsLeftSubNavPageTemplateSubNav smallSubnav=true>
      <@fdsSubNavigation.subNavigation>
        <@fdsSubNavigation.subNavigationSection>
        </@fdsSubNavigation.subNavigationSection>
      </@fdsSubNavigation.subNavigation>
    </@fdsLeftSubNavPageTemplateSubNav>
    <@fdsBackToTop.backToTop/>
    <@fdsLeftSubNavPageTemplateContent
      pageHeading=pageHeading
      singleErrorMessage=isSubmittable?then("", "You cannot submit your nomination until all sections shown on the task list are completed")
    >
      <@fdsForm.htmlForm
        actionUrl=springUrl(actionUrl)
      >
        <#if isSubmittable>
          <@fdsAction.submitButtons
            linkSecondaryAction=true
            linkSecondaryActionUrl=springUrl(backLinkUrl)
            primaryButtonText="Submit"
            secondaryLinkText="Back to task list"
          />
        <#else>
          <p class="govuk-body">
            <@fdsAction.link linkText="Back to task list" linkUrl=springUrl(backLinkUrl)/>
          </p>
        </#if>
      </@fdsForm.htmlForm>
    </@fdsLeftSubNavPageTemplateContent>
</@defaultPageWithSubNavigation>