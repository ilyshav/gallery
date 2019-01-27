import * as React from "react";
import {render} from "react-dom";
import {AppContainer} from "react-hot-loader";
import AlbumDetails from "./components/AlbumDetails"
import PhotoDetails from "./components/PhotoDetails";
import Gallery from "./Gallery";

const rootEl = document.getElementById("root");

render(
    <AppContainer>
        <Gallery location=""/>
    </AppContainer>,
    rootEl
);

// Hot Module Replacement API
declare let module: { hot: any };

// if (module.hot) {
//     module.hot.accept("./components/Albums", () => {
//         const Albums = require("./components/Albums").default;

//         render(
//             <AppContainer>
//                 <AlbumDetails albumId="root"/>
//             </AppContainer>,
//             rootEl
//         );
//     });
// }
