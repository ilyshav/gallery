class Album {
    id: string
    name: string
}

class Photo {
    id: string
    name: string
    thumbnail?: string
}

class AlbumData {
    albums: Album[]
    photos: Photo[]
}