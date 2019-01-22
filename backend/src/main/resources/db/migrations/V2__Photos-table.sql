CREATE TABLE photos
(
    id nvarchar PRIMARY KEY NOT NULL,
    realPath nvarchar,
    albumId nvarchar,
    CONSTRAINT photos_to_albums___fk FOREIGN KEY (albumId) REFERENCES albums (id)
);
CREATE UNIQUE INDEX photos_id_uindex ON photos (id);