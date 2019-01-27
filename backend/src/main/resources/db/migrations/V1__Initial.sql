CREATE TABLE albums
(
    id nvarchar PRIMARY KEY NOT NULL,
    path nvarchar NOT NULL,
    lastCheck bigint,
    name nvarchar NOT NULL,
    parentAlbumId nvarchar,
    CONSTRAINT album_to_parent_album___fk FOREIGN KEY (parentAlbumId) REFERENCES albums (id)
);
CREATE UNIQUE INDEX albums_path_uindex ON albums (path);

INSERT INTO albums(id, path, lastCheck, name) VALUES ('root', '', 0, 'root album');
CREATE INDEX albums_parentAlbumId_index ON albums (parentAlbumId);