CREATE TABLE albums
(
    id nvarchar PRIMARY KEY NOT NULL,
    path nvarchar NOT NULL,
    lastCheck bigint
);
CREATE UNIQUE INDEX albums_path_uindex ON albums (path);