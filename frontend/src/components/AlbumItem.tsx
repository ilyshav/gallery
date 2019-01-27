import * as React from "react";

export interface AlbumProps {
    album: Album;
}

export default class AlbumItem extends React.Component<AlbumProps, any> {
    render() {
        return (<div>
            Album: {this.props.album.id} - {this.props.album.name}
        </div>)
    }
}
