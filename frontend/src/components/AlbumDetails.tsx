import * as React from "react";
import { Api } from "../Api";
import PhotoPreview from "./PhotoPreview";

export interface AlbumProps {
    albumId: string;
}

interface State {
    isLoading: boolean
    isError: boolean
    photos: Photo[]
}

export default class AlbumDetails extends React.Component<AlbumProps, State> {
    state = {
        isLoading: true,
        isError: false,
        photos: []
    }

    constructor(props: AlbumProps) {
        super(props)

        fetch(Api.buildPath(`/photos/${props.albumId}`))
            .then(r => r.json())
            .then(data => {
                this.setState({photos: data, isLoading: false})
            })
            .catch(err => {
                console.log(err) 
                this.setState({isError: true, isLoading: false})
            }
            )
    }

    renderLoaded() {
        if (this.state.photos.length == 0) return (<div>pusto</div>)
        else return this.state.photos.map(photo => <PhotoPreview photo={photo} key={photo.id}/>)
    }

    render() {
        if (this.state.isLoading) return <div>loading</div>
        else if (this.state.isError) return <div>error</div>
        else return this.renderLoaded()
    }
}