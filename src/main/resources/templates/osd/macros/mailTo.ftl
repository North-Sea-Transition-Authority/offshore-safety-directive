<#include '../layout/layout.ftl'/>

<#macro mailToLink mailToEmailAddress linkText="" linkClass="govuk-link" linkScreenReaderText="">
  <@fdsAction.link
    linkText=linkText?has_content?then(linkText, mailToEmailAddress) linkUrl="mailto:${mailToEmailAddress}"
    linkClass=linkClass
    linkScreenReaderText=linkScreenReaderText
  />
</#macro>