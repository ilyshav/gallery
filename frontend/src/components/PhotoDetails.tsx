import * as React from "react";
import { Api } from "../Api";

export class PhotoDetailsProps {
    photoId: string
}

export default class PhotoDetails extends React.Component<PhotoDetailsProps, any> {
    constructor(props) {
        console.log("TESTS")
        super(props)
    }

    render() {
        console.log("sdasd")
        console.log(this.props)

        const photoPath = Api.buildStaticPath(`/static/photo/${this.props.photoId}`)

        return <img src={photoPath}/>
    }
}