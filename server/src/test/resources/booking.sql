DELETE FROM users;

INSERT INTO users (email, name)
VALUES ('owner@mail.com', 'owner'),
('booker@mail.com', 'booker');

DELETE FROM items;
SET @itemOwnerId = SELECT user_id FROM users WHERE email = 'owner@mail.com';
INSERT INTO items (owner_id, item_name, description, available)
VALUES(@itemOwnerId ,'item', 'description', true);
