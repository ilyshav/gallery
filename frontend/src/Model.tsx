class Album {
    id: string
    name: string
}

class Size {
    width: number
    height: number
}

class Photo {
    id: string
    name: string
    thumbnail?: string
    size: Size
}

class AlbumData {
    albums: Album[]
    photos: Photo[]
}