create table users
(
    id            bigserial primary key,
    email         varchar(255) not null unique,
    password_hash varchar(255) not null,
    full_name     varchar(255) not null,
    phone         varchar(50),
    status        varchar(50)  not null,
    created_at    timestamp    not null default now(),
    updated_at    timestamp    not null default now()
);

create table roles
(
    id   bigserial primary key,
    code varchar(100) not null unique
);

create table user_roles
(
    user_id bigint not null references users (id),
    role_id bigint not null references roles (id),
    primary key (user_id, role_id)
);

create table sellers
(
    id         bigserial primary key,
    code       varchar(100) not null unique,
    name       varchar(255) not null,
    status     varchar(50)  not null,
    created_at timestamp    not null default now(),
    updated_at timestamp    not null default now()
);

create table brands
(
    id         bigserial primary key,
    seller_id  bigint       not null references sellers (id),
    code       varchar(100) not null unique,
    name       varchar(255) not null,
    slug       varchar(255) not null unique,
    status     varchar(50)  not null,
    created_at timestamp    not null default now(),
    updated_at timestamp    not null default now()
);

create table categories
(
    id         bigserial primary key,
    parent_id  bigint references categories (id),
    name       varchar(255) not null,
    slug       varchar(255) not null unique,
    status     varchar(50)  not null,
    sort_order integer      not null default 0,
    created_at timestamp    not null default now(),
    updated_at timestamp    not null default now()
);

create table products
(
    id                bigserial primary key,
    seller_id         bigint       not null references sellers (id),
    brand_id          bigint references brands (id),
    category_id       bigint       not null references categories (id),
    name              varchar(255) not null,
    slug              varchar(255) not null unique,
    short_description varchar(500),
    description       text,
    status            varchar(50)  not null,
    created_at        timestamp    not null default now(),
    updated_at        timestamp    not null default now()
);

create table skus
(
    id               bigserial primary key,
    product_id       bigint         not null references products (id) on delete cascade,
    sku_code         varchar(100)   not null unique,
    name             varchar(255)   not null,
    price            numeric(18, 2) not null,
    compare_at_price numeric(18, 2),
    status           varchar(50)    not null,
    created_at       timestamp      not null default now(),
    updated_at       timestamp      not null default now()
);

create table inventory
(
    id         bigserial primary key,
    sku_id     bigint    not null unique references skus (id) on delete cascade,
    on_hand    integer   not null default 0,
    reserved   integer   not null default 0,
    available  integer   not null default 0,
    updated_at timestamp not null default now()
);

insert into roles(code)
values ('SUPER_ADMIN'),
       ('SELLER_ADMIN'),
       ('BUYER');