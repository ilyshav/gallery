import * as React from "react";
import { Link } from "react-router-dom"

export interface PhotoPreviewProps {
    photo: Photo
}

export default class PhotoPreview extends React.Component<PhotoPreviewProps, any> {
    render() {
        const ref = `/photo/${this.props.photo.id}`

        return (
            <div>
                <Link to={ref}>
                    ${this.props.photo.id} - {this.props.photo.name}
                </Link>
            </div>
        )
    }
}