<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.hydrocarbonmaturation.fds.ErrorItem>" -->
<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->
<#-- @ftlvariable name="serviceRatings" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="showBackLink" type="String" -->
<#-- @ftlvariable name="maxCharacterLength" type="String" -->
<#-- @ftlvariable name="pageName" type="String" -->

<@defaultPage
  htmlTitle=pageName
  pageHeading=pageName
  pageSize=PageSize.TWO_THIRDS_COLUMN
  phaseBanner=false
  backLinkWithBrowserBack=true
  errorItems=errorList>
  <@fdsForm.htmlForm actionUrl=springUrl(actionUrl)>
    <@fdsRadio.radio
      path="form.serviceRating"
      labelText="Overall, how did you feel about using this service?"
      radioItems=serviceRatings/>
    <@fdsTextarea.textarea
      path="form.feedback.inputValue"
      labelText="How could we improve this service?"
      hintText="Do not include any personal or financial information, for example your National Insurance or credit card numbers"
      optionalLabel=true
      maxCharacterLength=maxCharacterLength
      characterCount=true
      rows="10"/>

    <@fdsAction.button buttonText="Send feedback"/>
  </@fdsForm.htmlForm>
</@defaultPage>