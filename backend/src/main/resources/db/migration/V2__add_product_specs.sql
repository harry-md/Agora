CREATE TABLE spec_definitions
(
    id        UUID PRIMARY KEY,
    code      VARCHAR(255) NOT NULL,
    name      VARCHAR(255) NOT NULL,
    data_type VARCHAR(10)  NOT NULL,
    unit      VARCHAR(255),

    CONSTRAINT uq_spec_definition_code UNIQUE (code),
    CONSTRAINT ck_spec_definition_type
        CHECK (data_type IN ('TEXT', 'NUMBER', 'BOOLEAN'))
);

CREATE TABLE category_specs
(
    id                 UUID PRIMARY KEY,
    category_id        UUID    NOT NULL,
    spec_definition_id UUID    NOT NULL,
    required           BOOLEAN NOT NULL DEFAULT TRUE,
    filterable         BOOLEAN NOT NULL DEFAULT FALSE,
    display_order      INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT uq_category_specs_category_spec_definition
        UNIQUE (category_id, spec_definition_id),

    CONSTRAINT fk_category_specs_category
        FOREIGN KEY (category_id) REFERENCES categories (id),

    CONSTRAINT fk_category_specs_definition
        FOREIGN KEY (spec_definition_id) REFERENCES spec_definitions (id),

    CONSTRAINT ck_category_specs_display_order
        CHECK (display_order >= 0)
);

CREATE TABLE product_spec_values
(
    id                 UUID PRIMARY KEY,
    product_id         UUID NOT NULL,
    spec_definition_id UUID NOT NULL,
    text_value         VARCHAR(255),
    number_value       NUMERIC,
    boolean_value      BOOLEAN,

    CONSTRAINT uq_product_spec_values_product_spec
        UNIQUE (product_id, spec_definition_id),

    CONSTRAINT fk_product_spec_values_product
        FOREIGN KEY (product_id) REFERENCES products (id),

    CONSTRAINT fk_product_spec_values_definition
        FOREIGN KEY (spec_definition_id) REFERENCES spec_definitions (id),

    CONSTRAINT ck_product_spec_values_one_value
        CHECK (
            num_nonnulls(text_value, number_value, boolean_value) = 1
            )
);