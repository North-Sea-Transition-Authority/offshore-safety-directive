<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="assetName" type="String" -->
<#-- @ftlvariable name="assetTypeDisplayName" type="String" -->
<#-- @ftlvariable name="preselectedOperator" type="java.util.Map<Integer, String>" -->
<#-- @ftlvariable name="submitUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->

<@defaultPage
    pageHeading="Update appointment"
    pageHeadingCaption="${assetName} - ${assetTypeDisplayName}"
    errorItems=errorList
    pageSize=PageSize.TWO_THIRDS_COLUMN
    backLinkWithBrowserBack=true
    showNavigationItems=(loggedInUser?has_content)
    allowSearchEngineIndexing=false
>

    <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
        <@fdsSearchSelector.searchSelectorRest
            path="form.appointedOperatorId"
            preselectedItems=preselectedOperator
            labelText="Who is the appointed operator?"
            selectorMinInputLength=2
            restUrl=springUrl(portalOrganisationsRestUrl)
        />

        <@fdsRadio.radioGroup
            path="form.forAllPhases"
            labelText="Is this appointment for all ${assetTypeSentenceCaseDisplayName} activity phases?"
            hiddenContent=true
        >
            <@fdsRadio.radioYes path="form.forAllPhases"/>
            <@fdsRadio.radioNo path="form.forAllPhases">
                <@fdsCheckbox.checkboxes
                    path="form.phases"
                    nestingPath="form.forAllPhases"
                    checkboxes=phases
                    fieldsetHeadingText="Which ${assetTypeSentenceCaseDisplayName} activity phases is this appointment for?"
                />
            </@fdsRadio.radioNo>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons
            primaryButtonText="Submit"
            secondaryLinkText="Cancel"
            linkSecondaryAction=true
            linkSecondaryActionUrl="javascript:history.back()"
        />
    </@fdsForm.htmlForm>

</@defaultPage>