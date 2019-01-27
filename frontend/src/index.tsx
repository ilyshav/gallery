import * as React from "react";
import {render} from "react-dom";
import {AppContainer} from "react-hot-loader";
import Albums from "./components/Albums";
import {BrowserRouter as Router, Route} from "react-router-dom"
import App from "./components/App";

const rootEl = document.getElementById("root");

render(
    <AppContainer>
        <Router>
            <div>
                <Route exact path="/" component={App} />
                <Route path="/albums" component={Albums} />
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
