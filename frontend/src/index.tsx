import * as React from "react";
import {render} from "react-dom";
import {AppContainer} from "react-hot-loader";
import Albums from "./components/Albums";

const rootEl = document.getElementById("root");

render(
    <AppContainer>
        <Albums/>
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
