import * as React from "react";
import { Link } from "react-router-dom"

export interface AlbumPreviewProps {
    album: Album;
}

export default class AlbumPreview extends React.Component<AlbumPreviewProps, any> {
    render() {
        const ref = `/albums/${this.props.album.id}`
        
        return (<div>
            <Link to={ref} >
                Album: {this.props.album.id} - {this.props.album.name}
            </Link>
        </div>)
    }
}
