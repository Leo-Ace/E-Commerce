create extension if not exists pgcrypto;

create table if not exists users
(
    id             uuid primary key default gen_random_uuid(),
    email          varchar(255) not null unique,
    password_hash  varchar(255) not null,
    full_name      varchar(255) not null,
    phone          varchar(50) unique,
    status         varchar(50)  not null,
    email_verified boolean      not null default false,
    phone_verified boolean      not null default false,
    is_active      boolean      not null default true,
    last_login_at  timestamp,
    created_at     timestamp    not null default now(),
    updated_at     timestamp    not null default now(),
    created_by varchar(100) not null default 'system',
    updated_by varchar(100) not null default 'system',
    constraint chk_users_status check (status in ('PENDING', 'ACTIVE', 'LOCKED', 'DISABLED'))
);

create table if not exists roles
(
    id          uuid primary key default gen_random_uuid(),
    code        varchar(100) not null unique,
    name        varchar(150) not null,
    description varchar(500),
    is_active   boolean      not null default true,
    created_at  timestamp    not null default now(),
    updated_at  timestamp    not null default now(),
    created_by varchar(100) not null default 'system',
    updated_by varchar(100) not null default 'system'
);

create table if not exists permissions
(
    id          uuid primary key default gen_random_uuid(),
    code        varchar(100) not null unique,
    name        varchar(150) not null,
    description varchar(500),
    is_active   boolean      not null default true,
    created_at  timestamp    not null default now(),
    updated_at  timestamp    not null default now(),
    created_by varchar(100) not null default 'system',
    updated_by varchar(100) not null default 'system'
);

create table if not exists user_roles
(
    user_id uuid not null references users (id) on delete cascade,
    role_id uuid not null references roles (id) on delete cascade,
    primary key (user_id, role_id)
);

create table if not exists role_permissions
(
    role_id       uuid not null references roles (id) on delete cascade,
    permission_id uuid not null references permissions (id) on delete cascade,
    primary key (role_id, permission_id)
);

create table if not exists sellers
(
    id         uuid primary key default gen_random_uuid(),
    code       varchar(100) not null unique,
    name       varchar(255) not null,
    slug       varchar(255) not null unique,
    email      varchar(255) unique,
    phone      varchar(50) unique,
    status     varchar(50)  not null,
    is_active  boolean      not null default true,
    created_at timestamp    not null default now(),
    updated_at timestamp    not null default now(),
    created_by varchar(100) not null default 'system',
    updated_by varchar(100) not null default 'system',
    constraint chk_sellers_status check (status in ('PENDING', 'ACTIVE', 'SUSPENDED', 'INACTIVE'))
);

create table if not exists seller_users
(
    id              uuid primary key default gen_random_uuid(),
    seller_id       uuid        not null references sellers (id) on delete cascade,
    user_id         uuid        not null references users (id) on delete cascade,
    membership_role varchar(30) not null,
    is_default      boolean     not null default false,
    is_active       boolean     not null default true,
    created_at      timestamp   not null default now(),
    updated_at      timestamp   not null default now(),
    created_by varchar(100) not null default 'system',
    updated_by varchar(100) not null default 'system',
    constraint uk_seller_users_seller_user unique (seller_id, user_id),
    constraint chk_seller_users_membership_role check (membership_role in ('OWNER', 'MANAGER', 'STAFF'))
);

create table if not exists brands
(
    id         uuid primary key default gen_random_uuid(),
    seller_id  uuid         not null references sellers (id),
    code       varchar(100) not null unique,
    name       varchar(255) not null,
    slug       varchar(255) not null unique,
    status     varchar(50)  not null,
    is_active  boolean      not null default true,
    created_at timestamp    not null default now(),
    updated_at timestamp    not null default now(),
    created_by varchar(100) not null default 'system',
    updated_by varchar(100) not null default 'system',
    constraint chk_brands_status check (status in ('DRAFT', 'ACTIVE', 'INACTIVE'))
);

create table if not exists categories
(
    id         uuid primary key default gen_random_uuid(),
    parent_id  uuid references categories (id),
    name       varchar(255) not null,
    slug       varchar(255) not null unique,
    status     varchar(50)  not null,
    sort_order integer      not null default 0,
    is_active  boolean      not null default true,
    created_at timestamp    not null default now(),
    updated_at timestamp    not null default now(),
    created_by varchar(100) not null default 'system',
    updated_by varchar(100) not null default 'system',
    constraint chk_categories_status check (status in ('DRAFT', 'ACTIVE', 'INACTIVE'))
);

create table if not exists products
(
    id                uuid primary key default gen_random_uuid(),
    seller_id         uuid         not null references sellers (id),
    brand_id          uuid references brands (id),
    category_id       uuid         not null references categories (id),
    name              varchar(255) not null,
    slug              varchar(255) not null unique,
    short_description varchar(500),
    description       text,
    status            varchar(50)  not null,
    is_active         boolean      not null default true,
    created_at        timestamp    not null default now(),
    updated_at        timestamp    not null default now(),
    created_by varchar(100) not null default 'system',
    updated_by varchar(100) not null default 'system',
    constraint chk_products_status check (status in ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

create table if not exists skus
(
    id               uuid primary key default gen_random_uuid(),
    product_id       uuid           not null references products (id) on delete cascade,
    sku_code         varchar(100)   not null unique,
    name             varchar(255)   not null,
    price            numeric(18, 2) not null,
    compare_at_price numeric(18, 2),
    status           varchar(50)    not null,
    is_active        boolean        not null default true,
    created_at       timestamp      not null default now(),
    updated_at       timestamp      not null default now(),
    created_by varchar(100) not null default 'system',
    updated_by varchar(100) not null default 'system',
    constraint chk_skus_status check (status in ('DRAFT', 'ACTIVE', 'INACTIVE')),
    constraint chk_skus_price_non_negative check (price >= 0),
    constraint chk_skus_compare_price_non_negative check (compare_at_price is null or compare_at_price >= 0)
);

create table if not exists inventory
(
    id         uuid primary key default gen_random_uuid(),
    sku_id     uuid      not null unique references skus (id) on delete cascade,
    on_hand    integer   not null default 0,
    reserved   integer   not null default 0,
    available  integer   not null default 0,
    updated_at timestamp not null default now(),
    created_by varchar(100) not null default 'system',
    updated_by varchar(100) not null default 'system',
    constraint chk_inventory_non_negative check (on_hand >= 0 and reserved >= 0 and available >= 0),
    constraint chk_inventory_reserved_le_on_hand check (reserved <= on_hand)
);

create table if not exists refresh_tokens
(
    id             uuid primary key,
    user_id        uuid         not null references users (id) on delete cascade,
    token_hash     varchar(255) not null unique,
    expires_at     timestamp    not null,
    revoked_at     timestamp,
    replaced_by_id uuid references refresh_tokens (id),
    device_name    varchar(255),
    ip_address     varchar(100),
    user_agent     varchar(1000),
    last_used_at   timestamp,
    created_at     timestamp    not null default now(),
    updated_at     timestamp    not null default now(),
    created_by varchar(100) not null default 'system',
    updated_by varchar(100) not null default 'system'
);

create index if not exists idx_users_email on users (email);
create index if not exists idx_users_phone on users (phone);
create index if not exists idx_users_status on users (status);

create index if not exists idx_roles_code on roles (code);
create index if not exists idx_permissions_code on permissions (code);

create index if not exists idx_sellers_code on sellers (code);
create index if not exists idx_sellers_slug on sellers (slug);
create index if not exists idx_sellers_status on sellers (status);

create index if not exists idx_seller_users_user_id on seller_users (user_id);
create index if not exists idx_seller_users_seller_id on seller_users (seller_id);
create index if not exists idx_seller_users_membership_role on seller_users (membership_role);

create index if not exists idx_brands_seller_id on brands (seller_id);
create index if not exists idx_brands_slug on brands (slug);
create index if not exists idx_categories_parent_id on categories (parent_id);
create index if not exists idx_categories_slug on categories (slug);
create index if not exists idx_products_seller_id on products (seller_id);
create index if not exists idx_products_brand_id on products (brand_id);
create index if not exists idx_products_category_id on products (category_id);
create index if not exists idx_products_slug on products (slug);
create index if not exists idx_skus_product_id on skus (product_id);
create index if not exists idx_skus_sku_code on skus (sku_code);
create index if not exists idx_refresh_tokens_user_id on refresh_tokens (user_id);
create index if not exists idx_refresh_tokens_expires_at on refresh_tokens (expires_at);

insert into roles(code, name, description)
values
    ('SUPER_ADMIN', 'Super Admin', 'Highest level administrator'),
    ('ADMIN', 'Admin', 'Backoffice administrator'),
    ('SELLER_ADMIN', 'Seller Admin', 'Seller side administrator'),
    ('BUYER', 'Buyer', 'End customer account')
on conflict (code) do nothing;

insert into permissions(code, name, description)
values
    ('USER_READ', 'User Read', 'View users'),
    ('USER_WRITE', 'User Write', 'Create and update users'),
    ('ROLE_READ', 'Role Read', 'View roles and permissions'),
    ('ROLE_WRITE', 'Role Write', 'Create and update roles and permissions'),
    ('SELLER_READ', 'Seller Read', 'View sellers'),
    ('SELLER_WRITE', 'Seller Write', 'Create and update sellers'),
    ('BRAND_READ', 'Brand Read', 'View brands'),
    ('BRAND_WRITE', 'Brand Write', 'Create and update brands'),
    ('CATEGORY_READ', 'Category Read', 'View categories'),
    ('CATEGORY_WRITE', 'Category Write', 'Create and update categories'),
    ('PRODUCT_READ', 'Product Read', 'View products'),
    ('PRODUCT_WRITE', 'Product Write', 'Create and update products'),
    ('SKU_READ', 'Sku Read', 'View skus'),
    ('SKU_WRITE', 'Sku Write', 'Create and update skus'),
    ('INVENTORY_READ', 'Inventory Read', 'View inventory'),
    ('INVENTORY_WRITE', 'Inventory Write', 'Create and update inventory')
on conflict (code) do nothing;

insert into role_permissions(role_id, permission_id)
select r.id, p.id
from roles r
         join permissions p on r.code = 'SUPER_ADMIN'
on conflict do nothing;

insert into role_permissions(role_id, permission_id)
select r.id, p.id
from roles r
         join permissions p on r.code = 'ADMIN'
where p.code in (
                 'USER_READ', 'USER_WRITE',
                 'ROLE_READ', 'ROLE_WRITE',
                 'SELLER_READ', 'SELLER_WRITE',
                 'BRAND_READ', 'BRAND_WRITE',
                 'CATEGORY_READ', 'CATEGORY_WRITE',
                 'PRODUCT_READ', 'PRODUCT_WRITE',
                 'SKU_READ', 'SKU_WRITE',
                 'INVENTORY_READ', 'INVENTORY_WRITE'
    )
on conflict do nothing;

insert into role_permissions(role_id, permission_id)
select r.id, p.id
from roles r
         join permissions p on r.code = 'SELLER_ADMIN'
where p.code in (
                 'SELLER_READ',
                 'BRAND_READ', 'BRAND_WRITE',
                 'CATEGORY_READ',
                 'PRODUCT_READ', 'PRODUCT_WRITE',
                 'SKU_READ', 'SKU_WRITE',
                 'INVENTORY_READ', 'INVENTORY_WRITE'
    )
on conflict do nothing;

insert into role_permissions(role_id, permission_id)
select r.id, p.id
from roles r
         join permissions p on r.code = 'BUYER'
where p.code in (
                 'CATEGORY_READ',
                 'PRODUCT_READ',
                 'SKU_READ'
    )
on conflict do nothing;
