**How to run:**

docker run 
-v <PATH_TO_GALLERY>:/data/gallery 
-v <PATH_TO_DB_DIRECTORY>:/data/database  
-v <PATH_FOR_THUMBNAILS>:/data/thumbnails
-p <HTTP_PORT>:80 ilyavasin/gallery:latest