CREATE OR REPLACE PACKAGE wios_migration.operator_name_mapping AS

  TYPE t_operator_lookup_type
    IS TABLE OF NUMBER NOT NULL
    INDEX BY VARCHAR2(4000);

  /**
    Function to initialise a type containing all the valid
    organisation unit names and their IDs. This is used for easy
    lookup of organisations based on their names

    @return a table of organisation IDs indexed by organisation names
  */
  FUNCTION initialise_operator_lookup
  RETURN t_operator_lookup_type;

  /**
    Function to get the ID of an operator from a name.

    @param p_operator_name The name of the operator to retrieve the ID of
    @param p_operator_lookup_type A table of organisation IDs indexed by organisation names
    @return The ID of the operator matching the provided name or NULL if there is
            no matching operator with that name
  */
  FUNCTION get_operator_from_name(
    p_operator_name IN VARCHAR2
  , p_operator_lookup_type IN t_operator_lookup_type
  ) RETURN NUMBER;

END operator_name_mapping;

CREATE OR REPLACE PACKAGE BODY wios_migration.operator_name_mapping AS

  FUNCTION initialise_operator_lookup
  RETURN t_operator_lookup_type
  IS

    l_operator_lookup t_operator_lookup_type;

  BEGIN

    FOR organisation_unit IN (
      SELECT
        ou.name
      , ou.id
      FROM decmgr.organisation_units ou
    )
    LOOP

      l_operator_lookup(organisation_unit.name) := organisation_unit.id;

    END LOOP;

    RETURN l_operator_lookup;

  END initialise_operator_lookup;

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

  FUNCTION get_operator_from_name(
    p_operator_name IN VARCHAR2
  , p_operator_lookup_type IN t_operator_lookup_type
  ) RETURN NUMBER
  IS

    l_operator_id NUMBER;

  BEGIN

    IF p_operator_lookup_type.COUNT = 0 THEN

      raise_application_error(-20999, 'p_operator_lookup_type param has not been initialised');

    END IF;

    BEGIN

      l_operator_id := p_operator_lookup_type(p_operator_name);

    EXCEPTION WHEN NO_DATA_FOUND THEN

      create_unmatched_operator_entry(
        p_operator_name => p_operator_name
      );

      l_operator_id := NULL;

    END;

    RETURN l_operator_id;

  END get_operator_from_name;

END operator_name_mapping;