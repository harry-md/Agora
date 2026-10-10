--
-- PostgreSQL database dump
--

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

-- *not* creating schema, since initdb creates it


--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS '';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: addresses; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.addresses
(
    id          uuid                   NOT NULL,
    address     character varying(255) NOT NULL,
    country_id  uuid                   NOT NULL,
    province_id uuid                   NOT NULL,
    ward_id     uuid                   NOT NULL
);


--
-- Name: categories; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.categories
(
    id         uuid                        NOT NULL,
    is_active  boolean                     NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    image_url  character varying(500)      NOT NULL,
    name       character varying(200)      NOT NULL,
    slug       character varying(150)      NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    parent_id  uuid
);

--
-- Name: countries; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.countries
(
    id   uuid                   NOT NULL,
    name character varying(200) NOT NULL
);

--
-- Name: manufacturers; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.manufacturers
(
    id          uuid                   NOT NULL,
    is_active   boolean                NOT NULL,
    country_id  uuid                   NOT NULL,
    description text,
    logo_url    character varying(500) NOT NULL,
    name        character varying(200) NOT NULL,
    slug        character varying(150) NOT NULL
);

--
-- Name: order_details; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.order_details
(
    id         uuid           NOT NULL,
    price      numeric(15, 2) NOT NULL,
    product_id uuid           NOT NULL,
    quantity   integer        NOT NULL,
    order_id   uuid           NOT NULL
);

--
-- Name: orders; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.orders
(
    id           uuid                        NOT NULL,
    created_at   timestamp(6) with time zone NOT NULL,
    order_number bigint                      NOT NULL,
    paid_at      timestamp(6) with time zone,
    status       character varying(255)      NOT NULL,
    total_price  numeric(15, 2)              NOT NULL,
    updated_at   timestamp(6) with time zone NOT NULL,
    user_id      uuid                        NOT NULL,
    CONSTRAINT orders_status_check CHECK (((status)::text = ANY
                                           ((ARRAY ['PENDING'::character varying, 'PAID'::character varying, 'CANCELLED'::character varying])::text[])))
);

--
-- Name: payments; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.payments
(
    id                   uuid                        NOT NULL,
    amount               numeric(12, 2)              NOT NULL,
    created_at           timestamp(6) with time zone NOT NULL,
    merchant_reference   character varying(255),
    order_id             uuid                        NOT NULL,
    provider             character varying(255)      NOT NULL,
    provider_checkout_id character varying(255),
    provider_payment_id  character varying(255),
    status               character varying(255)      NOT NULL,
    updated_at           timestamp(6) with time zone NOT NULL,
    user_id              uuid                        NOT NULL,
    CONSTRAINT payments_provider_check CHECK (((provider)::text = ANY
                                               ((ARRAY ['STRIPE'::character varying, 'ZALOPAY'::character varying])::text[]))),
    CONSTRAINT payments_status_check CHECK (((status)::text = ANY
                                             ((ARRAY ['PENDING'::character varying, 'PAID'::character varying, 'CANCELLED'::character varying, 'EXPIRED'::character varying])::text[])))
);

--
-- Name: product_categories; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.product_categories
(
    category_id uuid NOT NULL,
    product_id  uuid NOT NULL
);

--
-- Name: product_details; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.product_details
(
    description text,
    images      jsonb,
    spec        jsonb,
    product_id  uuid NOT NULL
);

--
-- Name: products; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.products
(
    id             uuid                        NOT NULL,
    average_rating numeric(3, 2)               NOT NULL,
    created_at     timestamp(6) with time zone NOT NULL,
    manufacturer   uuid                        NOT NULL,
    name           character varying(200)      NOT NULL,
    rating_count   integer                     NOT NULL,
    slug           character varying(150)      NOT NULL,
    sold_count     integer                     NOT NULL,
    thumbnail      character varying(500)
);

--
-- Name: provinces; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.provinces
(
    id         uuid                   NOT NULL,
    name       character varying(255) NOT NULL,
    country_id uuid                   NOT NULL
);

--
-- Name: reviews; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.reviews
(
    id         uuid                        NOT NULL,
    comment    text,
    created_at timestamp(6) with time zone NOT NULL,
    product_id uuid                        NOT NULL,
    rating     integer                     NOT NULL,
    user_id    uuid                        NOT NULL
);

--
-- Name: sale_item; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.sale_item
(
    id            uuid           NOT NULL,
    max_per_user  integer        NOT NULL,
    price         numeric(15, 2) NOT NULL,
    quantity      integer        NOT NULL,
    sold_quantity integer        NOT NULL,
    sale_id       uuid           NOT NULL
);

--
-- Name: sales; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.sales
(
    id         uuid                        NOT NULL,
    banner_url character varying(500)      NOT NULL,
    end_at     timestamp(6) with time zone NOT NULL,
    name       character varying(100)      NOT NULL,
    start_at   timestamp(6) with time zone NOT NULL,
    status     character varying(255)      NOT NULL,
    CONSTRAINT sales_status_check CHECK (((status)::text = ANY
                                          ((ARRAY ['UPCOMING'::character varying, 'ACTIVE'::character varying, 'ENDED'::character varying])::text[])))
);

--
-- Name: users; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.users
(
    id         uuid                        NOT NULL,
    is_active  boolean                     NOT NULL,
    address_id uuid,
    avatar     character varying(255)      NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    email      character varying(255)      NOT NULL,
    full_name  character varying(200)      NOT NULL,
    password   character varying(255)      NOT NULL,
    role       character varying(255)      NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    username   character varying(200)      NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY
                                        ((ARRAY ['STAFF'::character varying, 'ADMIN'::character varying, 'CUSTOMER'::character varying])::text[])))
);

--
-- Name: voucher_scopes; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.voucher_scopes
(
    id          uuid NOT NULL,
    category_id uuid,
    product_id  uuid,
    voucher_id  uuid NOT NULL
);

--
-- Name: voucher_usages; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.voucher_usages
(
    id              uuid                        NOT NULL,
    discount_amount numeric(12, 2)              NOT NULL,
    order_id        uuid                        NOT NULL,
    used_at         timestamp(6) with time zone NOT NULL,
    user_id         uuid                        NOT NULL,
    voucher_id      uuid                        NOT NULL
);

--
-- Name: vouchers; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.vouchers
(
    id              uuid                        NOT NULL,
    is_active       boolean                     NOT NULL,
    code            character varying(50)       NOT NULL,
    created_at      timestamp(6) with time zone NOT NULL,
    discount_value  real                        NOT NULL,
    end_at          timestamp(6) with time zone NOT NULL,
    max_discount    numeric(12, 2)              NOT NULL,
    min_order_value numeric(12, 2)              NOT NULL,
    start_at        timestamp(6) with time zone NOT NULL,
    type            character varying(255)      NOT NULL,
    usage_limit     integer                     NOT NULL,
    used_count      integer                     NOT NULL,
    CONSTRAINT vouchers_type_check CHECK (((type)::text = ANY
                                           ((ARRAY ['PERCENT'::character varying, 'FIXED'::character varying])::text[])))
);

--
-- Name: wards; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.wards
(
    id          uuid                   NOT NULL,
    name        character varying(255) NOT NULL,
    province_id uuid                   NOT NULL
);

--
-- Name: addresses addresses_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.addresses
    ADD CONSTRAINT addresses_pkey PRIMARY KEY (id);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);


--
-- Name: countries countries_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.countries
    ADD CONSTRAINT countries_pkey PRIMARY KEY (id);


--
-- Name: manufacturers manufacturers_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.manufacturers
    ADD CONSTRAINT manufacturers_pkey PRIMARY KEY (id);


--
-- Name: order_details order_details_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.order_details
    ADD CONSTRAINT order_details_pkey PRIMARY KEY (id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- Name: product_categories product_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.product_categories
    ADD CONSTRAINT product_categories_pkey PRIMARY KEY (category_id, product_id);


--
-- Name: product_details product_details_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.product_details
    ADD CONSTRAINT product_details_pkey PRIMARY KEY (product_id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: provinces provinces_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.provinces
    ADD CONSTRAINT provinces_pkey PRIMARY KEY (id);


--
-- Name: reviews reviews_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_pkey PRIMARY KEY (id);


--
-- Name: sale_item sale_item_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.sale_item
    ADD CONSTRAINT sale_item_pkey PRIMARY KEY (id);


--
-- Name: sales sales_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.sales
    ADD CONSTRAINT sales_pkey PRIMARY KEY (id);


--
-- Name: reviews uk_review_user_product; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT uk_review_user_product UNIQUE (user_id, product_id);


--
-- Name: voucher_scopes uk_voucher_scope_category; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.voucher_scopes
    ADD CONSTRAINT uk_voucher_scope_category UNIQUE (voucher_id, category_id);


--
-- Name: voucher_scopes uk_voucher_scope_product; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.voucher_scopes
    ADD CONSTRAINT uk_voucher_scope_product UNIQUE (voucher_id, product_id);


--
-- Name: orders uknthkiu7pgmnqnu86i2jyoe2v7; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT uknthkiu7pgmnqnu86i2jyoe2v7 UNIQUE (order_number);


--
-- Name: users uq_users_email; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uq_users_email UNIQUE (email);


--
-- Name: users uq_users_username; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uq_users_username UNIQUE (username);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: voucher_scopes voucher_scopes_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.voucher_scopes
    ADD CONSTRAINT voucher_scopes_pkey PRIMARY KEY (id);


--
-- Name: voucher_usages voucher_usages_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.voucher_usages
    ADD CONSTRAINT voucher_usages_pkey PRIMARY KEY (id);


--
-- Name: vouchers vouchers_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.vouchers
    ADD CONSTRAINT vouchers_pkey PRIMARY KEY (id);


--
-- Name: wards wards_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.wards
    ADD CONSTRAINT wards_pkey PRIMARY KEY (id);


--
-- Name: idx_users_address; Type: INDEX; Schema: public; Owner: root
--

CREATE INDEX idx_users_address ON public.users USING btree (address_id);


--
-- Name: provinces fk48p9qkti5auert2gquvn76338; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.provinces
    ADD CONSTRAINT fk48p9qkti5auert2gquvn76338 FOREIGN KEY (country_id) REFERENCES public.countries (id);


--
-- Name: addresses fk6h32ws7shu7ei7c4dxvm5dyv6; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.addresses
    ADD CONSTRAINT fk6h32ws7shu7ei7c4dxvm5dyv6 FOREIGN KEY (province_id) REFERENCES public.provinces (id);


--
-- Name: wards fkbwfs5nhey1leef1v5ydhb45j2; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.wards
    ADD CONSTRAINT fkbwfs5nhey1leef1v5ydhb45j2 FOREIGN KEY (province_id) REFERENCES public.provinces (id);


--
-- Name: sale_item fkdjr346l9a8kku0uao1jorl1nj; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.sale_item
    ADD CONSTRAINT fkdjr346l9a8kku0uao1jorl1nj FOREIGN KEY (sale_id) REFERENCES public.sales (id);


--
-- Name: voucher_scopes fkg2w80va912yy28bjvyfd1t30o; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.voucher_scopes
    ADD CONSTRAINT fkg2w80va912yy28bjvyfd1t30o FOREIGN KEY (voucher_id) REFERENCES public.vouchers (id);


--
-- Name: addresses fkgafq5o69m5p2rq5q5egx2sfm; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.addresses
    ADD CONSTRAINT fkgafq5o69m5p2rq5q5egx2sfm FOREIGN KEY (ward_id) REFERENCES public.wards (id);


--
-- Name: voucher_usages fkguvf95urdn2namu0hgiasgttx; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.voucher_usages
    ADD CONSTRAINT fkguvf95urdn2namu0hgiasgttx FOREIGN KEY (voucher_id) REFERENCES public.vouchers (id);


--
-- Name: order_details fkjyu2qbqt8gnvno9oe9j2s2ldk; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.order_details
    ADD CONSTRAINT fkjyu2qbqt8gnvno9oe9j2s2ldk FOREIGN KEY (order_id) REFERENCES public.orders (id);


--
-- Name: product_categories fklda9rad6s180ha3dl1ncsp8n7; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.product_categories
    ADD CONSTRAINT fklda9rad6s180ha3dl1ncsp8n7 FOREIGN KEY (product_id) REFERENCES public.products (id);


--
-- Name: addresses fkn3sth7s3kur1rafwbbrqqnswt; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.addresses
    ADD CONSTRAINT fkn3sth7s3kur1rafwbbrqqnswt FOREIGN KEY (country_id) REFERENCES public.countries (id);


--
-- Name: product_details fknfvvq3meg4ha3u1bju9k4is3r; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.product_details
    ADD CONSTRAINT fknfvvq3meg4ha3u1bju9k4is3r FOREIGN KEY (product_id) REFERENCES public.products (id);


--
-- Name: categories fksaok720gsu4u2wrgbk10b5n8d; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT fksaok720gsu4u2wrgbk10b5n8d FOREIGN KEY (parent_id) REFERENCES public.categories (id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

--
-- PostgreSQL database dump complete
--
