-- Pedidos criados antes do histórico existir: registra o que se sabe com certeza.
-- A criação (RECEBIDO, por quem criou, na data de criação) e, se o pedido já tinha
-- avançado, o status atual na data da última alteração. Quem fez essa última mudança
-- não foi registrado na época, por isso fica sem autor.

INSERT INTO order_status_history (order_id, status, changed_by_id, changed_at)
SELECT id, 'RECEBIDO', created_by_id, created_at
FROM orders;

INSERT INTO order_status_history (order_id, status, changed_by_id, changed_at)
SELECT id, status, NULL, updated_at
FROM orders
WHERE status <> 'RECEBIDO';
