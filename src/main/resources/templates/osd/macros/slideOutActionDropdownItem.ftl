<#import "../../fds/components/slideOutPanel/slideOutPanel.ftl" as fdsSlideOutPanel/>

<#--
 This file is used to construct an action that opens a slideout panel for use within an actionDropdown.
 The parameters of an actionDropdownItem do not (at the time of writing) support.

 A jira has been raised to resolve as part of FDS. See: (https://jira.fivium.co.uk/browse/FDS-431)
 -->
<#macro slideOutActionDropdownItem actionText slideOutPanelId>
  <li class="fds-action-dropdown__list-item">
      <@fdsSlideOutPanel.slideOutPanelButton buttonText=actionText buttonPanelId=slideOutPanelId buttonClass="fds-action-dropdown__item-button fds-link-button"/>
  </li>
</#macro>