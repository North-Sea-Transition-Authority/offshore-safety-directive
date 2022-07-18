<#include '../layout/layout.ftl'/>

<#macro mailToLink linkText mailToEmailAddress linkClass="govuk-link" linkScreenReaderText="">
  <@fdsAction.link
    linkText=linkText linkUrl="mailto:${mailToEmailAddress}"
    linkClass=linkClass
    linkScreenReaderText=linkScreenReaderText
  />
</#macro>