CREATE TABLE albums
(
    id integer PRIMARY KEY AUTOINCREMENT NOT NULL,
    path nvarchar NOT NULL,
    lastCheck bigint
);
CREATE UNIQUE INDEX albums_path_uindex ON albums (path);