import * as React from "react";
import { isError } from "util";

export interface AppProps {
}

export interface AlbumsState {
    isLoading: boolean;
    isError: boolean;
    albums: Album[];
}

class Album {
    id: string
    name: string
}

export default class App extends React.Component<AppProps, AlbumsState> {
    state = {
        isLoading: true,
        isError: false,
        albums: []
    }

    constructor(props) {
        super(props)
        const currentHost = location.protocol + '//' + location.host
        console.log(currentHost + "/api/albums")
        fetch(currentHost + "/api/albums")
            .then (r => r.json())
            .then (data => {
                this.setState({albums: data as Album[], isLoading: false})
            })
            .catch(err => {
                console.log(err)
                this.setState({isError: true})
            })
    }

    loading() {
        return (
            <div>Loading albums</div>
        )
    }

    error() {
        return (
            <div>Error</div>
        )
    }

    loaded() {
        return (
            <div>
                Albums. Loaded: {this.state.albums.length}
            </div>
        )
    }

    render() {
        if (this.state.isError) return this.error()
        else if (this.state.isLoading) return this.loading()
        else return this.loaded()
    }
}
