-- Esquema inicial, idêntico ao que o Hibernate gerava com ddl-auto: update.
-- Bancos que já existiam antes do Flyway são adotados a partir desta versão (baseline-on-migrate),
-- sem recriar nada; bancos novos são criados por este script.

CREATE TABLE users (
    id         integer,
    created_at timestamp    not null,
    email      varchar(255) not null unique,
    name       varchar(100) not null,
    password   varchar(255) not null,
    primary key (id)
);

CREATE TABLE orders (
    id             integer,
    created_at     timestamp    not null,
    customer_name  varchar(255) not null,
    customer_phone varchar(255) not null,
    city           varchar(255) not null,
    complement     varchar(255),
    neighborhood   varchar(255) not null,
    number         varchar(255) not null,
    street         varchar(255) not null,
    zip_code       varchar(8)   not null,
    status         varchar(20)  not null check (status in ('RECEBIDO', 'EM_PREPARO', 'SAIU_PARA_ENTREGA', 'ENTREGUE', 'CANCELADO')),
    updated_at     timestamp    not null,
    created_by_id  bigint       not null,
    primary key (id)
);

CREATE TABLE order_items (
    id         integer,
    name       varchar(255)   not null,
    quantity   integer        not null,
    unit_price numeric(10, 2) not null,
    order_id   bigint         not null,
    primary key (id)
);
