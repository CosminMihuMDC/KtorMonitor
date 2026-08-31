// Patched copy of @cashapp/sqldelight-sqljs-worker/sqljs.worker.js.
//
// The upstream worker hardcodes `locateFile: file => '/sql-wasm.wasm'`, an absolute path resolved against the
// domain root. That breaks when the app is deployed under a sub-path (e.g. https://host/some/sub/path/), since
// the wasm binary actually lives next to this worker chunk, not at the domain root. Resolving `file` relative to
// `self.location.href` (the worker's own script URL) makes this work regardless of the deployment path.
import initSqlJs from "sql.js";

let db = null;
async function createDatabase() {
  let SQL = await initSqlJs({ locateFile: file => new URL(file, self.location.href).href });
  db = new SQL.Database();
}

function onModuleReady() {
  const data = this.data;

  switch (data && data.action) {
    case "exec":
      if (!data["sql"]) {
        throw new Error("exec: Missing query string");
      }

      return postMessage({
        id: data.id,
        results: db.exec(data.sql, data.params)[0] ?? { values: [] }
      });
    case "begin_transaction":
      return postMessage({
        id: data.id,
        results: db.exec("BEGIN TRANSACTION;")
      })
    case "end_transaction":
      return postMessage({
        id: data.id,
        results: db.exec("END TRANSACTION;")
      })
    case "rollback_transaction":
      return postMessage({
        id: data.id,
        results: db.exec("ROLLBACK TRANSACTION;")
      })
    default:
      throw new Error(`Unsupported action: ${data && data.action}`);
  }
}

function onError(err) {
  return postMessage({
    id: this.data.id,
    error: err
  });
}

if (typeof importScripts === "function") {
  db = null;
  const sqlModuleReady = createDatabase()
  self.onmessage = (event) => {
    return sqlModuleReady
      .then(onModuleReady.bind(event))
      .catch(onError.bind(event));
  }
}
