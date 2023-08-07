CREATE OR REPLACE PACKAGE wios_migration.operator_name_mapping AS

  /**
    Function to get the ID of an operator from a name.

    @param p_operator_name The name of the operator to retrieve the ID of
    @return The ID of the operator matching the provided name or NULL if there is
            no matching operator with that name
  */
  FUNCTION get_operator_from_name(
    p_operator_name IN VARCHAR2
  ) RETURN NUMBER;

  /**
    Utility function to remove the company house number from an operator name.

    Assumptions:
    - The string will be provided in the following format: name (company house number)
    - The company house number will be 8 characters long

    If the operator name doesn't contain the company house number then the input will
    be returned unchanged.

    @param p_operator_name The name of the operator with the company house number attached
    @return The operator name without the company house number
  */
  FUNCTION remove_company_house_number(p_operator_name IN VARCHAR2)
  RETURN VARCHAR2 DETERMINISTIC;

END operator_name_mapping;

CREATE OR REPLACE PACKAGE BODY wios_migration.operator_name_mapping AS

  /**
    Utility procedure to write an entry to the unmatched organisation units
    table so we can report back to NSTA about any incorrect organisations
    that have been entered.

    @param p_operator_name The name of the operator that doesn't exist in the
                           portal dataset
  */
  PROCEDURE create_unmatched_operator_entry(
    p_operator_name IN VARCHAR2
  )
  IS

    PRAGMA AUTONOMOUS_TRANSACTION;

    l_possible_matches CLOB;

  BEGIN

    SELECT st.join(stagg(ou.name))
    INTO l_possible_matches
    FROM decmgr.organisation_units ou
    WHERE SUBSTR(ou.name, 0, 5) = SUBSTR(TO_CHAR(p_operator_name), 0, 5);

    MERGE INTO wios_migration.unmatched_organisation_units uoi
    USING (
      SELECT TO_CHAR(p_operator_name) operator_name
      FROM dual
    ) operator
      ON (TO_CHAR(uoi.operator_name) = operator.operator_name)
    WHEN NOT MATCHED THEN
      INSERT (operator_name, possible_matches)
      VALUES(p_operator_name, l_possible_matches);

    COMMIT;

  END create_unmatched_operator_entry;

  FUNCTION remove_company_house_number(p_operator_name IN VARCHAR2)
  RETURN VARCHAR2 DETERMINISTIC
  IS

  BEGIN

    -- Assumption from portal is company house number is 8 characters.
    -- Assumption from migration spreadsheet is format is [name (company house number)]
    RETURN TRIM(REGEXP_REPLACE(p_operator_name, '\((.{8}?)\)$',''));

  END;

  FUNCTION get_operator_from_name(p_operator_name IN VARCHAR2)
  RETURN NUMBER
  IS

    l_operator_id NUMBER;
    l_current_name VARCHAR2(4000);
    l_matched_name VARCHAR2(4000);

  BEGIN

    IF p_operator_name = 'NO OPERATOR' THEN

      raise_application_error(
        -20999
      , 'An appointment for the NO OPERATOR organisation unit is incorrectly included in the migration spreadsheet'
      );

    END IF;

    BEGIN

      SELECT
        xou.organ_id id
      , xon.name matched_name
      , xou.name current_name
      INTO
        l_operator_id
      , l_matched_name
      , l_current_name
      FROM decmgr.xview_organisation_names xon
      JOIN decmgr.xview_organisation_units xou ON xou.organ_id = xon.organ_id
      WHERE xon.name = remove_company_house_number(p_operator_name);

      IF l_matched_name != l_current_name THEN

        MERGE INTO wios_migration.historical_company_name_mappings hm
          USING (SELECT l_matched_name name_in_spreadsheet, l_current_name name_in_sor FROM dual) x
          ON (hm.name_in_spreadsheet = x.name_in_spreadsheet)
        WHEN NOT MATCHED THEN
          INSERT (name_in_spreadsheet, name_in_sor)
          VALUES(x.name_in_spreadsheet, x.name_in_sor);

      END IF;

    EXCEPTION

      WHEN NO_DATA_FOUND THEN

        create_unmatched_operator_entry(
          p_operator_name => p_operator_name
        );

        l_operator_id := NULL;

      WHEN TOO_MANY_ROWS THEN

        raise_application_error(-20999, 'Multiple organisations with name ' || remove_company_house_number(p_operator_name));

    END;

    RETURN l_operator_id;

  END get_operator_from_name;

END operator_name_mapping;