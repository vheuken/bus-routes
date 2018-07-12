(ns bus-routes.routes.bus-stop
  (:require [schema.core :as s]
            [ring.util.http-response :as response]
            [clojure.tools.logging :as log]))


(s/defschema Coord
  {:bus-line String
   :current-lat String
   :current-long String
   :timestamp Long})

(defonce all-coords (atom []))


(defn get-coord [bus-line]
  (let [latest-coord (last (filterv #(= bus-line (:bus-line %)) @all-coords))
        _ (log/info latest-coord)]
    (response/ok latest-coord)))

(defn receive-coord [coord]
  (try
    (swap! all-coords conj coord)
    (log/info "Saved coord: " coord)
    (-> {:result :ok}
       (response/ok))
    (catch Exception e
      (response/precondition-failed {:result :error
                                     :message "Coord not accepted"}))))


;;TODO - namesti da radi lokalno postovanj iz mockupa
