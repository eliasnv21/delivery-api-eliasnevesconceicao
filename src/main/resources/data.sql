-- Dados de exemplo para testes CORRIGIDOS
-- Arquivo: src/main/resources/data.sql

-- Inserir clientes
INSERT INTO clientes (nome, email, telefone, endereco, data_cadastro, ativo) VALUES
('João Silva', 'joao@email.com', '(11) 99999-1111', 'Rua A, 123 - São Paulo/SP', CURRENT_TIMESTAMP, true),
('Maria Santos', 'maria@email.com', '(11) 99999-2222', 'Rua B, 456 - São Paulo/SP', CURRENT_TIMESTAMP, true),
('Pedro Oliveira', 'pedro@email.com', '(11) 99999-3333', 'Rua C, 789 - São Paulo/SP', CURRENT_TIMESTAMP, true);

-- Inserir restaurantes
INSERT INTO restaurantes (nome, categoria, endereco, telefone, taxa_entrega, avaliacao, ativo) VALUES
('Pizzaria Bella', 'Italiana', 'Av. Paulista, 1000 - São Paulo/SP', '(11) 3333-1111', 5.00, 4.5, true),
('Burger House', 'Hamburgueria', 'Rua Augusta, 500 - São Paulo/SP', '(11) 3333-2222', 3.50, 4.2, true),
('Sushi Master', 'Japonesa', 'Rua Liberdade, 200 - São Paulo/SP', '(11) 3333-3333', 8.00, 4.8, true);

-- Inserir produtos
INSERT INTO produtos (nome, descricao, preco, categoria, disponivel, restaurante_id) VALUES
('Pizza Margherita', 'Molho de tomate', 35.90, 'Pizza', true, 1), -- ID 1
('Pizza Calabresa', 'Molho de tomate', 38.90, 'Pizza', true, 1),   -- ID 2
('Lasanha', 'Bolonhesa', 28.90, 'Massa', true, 1),               -- ID 3
('X-Burger', 'Hambúrguer', 18.90, 'Hambúrguer', true, 2),        -- ID 4
('X-Bacon', 'Bacon', 22.90, 'Hambúrguer', true, 2),              -- ID 5
('Batata Frita', 'Fritas', 12.90, 'Acompanhamento', true, 2),    -- ID 6
('Combo Sashimi', '15 peças', 45.90, 'Sashimi', true, 3),        -- ID 7
('Hot Roll', '8 peças', 32.90, 'Hot Roll', true, 3),             -- ID 8
('Temaki', 'Atum', 15.90, 'Temaki', true, 3);                    -- ID 9

-- Inserir pedidos (SEM A COLUNA ITENS)
INSERT INTO pedidos (numero_pedido, data_pedido, status, valor_total, observacoes, cliente_id, restaurante_id) VALUES
('PED1234567890', CURRENT_TIMESTAMP, 'PENDENTE', 54.80, 'Sem cebola', 1, 1), -- Pedido ID 1
('PED1234567891', CURRENT_TIMESTAMP, 'CONFIRMADO', 41.80, '', 2, 2),         -- Pedido ID 2
('PED1234567892', CURRENT_TIMESTAMP, 'ENTREGUE', 78.80, 'Wasabi', 3, 3);     -- Pedido ID 3

-- Inserir Itens do Pedido (Tabela Relacional)
-- Pedido 1 (Pizzaria)
INSERT INTO item_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES (1, 35.90, 35.90, 1, 1);
INSERT INTO item_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES (1, 18.90, 18.90, 1, 2);

-- Pedido 2 (Hamburgueria)
INSERT INTO item_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES (1, 18.90, 18.90, 2, 4);
INSERT INTO item_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES (2, 11.45, 22.90, 2, 6);

-- Pedido 3 (Japa)
INSERT INTO item_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES (1, 45.90, 45.90, 3, 7);
INSERT INTO item_pedido (quantidade, preco_unitario, subtotal, pedido_id, produto_id) VALUES (1, 32.90, 32.90, 3, 8);