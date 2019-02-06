CREATE TABLE thumbnails
(
    id nvarchar PRIMARY KEY NOT NULL,
    realPath varchar NOT NULL
);

CREATE TABLE photos
(
    id nvarchar PRIMARY KEY NOT NULL,
    realPath nvarchar,
    albumId nvarchar,
    width int NOT NULL,
    height int NOT NULL,
    thumbnailId nvarchar,
    CONSTRAINT photos_to_albums___fk FOREIGN KEY (albumId) REFERENCES albums (id),
    CONSTRAINT photos_to_thumbnail___fk FOREIGN KEY (thumbnailId) REFERENCES thumbnail (id)
);
CREATE UNIQUE INDEX photos_id_uindex ON photos (realPath);