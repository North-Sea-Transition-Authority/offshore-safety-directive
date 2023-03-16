# Data to migrate

The data to migrate will be coming from an NSTA spreadsheet. This document details the data in the spreadsheet
and the migration effort using T-shirt sizing for each.

| Effort | Description                                                                                             |
|--------|---------------------------------------------------------------------------------------------------------|
| SMALL  | Requires minimal to no effort to migrate into WIOS                                                      |
| MEDIUM | Requires some effort to migrate into WIOS but effort is known ahead of time and is same for most values |
| LARGE  | A lot of effort to pre-process the data prior to migrating in to WIOS                                   |

<table>
  <thead>
    <tr>
      <th>Sheet name</th>
      <th>What</th>
      <th>Column</th>
      <th>Effort</th>
      <th>Note</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td rowspan="9">Wells migration template</td>
      <td>Well registration number</td>
      <td>A</td>
      <td>LARGE</td>
      <td>
        <p>Need to check the well registration numbers exist in the portal. Need a way of flagging which wellbores we cannot identify from the portal dataset and needing to send to NSTA to update their spreadsheet.</p>
        <p>Potential concerns regarding wellbore registration numbers having spaces incorrect added or remove in spreadsheet which could lead to a mismatch. </p>
        <p>Mitigate slightly by Fivium having provided a list of wellbores to NSTA so not expecting many issues.</p>
        <p>Will need to do this process a few times prior to go live to combat new wellbores being added to WONS.</p>
      </td>
    </tr>
    <tr>
      <td>Appointed operator</td>
      <td>B</td>
      <td>LARGE</td>
      <td>
        <p>Need to check the organisation names match exactly an Energy Portal organisation unit. Need a way of flagging which organisations we cannot identify from the portal dataset and needing to send to NSTA to update their spreadsheet.</p>
        <p>Potential concerns around having the names and not the numbers which would be easier to look up.</p>
        <p>NSTA are adding these names free text so high chance of possible issues with data entry.</p>
        <p>Will need to do this process a few times prior to go live to combat renamed organisations being added to WIOS.</p>
      </td>
    </tr>
    <tr>
      <td>Responsible from date</td>
      <td>M</td>
      <td>MEDIUM</td>
      <td>Format in spreadsheet is DD/MM/YYYY but database format will be YYYY-MM-DD. Easy to convert the format as all dates are the same</td>
    </tr>
    <tr>
      <td>Responsible to date</td>
      <td>N</td>
      <td>MEDIUM</td>
      <td>Format in spreadsheet is DD/MM/YYYY but database format will be YYYY-MM-DD. Easy to convert the format as all dates are the same</td>
    </tr>
    <tr>
      <td>E&A phase</td>
      <td>O</td>
      <td>SMALL</td>
      <td>If "yes" in this column then EXPLORATION_AND_APPRAISAL written to asset_phases table</td>
    </tr>
    <tr>
      <td>Development phase</td>
      <td>P</td>
      <td>SMALL</td>
      <td>If "yes" in this column then DEVELOPMENT written to asset_phases table</td>
    </tr>
    <tr>
      <td>Decom phase</td>
      <td>Q</td>
      <td>SMALL</td>
      <td>If "yes" in this column then DECOMMISSIONING written to asset_phases table</td>
    </tr>
    <tr>
      <td>Appointment source</td>
      <td>R</td>
      <td>SMALL</td>
      <td>
        <p>If "deemed" then DEEMED</p>
        <p>If "Nominated" then NOMINATED</p>
        <p>If "Forward approved" then FORWARD_APPROVED</p>
      </td>
    </tr>
    <tr>
      <td>Migrated nomination reference</td>
      <td>S</td>
      <td>SMALL</td>
      <td>Copy in whatever is in the cell as is</td>
    </tr>
    <tr>
      <td rowspan="12">Forward approvals mig template</td>
      <td>Licence type</td>
      <td>A</td>
      <td>MEDIUM</td>
      <td>
        <p>Need to check that the licence type is a licence type that exists in the Energy Portal dataset (the licence type mapset)</p>
        <p>This needs to be combined with the licence number in order to check this is a valid licence, e.g. P and 123 should make to a licence with reference P123 in PEARS.</p>
        <p>This needs to be an extant licence.</p>
        <p>Less room for error compared to well and organisation names as very easy data entry.</p>
        <p>Would need a way of flagging to NSTA if any licence doesn't exist on the portal.</p>
      </td>
    </tr>
    <tr>
      <td>Licence no</td>
      <td>B</td>
      <td>MEDIUM</td>
      <td>
        <p>See above</p>
      </td>
    </tr>
    <tr>
      <td>Block reference</td>
      <td>C</td>
      <td>LARGE</td>
      <td>Need to check that the block reference is a valid block reference for a block on an extant licence on the Energy Portal</td>
    </tr>
    <tr>
      <td>Subarea title</td>
      <td>D</td>
      <td>LARGE</td>
      <td>Need to check that this subarea is a valid subarea on the block reference and licence on the Energy Portal at the time of the appointment</td>
    </tr>
    <tr>
      <td>Appointed operator</td>
      <td>F</td>
      <td>LARGE</td>
      <td>
        <p>Need to check the organisation names match exactly an Energy Portal organisation unit. Need a way of flagging which organisations we cannot identify from the portal dataset and needing to send to NSTA to update their spreadsheet.</p>
        <p>Potential concerns around having the names and not the numbers which would be easier to look up.</p>
        <p>NSTA are adding these names free text so high chance of possible issues with data entry.</p>
        <p>Will need to do this process a few times prior to go live to combat renamed organisations being added to WIOS.</p>
      </td>
    </tr>
    <tr>
      <td>Responsible from date</td>
      <td>G</td>
      <td>MEDIUM</td>
      <td>Format in spreadsheet is DD/MM/YYYY but database format will be YYYY-MM-DD. Easy to convert the format as all dates are the same</td>
    </tr>
    <tr>
      <td>Responsible to date</td>
      <td>H</td>
      <td>MEDIUM</td>
      <td>Format in spreadsheet is DD/MM/YYYY but database format will be YYYY-MM-DD. Easy to convert the format as all dates are the same</td>
    </tr>
    <tr>
      <td>E&A phase </td>
      <td>I</td>
      <td>SMALL</td>
      <td>If "yes" in this column then EXPLORATION_AND_APPRAISAL written to asset_phases table</td>
    </tr>
    <tr>
      <td>Development phase</td>
      <td>J</td>
      <td>SMALL</td>
      <td>If "yes" in this column then DEVELOPMENT written to asset_phases table</td>
    </tr>
    <tr>
      <td>Decom phase</td>
      <td>K</td>
      <td>SMALL</td>
      <td>If "yes" in this column then DECOMMISSIONING written to asset_phases table</td>
    </tr>
    <tr>
      <td>Appointment source</td>
      <td>L</td>
      <td>SMALL</td>
      <td>
        <p>If "deemed" then DEEMED</p>
        <p>If "Nominated" then NOMINATED</p>
        <p>If "Forward approved" then FORWARD_APPROVED</p>
      </td>
    </tr>
    <tr>
      <td>Migrated nomination reference</td>
      <td>M</td>
      <td>SMALL</td>
      <td>Copy in whatever is in the cell as is</td>
    </tr>
    <tr>
      <td rowspan="8">Installation mig template</td>
      <td>Facility name</td>
      <td>A</td>
      <td>LARGE</td>
      <td>
        <p>
            Need to check the installations names exist in the portal. Need a way of flagging which installations we 
            cannot identify from the portal dataset and needing to send to NSTA to update their spreadsheet.
        </p>
        <p>
            Potential concerns regarding installation names having spaces incorrect added or remove in spreadsheet 
            which could lead to a mismatch.
        </p>
        <p>
            Mitigate slightly by Fivium having provided a list of installations to NSTA so not expecting many issues.
        </p>
        <p>
            Will need to do this process a few times prior to go live to combat new installations being added to DEVUK.
        </p>
      </td>
    </tr>
    <tr>
      <td>Appointed operator</td>
      <td>B</td>
      <td>LARGE</td>
      <td>
        <p>Need to check the organisation names match exactly an Energy Portal organisation unit. Need a way of flagging which organisations we cannot identify from the portal dataset and needing to send to NSTA to update their spreadsheet.</p>
        <p>Potential concerns around having the names and not the numbers which would be easier to look up.</p>
        <p>NSTA are adding these names free text so high chance of possible issues with data entry.</p>
        <p>Will need to do this process a few times prior to go live to combat renamed organisations being added to WIOS.</p>
      </td>
    </tr>
    <tr>
      <td>Responsible from date</td>
      <td>C</td>
      <td>MEDIUM</td>
      <td>Format in spreadsheet is DD/MM/YYYY but database format will be YYYY-MM-DD. Easy to convert the format as all dates are the same</td>
    </tr>
    <tr>
      <td>Responsible to date</td>
      <td>D</td>
      <td>MEDIUM</td>
      <td>Format in spreadsheet is DD/MM/YYYY but database format will be YYYY-MM-DD. Easy to convert the format as all dates are the same</td>
    </tr>
    <tr>
      <td>Development phase</td>
      <td>E</td>
      <td>MEDIUM</td>
      <td>
        If "yes" in this column then the following phases are written to the asset_phases table:
        <ul>
          <li>DEVELOPMENT_DESIGN</li>
          <li>DEVELOPMENT_CONSTRUCTION</li>
          <li>DEVELOPMENT_INSTALLATION</li>
          <li>DEVELOPMENT_COMMISSIONING</li>
          <li>DEVELOPMENT_PRODUCTION</li>
        </ul>
      </td>
    </tr>
    <tr>
      <td>Decom phase</td>
      <td>F</td>
      <td>SMALL</td>
      <td>If "yes" in this column then DECOMMISSIONING written to asset_phases table</td>
    </tr>
    <tr>
      <td>Appointment source</td>
      <td>G</td>
      <td>SMALL</td>
      <td>
        <p>If "deemed" then DEEMED</p>
        <p>If "Nominated" then NOMINATED</p>
        <p>If "Forward approved" then FORWARD_APPROVED</p>
      </td>
    </tr>
    <tr>
      <td>Migrated nomination reference</td>
      <td>H</td>
      <td>SMALL</td>
      <td>Copy in whatever is in the cell as is</td>
    </tr>
  </tbody>
</table>