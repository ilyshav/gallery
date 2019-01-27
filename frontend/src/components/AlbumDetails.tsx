import * as React from "react";
import { Api } from "../Api";
import PhotoPreview from "./PhotoPreview";
import AlbumPreview from "./AlbumPreview";

export interface AlbumProps {
    albumId: string;
}

interface State {
    isLoading: boolean
    isError: boolean
    photos: Photo[]
    albums: Album[]
}

export default class AlbumDetails extends React.Component<AlbumProps, State> {
    state = {
        isLoading: true,
        isError: false,
        photos: [],
        albums: []
    }

    constructor(props: AlbumProps) {
        super(props)

        console.log("album details")
        console.log(props)

        fetch(Api.buildPath(`/albums/${props.albumId}`))
            .then(r => r.json())
            .then(data => {
                this.setState({photos: data.photos, albums: data.albums, isLoading: false})
            })
            .catch(err => {
                console.log(err) 
                this.setState({isError: true, isLoading: false})
            }
            )
    }

    renderLoaded() {
        return (<div>
            <b>Photos</b>
            {this.state.photos.map(photo => <PhotoPreview photo={photo} key={photo.id}/>)}
            <b>Albums</b>
            {this.state.albums.map(album => <AlbumPreview album={album} key={album.id}/>)}
        </div>)
    }

    render() {
        if (this.state.isLoading) return <div>loading</div>
        else if (this.state.isError) return <div>error</div>
        else return this.renderLoaded()
    }
}