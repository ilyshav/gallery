import * as React from "react";
import {BrowserRouter as Router, Route} from "react-router-dom"
import AlbumDetails from "./components/AlbumDetails";
import PhotoDetails from "./components/PhotoDetails";

interface GalleryProps {
    location: string
}

const RootAlbumPage = () => <AlbumDetails albumId="root" />;
const AlbumDetailsPage = ({ match }) => <AlbumDetails albumId={match.params.id} key={match.url}/>;
const PhotoDetailsPage = ({ match }) => <PhotoDetails photoId={match.params.id} />; 

export default class Gallery extends React.Component<GalleryProps, any> {   
    render() {
        return (<Router>
            <div>
                <Route exact path="/" component={RootAlbumPage} />
                <Route exact path="/albums/:id" component={AlbumDetailsPage} />
                <Route exact path="/photo/:id" component={PhotoDetailsPage} />
            </div>
        </Router>)
    }
}

