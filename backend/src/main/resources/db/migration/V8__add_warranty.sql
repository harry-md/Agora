CREATE TABLE public.warranty_policies
(
    id             uuid PRIMARY KEY,
    code           varchar(100)                NOT NULL,
    version        integer                     NOT NULL,
    title          varchar(255)                NOT NULL,
    effective_from timestamp(6) with time zone NOT NULL,
    document_key   varchar(500)                NOT NULL,
    created_at     timestamp(6) with time zone NOT NULL,
    CONSTRAINT uq_warranty_policies_code_version UNIQUE (code, version)
);


ALTER TABLE public.products
    ADD COLUMN price                   numeric(15, 2) NOT NULL,
    ADD COLUMN updated_at              timestamp(6) with time zone NOT NULL,
    ADD COLUMN warranty_policy_id      uuid NOT NULL,
    ADD COLUMN warranty_duration_value integer,
    ADD COLUMN warranty_duration_unit  varchar(10) NOT NULL DEFAULT 'MONTH';


ALTER TABLE public.products
    ADD CONSTRAINT fk_products_warranty_policy
        FOREIGN KEY (warranty_policy_id) REFERENCES public.warranty_policies (id);


ALTER TABLE public.order_details
    ADD COLUMN delivered_at            timestamp(6) with time zone,
    ADD COLUMN warranty_policy_id      uuid        NOT NULL,
    ADD COLUMN warranty_duration_value integer,
    ADD COLUMN warranty_duration_unit  varchar(10) NOT NULL DEFAULT 'MONTH',
    ADD COLUMN warranty_starts_on      date,
    ADD COLUMN warranty_expires_on     date;

ALTER TABLE public.order_details
    ADD CONSTRAINT fk_order_details_warranty_policy
        FOREIGN KEY (warranty_policy_id) REFERENCES public.warranty_policies (id);
