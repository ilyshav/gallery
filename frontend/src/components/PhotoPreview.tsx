import * as React from "react";
import { Link } from "react-router-dom"
import { Api } from "../Api";

export interface PhotoPreviewProps {
    photo: Photo
}

export default class PhotoPreview extends React.Component<PhotoPreviewProps, any> {
    render() {
        const ref = `/photo/${this.props.photo.id}`

        if (this.props.photo.thumbnail != undefined) {
            return (<div>
                <Link to={ref}>
                    <img src={Api.buildStaticPath(`/static/thumbnail/${this.props.photo.thumbnail}`)} />
                </Link>
            </div>)
        }  else {
           return (<div>
                <Link to={ref}>
                    ${this.props.photo.id} - {this.props.photo.name}
                </Link>
            </div>)
        }
    }
}