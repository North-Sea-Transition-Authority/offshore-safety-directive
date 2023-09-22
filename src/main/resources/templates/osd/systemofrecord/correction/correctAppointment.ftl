<#include '../../layout/layout.ftl'>
<#import 'correctionHistoryTimeline.ftl' as correctionHistoryTimeline>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="assetName" type="String" -->
<#-- @ftlvariable name="assetTypeDisplayName" type="String" -->
<#-- @ftlvariable name="preselectedOperator" type="java.util.Map<Integer, String>" -->
<#-- @ftlvariable name="submitUrl" type="String" -->
<#-- @ftlvariable name="phaseSelectionHint" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->
<#-- @ftlvariable name="correctionHistoryViews" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionHistoryView>" -->
<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="showCorrectionHistory" type="Boolean" -->

<@defaultPage
    pageHeading=pageTitle
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
            path="form.appointmentType"
            labelText="Select the type of appointment"
            hiddenContent=true
        >

            <#assign isFirstAppointmentType = true/>
            <#list appointmentTypes as appointmentTypeName, appointmentTypeText>
                <@fdsRadio.radioItem
                    path="form.appointmentType"
                    itemMap={appointmentTypeName: appointmentTypeText}
                    isFirstItem=isFirstAppointmentType
                >
                    <#if appointmentTypeName == "DEEMED">
                        <@fdsInsetText.insetText>
                          The start date will automatically be set to the 19 July 2015
                        </@fdsInsetText.insetText>
                    <#elseif appointmentTypeName == "OFFLINE_NOMINATION">
                        <@fdsTextInput.textInput
                            path="form.offlineNominationReference.inputValue"
                            labelText="Nomination reference"
                            optionalLabel=true
                        />
                        <@fdsDateInput.dateInput
                            dayPath="form.offlineAppointmentStartDate.dayInput.inputValue"
                            monthPath="form.offlineAppointmentStartDate.monthInput.inputValue"
                            yearPath="form.offlineAppointmentStartDate.yearInput.inputValue"
                            labelText="Start date"
                            formId="offlineStartDate"
                            nestingPath="form.appointmentType"
                        />
                    <#elseif appointmentTypeName == "ONLINE_NOMINATION">
                        <@fdsSearchSelector.searchSelectorRest
                            path="form.onlineNominationReference"
                            restUrl=springUrl(nominationReferenceRestUrl)
                            labelText="${serviceBranding.mnemonic()} nomination reference"
                            preselectedItems=preselectedNominationReference!{}
                        />
                        <@fdsDateInput.dateInput
                            dayPath="form.onlineAppointmentStartDate.dayInput.inputValue"
                            monthPath="form.onlineAppointmentStartDate.monthInput.inputValue"
                            yearPath="form.onlineAppointmentStartDate.yearInput.inputValue"
                            labelText="Start date"
                            formId="onlineStartDate"
                            nestingPath="form.appointmentType"
                        />
                    </#if>
                </@fdsRadio.radioItem>
                <#assign isFirstAppointmentType = false/>
            </#list>
        </@fdsRadio.radioGroup>

        <@fdsRadio.radioGroup
            path="form.hasEndDate"
            labelText="Does this appointment have an end date?"
            hiddenContent=true
        >
            <@fdsRadio.radioYes path="form.hasEndDate">
              <@fdsDateInput.dateInput
                dayPath="form.endDate.dayInput.inputValue"
                monthPath="form.endDate.monthInput.inputValue"
                yearPath="form.endDate.yearInput.inputValue"
                labelText="End date"
                formId="endDate"
                nestingPath="form.hasEndDate"
              />
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path="form.hasEndDate"/>
        </@fdsRadio.radioGroup>

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
                    fieldsetHeadingText="Select all ${assetTypeSentenceCaseDisplayName} activity phases for this appointment"
                    hintText=phaseSelectionHint!""
                />
            </@fdsRadio.radioNo>
        </@fdsRadio.radioGroup>

        <@fdsTextarea.textarea
            path="form.reason.inputValue"
            labelText="What is the reason for the correction?"
            hintText="The explanation provided should be sufficient to understand why the correction was carried out"
        />

        <@fdsDetails.summaryDetails summaryTitle="What information should I provide for the correction reason?">
            <p class="govuk-body">
                You should include information on:
            </p>
            <ul class="govuk-list govuk-list--bullet">
                <li>Who requested the information be corrected</li>
                <li>The evidence provided for the correction</li>
                <li>Reference any relevant documents attached to the appointment</li>
                <li>If information provided does not align with an associated nomination or forward approval clearly state why</li>
                <li>Include information from the current correction reason, if one exists, that is still relevant to the information shown on the appointment</li>
            </ul>
        </@fdsDetails.summaryDetails>

        <#if showCorrectionHistory!false>
            <@fdsDetails.summaryDetails summaryTitle="View correction history">
                <@correctionHistoryTimeline.correctionHistory correctionHistoryViews=correctionHistoryViews![] />
            </@fdsDetails.summaryDetails>
        </#if>

        <@fdsAction.submitButtons
            primaryButtonText="Submit"
            secondaryLinkText="Cancel"
            linkSecondaryAction=true
            linkSecondaryActionUrl=springUrl(cancelUrl)
        />
    </@fdsForm.htmlForm>

</@defaultPage>