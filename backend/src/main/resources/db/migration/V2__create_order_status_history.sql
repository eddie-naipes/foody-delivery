-- Linha do tempo do pedido: cada mudança de status (inclusive a criação, como RECEBIDO)
-- vira uma linha com quem fez e quando.

CREATE TABLE order_status_history (
    id            integer,
    order_id      bigint      not null references orders (id),
    status        varchar(20) not null check (status in ('RECEBIDO', 'EM_PREPARO', 'SAIU_PARA_ENTREGA', 'ENTREGUE', 'CANCELADO')),
    changed_by_id bigint      references users (id),
    changed_at    timestamp   not null,
    primary key (id)
);

CREATE INDEX idx_order_status_history_order ON order_status_history (order_id);

-- A listagem filtra por status e ordena por data de criação
CREATE INDEX idx_orders_status_created_at ON orders (status, created_at);
CREATE INDEX idx_orders_created_at ON orders (created_at);
CREATE INDEX idx_order_items_order ON order_items (order_id);
