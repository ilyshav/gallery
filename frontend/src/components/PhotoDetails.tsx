import * as React from "react";

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

        return <div>{this.props.photoId}</div>
    }
}