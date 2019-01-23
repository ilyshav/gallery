CREATE TABLE albums
(
    id nvarchar PRIMARY KEY NOT NULL,
    path nvarchar NOT NULL,
    lastCheck bigint,
    name nvarchar NOT NULL
);
CREATE UNIQUE INDEX albums_path_uindex ON albums (path);