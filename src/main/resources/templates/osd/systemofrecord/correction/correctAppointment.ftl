<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="assetName" type="String" -->
<#-- @ftlvariable name="assetTypeDisplayName" type="String" -->
<#-- @ftlvariable name="preselectedOperator" type="java.util.Map<Integer, String>" -->
<#-- @ftlvariable name="submitUrl" type="String" -->

<@defaultPage
    pageHeading="Update appointment"
    pageHeadingCaption="${assetName} - ${assetTypeDisplayName}"
    errorItems=[]
    pageSize=PageSize.TWO_THIRDS_COLUMN
    backLinkWithBrowserBack=true
    showNavigationItems=(loggedInUser?has_content)
    allowSearchEngineIndexing=false
>

    <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
        <@fdsSearchSelector.searchSelectorRest
            path="form.nominatedOperatorId"
            preselectedItems=preselectedOperator
            labelText="Who is the operator?"
            selectorMinInputLength=2
            restUrl=springUrl(portalOrganisationsRestUrl)
        />

        <@fdsAction.submitButtons
            primaryButtonText="Submit"
            secondaryLinkText="Cancel"
            linkSecondaryAction=true
            linkSecondaryActionUrl="javascript:history.back()"
        />
    </@fdsForm.htmlForm>

</@defaultPage>