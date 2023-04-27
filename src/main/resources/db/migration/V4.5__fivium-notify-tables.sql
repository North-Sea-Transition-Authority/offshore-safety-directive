-- ## Outbound Email ##
CREATE TABLE osd.outbound_email (
    id SERIAL PRIMARY KEY,
    template_name TEXT NOT NULL,
    email_address TEXT NOT NULL,
    reference TEXT,
    status TEXT NOT NULL,
    email_reply_to_id TEXT,
    created_date_time TIMESTAMP NOT NULL,
    updated_date_time TIMESTAMP
);

CREATE TABLE osd.outbound_email_personalisation (
    id SERIAL PRIMARY KEY,
    outbound_email_id SERIAL NOT NULL,
    key TEXT NOT NULL,
    value TEXT NOT NULL,
    CONSTRAINT outbound_email_personalisation_fk1 FOREIGN KEY (outbound_email_id) REFERENCES outbound_email (id)
);

CREATE INDEX outbound_email_personalisation_idx1 ON outbound_email_personalisation(outbound_email_id);