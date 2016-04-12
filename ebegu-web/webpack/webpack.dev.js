var loaders = require('./loaders');
var BrowserSyncPlugin = require('browser-sync-webpack-plugin');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var webpack = require('webpack');
var helpers = require('./helpers');

/**
 * Webpack Constants
 */
const ENV = process.env.ENV = process.env.NODE_ENV = 'development';
const HMR = helpers.hasProcessFlag('hot');
const METADATA = {
    title: 'ebegu Webpack',
    baseUrl: '/',
    host: 'localhost',
    port: 3000,
    ENV: ENV,
    HMR: HMR
};

module.exports = {
    // Static metadata for index.html
    //
    // See: (custom attribute)
    metadata: METADATA,

    // Switch loaders to debug mode.
    //
    // See: http://webpack.github.io/docs/configuration.html#debug
    debug: true,

    entry: ['./src/core/bootstrap.ts'],
    // entry: ['webpack/hot/dev-server', '/src/app.module.ts'],
    output: {
        filename: 'build.js',
        path: 'dist'
    },
    resolve: {
        root: __dirname,
        extensions: ['', '.ts', '.js', '.json']
    },
    resolveLoader: {
        modulesDirectories: ['node_modules']
    },
    devtool: 'source-map',
    plugins: [
        new HtmlWebpackPlugin({
            template: './src/index.html',
            inject: 'body',
            hash: true
        }),
        // new BrowserSyncPlugin({
        //     host: 'localhost',
        //     port: 8080,
        //     server: {
        //         baseDir: 'dist'
        //     },
        //     ui: false,
        //     online: false,
        //     notify: false
        // }),
        new webpack.ProvidePlugin({
            $: 'jquery',
            jQuery: 'jquery',
            'window.jQuery': 'jquery',
            'window.jquery': 'jquery'
        })
    ],

    // Webpack Development Server configuration
    // Description: The webpack-dev-server is a little node.js Express server.
    // The server emits information about the compilation state to the client,
    // which reacts to those events.
    //
    // See: https://webpack.github.io/docs/webpack-dev-server.html
    devServer: {
        port: METADATA.port,
        host: METADATA.host,
        historyApiFallback: true,
        watchOptions: {
            aggregateTimeout: 300,
            poll: 1000
        }
    },

    module: {
        loaders: loaders
    }
};
