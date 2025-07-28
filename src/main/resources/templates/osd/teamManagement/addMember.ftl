<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->

<#assign pageHeading = "What is the UK Energy Portal email address of the user?"/>

<@defaultPage htmlTitle=pageHeading pageHeading="" errorItems=errorList>
  <@fdsForm.htmlForm>
    <@fdsTextInput.textInput
       path="form.emailAddress"
       labelText=pageHeading
       pageHeading=true
    />

    <@fdsDetails.summaryDetails summaryTitle="The user I want to add does not have an account">
      <p class="govuk-body">
        The user must have an account on the UK Energy Portal in order to be added to the team.
      </p>
      <p class="govuk-body">
        A user can register for an account on the UK Energy Portal using the following link:
      </p>
      <p class="govuk-body">
        <@fdsAction.link linkText=registerUrl linkUrl=registerUrl openInNewTab=true/>
      </p>
    </@fdsDetails.summaryDetails>

    <@fdsAction.submitButtons
      primaryButtonText="Continue"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)
    />
</@fdsForm.htmlForm>
</@defaultPage>