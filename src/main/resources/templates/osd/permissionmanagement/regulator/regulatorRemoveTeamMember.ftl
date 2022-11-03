<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="htmlTitle" type="java.lang.String" -->
<#-- @ftlvariable name="backLinkUrl" type="java.lang.String" -->
<#-- @ftlvariable name="teamName" type="java.lang.String" -->
<#-- @ftlvariable name="teamMember" type="java.lang.String" -->

<#assign pageTitle = "Are you sure you want to remove ${teamMember.getDisplayName()} from the ${teamName}?" />

<@defaultPage
    htmlTitle=htmlTitle
    pageHeading=pageTitle
    errorItems=[]
    pageSize=PageSize.TWO_THIRDS_COLUMN
    backLinkUrl=springUrl(backLinkUrl)
>

    <@fdsSummaryList.summaryList>
        <@fdsSummaryList.summaryListRowNoAction keyText="Email address">${teamMember.contactEmail()}</@fdsSummaryList.summaryListRowNoAction>
        <#if teamMember.contactNumber()?has_content>
            <@fdsSummaryList.summaryListRowNoAction keyText="Telephone number">${teamMember.contactNumber()}</@fdsSummaryList.summaryListRowNoAction>
        </#if>
        <@fdsSummaryList.summaryListRowNoAction keyText="Roles">
          <ul class="govuk-list govuk-!-margin-bottom-0">
              <#list teamMember.teamRoles() as role>
                <li>${role.displayText}</li>
              </#list>
          </ul>
        </@fdsSummaryList.summaryListRowNoAction>
    </@fdsSummaryList.summaryList>

    <@fdsForm.htmlForm>
        <@fdsAction.buttonGroup>
            <@fdsAction.button buttonText="Remove" buttonClass="govuk-button govuk-button--warning"/>
            <@fdsAction.link linkText="Cancel" linkUrl=springUrl(backLinkUrl) linkClass="fds-link-button"/>
        </@fdsAction.buttonGroup>
    </@fdsForm.htmlForm>

</@defaultPage>
