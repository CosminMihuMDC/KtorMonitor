// {project}/webpack.config.d/sqljs.js
const path = require('path');

config.resolve = {
    modules: [
        'node_modules',
        // Needed so that the aliased sqljs.worker.patched.js (which lives outside the generated npm
        // package directory) can still resolve its own `import initSqlJs from "sql.js"` dependency.
        path.resolve(__dirname, '../../node_modules'),
    ],
    fallback: {
        fs: false,
        path: false,
        crypto: false,
    },
    alias: {
        ...(config.resolve && config.resolve.alias),
        // The published worker hardcodes an absolute, domain-root path ('/sql-wasm.wasm') for locating the
        // sql.js wasm binary, which breaks when the app is deployed under a sub-path. Redirect webpack's
        // bundling of `new Worker(new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url))`
        // (in DatabaseDriverFactory.kt) to a patched copy that resolves the file relative to the worker's own
        // location instead, so it works regardless of the deployment path.
        '@cashapp/sqldelight-sqljs-worker/sqljs.worker.js': path.resolve(__dirname, '../../../../sample/ktor/webApp/webpack-resources/sqljs.worker.patched.js'),
    },
};

const CopyWebpackPlugin = require('copy-webpack-plugin');
config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            '../../node_modules/sql.js/dist/sql-wasm.wasm'
        ]
    })
);