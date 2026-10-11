ALTER TABLE public.products
    ADD COLUMN IF NOT EXISTS category_id UUID;

DO
$$
    BEGIN
        IF to_regclass('public.product_categories') IS NOT NULL THEN
            IF EXISTS (SELECT product_id
                       FROM public.product_categories
                       GROUP BY product_id
                       HAVING COUNT(*) > 1) THEN
                RAISE EXCEPTION 'Some products have multiple legacy categories.'
                    USING HINT = 'Keep the intended single category per product in product_categories, then restart.';
            END IF;

            IF EXISTS (SELECT 1
                       FROM public.products p
                                JOIN public.product_categories pc ON pc.product_id = p.id
                       WHERE p.category_id IS NOT NULL
                         AND p.category_id <> pc.category_id) THEN
                RAISE EXCEPTION 'products.category_id conflicts with product_categories.'
                    USING HINT = 'Resolve the category assignments before retrying this migration.';
            END IF;

            UPDATE public.products p
            SET category_id = pc.category_id
            FROM public.product_categories pc
            WHERE pc.product_id = p.id
              AND p.category_id IS NULL;
        END IF;

        IF EXISTS (SELECT 1 FROM public.products WHERE category_id IS NULL) THEN
            RAISE EXCEPTION 'Existing products have no category_id; the category cannot be inferred.'
                USING HINT =
                        'Before restarting, ADD COLUMN IF NOT EXISTS category_id UUID to products and UPDATE each missing category_id with a valid categories.id. This failed migration rolls back its own changes.';
        END IF;
    END
$$;

ALTER TABLE public.products
    ALTER COLUMN category_id SET NOT NULL;
ALTER TABLE public.products
    ADD CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES public.categories (id);

DO
$$
    DECLARE
        has_old_column BOOLEAN;
        has_new_column BOOLEAN;
    BEGIN
        SELECT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_schema = 'public'
                         AND table_name = 'products'
                         AND column_name = 'manufacturer')
        INTO has_old_column;

        SELECT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_schema = 'public'
                         AND table_name = 'products'
                         AND column_name = 'manufacturer_id')
        INTO has_new_column;

        IF has_old_column AND has_new_column THEN
            RAISE EXCEPTION 'Both manufacturer and manufacturer_id exist on products.'
                USING HINT =
                        'Reconcile the two columns before retrying; this migration will not discard either column.';
        ELSIF has_old_column THEN
            ALTER TABLE public.products
                RENAME COLUMN manufacturer TO manufacturer_id;
        ELSIF NOT has_new_column THEN
            RAISE EXCEPTION 'Neither manufacturer nor manufacturer_id exists on products.';
        END IF;
    END
$$;

ALTER TABLE public.products
    ALTER COLUMN manufacturer_id SET NOT NULL;
ALTER TABLE public.products
    ADD CONSTRAINT fk_products_manufacturer
        FOREIGN KEY (manufacturer_id) REFERENCES public.manufacturers (id);

ALTER TABLE public.products
    ADD CONSTRAINT uq_products_slug UNIQUE (slug);

ALTER TABLE public.categories
    ADD CONSTRAINT uq_categories_slug UNIQUE (slug);

UPDATE public.product_details
SET images = NULL
WHERE images = 'null'::jsonb;

UPDATE public.product_details
SET images = jsonb_build_array(images)
WHERE jsonb_typeof(images) = 'object';

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM public.product_details
                   WHERE images IS NOT NULL
                     AND jsonb_typeof(images) <> 'array') THEN
            RAISE EXCEPTION 'product_details.images contains a JSON scalar instead of a gallery.'
                USING HINT = 'Use a JSON array of image objects or NULL before retrying.';
        END IF;
    END
$$;

DROP TABLE IF EXISTS public.product_categories;

CREATE INDEX idx_products_category ON public.products (category_id);
CREATE INDEX idx_products_manufacturer ON public.products (manufacturer_id);

CREATE INDEX idx_product_spec_values_number
    ON public.product_spec_values (spec_definition_id, number_value, product_id);

CREATE INDEX idx_category_specs_definition
    ON public.category_specs (spec_definition_id);
