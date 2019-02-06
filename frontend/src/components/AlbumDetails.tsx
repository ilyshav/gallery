import * as React from "react";
import { Api } from "../Api";
import PhotoPreview from "./PhotoPreview";
import AlbumPreview from "./AlbumPreview";

import 'react-photoswipe/lib/photoswipe.css';
import {PhotoSwipeGallery} from 'react-photoswipe';


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

        fetch(Api.buildPath(`/albums/${props.albumId}`))
            .then(r => r.json())
            .then(data => {
                console.log(data)
                this.setState({photos: data.photos, albums: data.albums, isLoading: false})
            })
            .catch(err => {
                console.log(err) 
                this.setState({isError: true, isLoading: false})
            }
            )
    }

    renderLoaded() {
        const photos = this.state.photos.map(photo => (
            {
                src: Api.buildStaticPath(`/static/photo/${photo.id}`),
                w: photo.size.width,
                h: photo.size.height,
                thumbnail: Api.buildStaticPath(`/static/thumbnail/${photo.thumbnail}`),
            }
        ))

        const getThumbnailContent = (item) => {
            return (
              <img src={item.thumbnail} width={200} height={200}/>
            );
          }

        return (<div>
            <b>Albums</b>
            {this.state.albums.map(album => <AlbumPreview album={album} key={album.id}/>)}
            <PhotoSwipeGallery items={photos} options={{}} thumbnailContent={getThumbnailContent}/>
          </div>)
    }

    render() {
        if (this.state.isLoading) return <div>loading</div>
        else if (this.state.isError) return <div>error</div>
        else return this.renderLoaded()
    }
}