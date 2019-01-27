import * as React from "react";
import {render} from "react-dom";
import {AppContainer} from "react-hot-loader";
import Albums from "./components/Albums";
import {BrowserRouter as Router, Route} from "react-router-dom"
import App from "./components/App";
import AlbumDetails from "./components/AlbumDetails"
import PhotoDetails from "./components/PhotoDetails";

const rootEl = document.getElementById("root");

const AlbumDetailsPage = ({ match }) => <AlbumDetails albumId={match.params.id} />;
const PhotoDetailsPage = ({ match }) => <PhotoDetails photoId={match.params.id} />;

render(
    <AppContainer>
        <Router>
            <div>
                <Route exact path="/" component={App} />
                <Route exact path="/albums" component={Albums} />
                <Route path="/albums/:id" component={AlbumDetailsPage} />
                <Route path="/photo/:id" component={PhotoDetailsPage} />
            </div>
        </Router>
    </AppContainer>,
    rootEl
);

// Hot Module Replacement API
declare let module: { hot: any };

if (module.hot) {
    module.hot.accept("./components/Albums", () => {
        const Albums = require("./components/Albums").default;

        render(
            <AppContainer>
                <Albums/>
            </AppContainer>,
            rootEl
        );
    });
}
