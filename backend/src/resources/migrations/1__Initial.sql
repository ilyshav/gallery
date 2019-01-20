CREATE TABLE albums
(
    id bigint PRIMARY KEY AUTOINCREMENT NOT NULL,
    path nvarchar NOT NULL,
    lastCheck bigint
);