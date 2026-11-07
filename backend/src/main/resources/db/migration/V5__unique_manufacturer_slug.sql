ALTER TABLE public.manufacturers
    ADD CONSTRAINT uq_manufacturers_slug UNIQUE (slug);
