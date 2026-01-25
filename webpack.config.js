const path = require('path');
const webpack = require('webpack');

module.exports = {
  mode: 'production',
  entry: './webApp/build/wasm/packages/CatJumpsBarrels-webApp/kotlin/CatJumpsBarrels-webApp.mjs',
  output: {
    filename: 'catjumpbarrels.js',
    path: path.resolve(__dirname, 'dist'),
    library: 'CatJumpsBarrels',
    libraryTarget: 'umd'
  },
  module: {
    rules: [
      {
        test: /\.wasm$/,
        type: 'webassembly/async'
      }
    ]
  },
  experiments: {
    asyncWebAssembly: true,
    topLevelAwait: true
  },
  resolve: {
    extensions: ['.mjs', '.js', '.wasm']
  },
  devServer: {
    port: 8080,
    static: {
      directory: path.join(__dirname, 'dist'),
    },
    compress: true,
    hot: false,
    client: false
  }
};
