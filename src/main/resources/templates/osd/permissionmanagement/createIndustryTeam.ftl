<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#include '../layout/layout.ftl'>

<@defaultPage
    htmlTitle=pageTitle
    pageHeading=""
    errorItems=errorList
    pageSize=PageSize.TWO_THIRDS_COLUMN
>

    <@fdsForm.htmlForm>
        <@fdsSearchSelector.searchSelectorRest
            path="form.orgGroupId"
            restUrl=springUrl(orgGroupRestUrl)
            labelText=pageTitle
            pageHeading=true
        />

        <@fdsDetails.summaryDetails summaryTitle="The organisation I want to create a team for is not listed">
            If the organisation you want to create a team for is not shown in the list
            then you must contact the person responsible for managing organisations on the
            U.K Energy Portal.
        </@fdsDetails.summaryDetails>

        <@fdsAction.button buttonText="Create team"/>
    </@fdsForm.htmlForm>

</@defaultPage>