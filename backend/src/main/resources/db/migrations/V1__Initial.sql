CREATE TABLE albums
(
    id integer PRIMARY KEY AUTOINCREMENT NOT NULL,
    path nvarchar NOT NULL,
    lastCheck bigint
);