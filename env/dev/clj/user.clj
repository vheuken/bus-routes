(ns user
  (:require [bus-routes.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]
            [bus-routes.figwheel :refer [start-fw stop-fw cljs]]
            [bus-routes.core :refer [start-app]]
            [bus-routes.db.core]
            [conman.core :as conman]
            [luminus-migrations.core :as migrations]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'bus-routes.core/repl-server))

(defn stop []
  (mount/stop-except #'bus-routes.core/repl-server))

(defn restart []
  (stop)
  (start))

(defn restart-db []
  (mount/stop #'bus-routes.db.core/*db*)
  (mount/start #'bus-routes.db.core/*db*)
  (binding [*ns* 'bus-routes.db.core]
    (conman/bind-connection bus-routes.db.core/*db* "sql/queries.sql")))

(defn reset-db []
  (migrations/migrate ["reset"] (select-keys env [:database-url])))

(defn migrate []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration [name]
  (migrations/create name (select-keys env [:database-url])))


