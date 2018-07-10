(ns bus-routes.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [bus-routes.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[bus-routes started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[bus-routes has shut down successfully]=-"))
   :middleware wrap-dev})
