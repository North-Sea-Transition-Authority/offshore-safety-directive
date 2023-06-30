CREATE TABLE outbound_email (
    id SERIAL PRIMARY KEY,
    template_name TEXT NOT NULL,
    email_address TEXT NOT NULL,
    reference TEXT,
    status TEXT NOT NULL,
    email_reply_to_id TEXT,
    created_date_time TIMESTAMP NOT NULL,
    updated_date_time TIMESTAMP
);

CREATE TABLE outbound_email_personalisation (
    id SERIAL PRIMARY KEY,
    outbound_email_id SERIAL NOT NULL,
    key TEXT NOT NULL,
    value TEXT NOT NULL,
    CONSTRAINT outbound_email_personalisation_outbound_email_id_fk
      FOREIGN KEY (outbound_email_id) REFERENCES outbound_email (id)
);

CREATE INDEX outbound_email_personalisation_outbound_email_id_idx
ON outbound_email_personalisation(outbound_email_id);