(ns bus-routes.routes.bus-stop
  (:require [schema.core :as s]
            [ring.util.http-response :as response]
            [clojure.tools.logging :as log]))

;;https://github.com/yogthos/memory-hole/blob/master/src/clj/memory_hole/routes/services/groups.clj#L15
;;pokupi foru za error u schema
(s/defschema Coord
  {:bus-line String
   :current-lat String
   :current-long String
   :timestamp Long})

(s/defschema BusLineCoord
  {(s/optional-key :coord) Coord
   (s/optional-key :error) s/Str})

(defonce all-coords (atom []))

(defn latest-coord [bus-line]
  (last (filterv #(= bus-line (:bus-line %)) @all-coords)))

(defn get-coord [bus-line]
  (let [latest-coord (latest-coord bus-line)
        _ (log/info latest-coord)]
    (if (nil? latest-coord)
      (response/precondition-failed {:error "no coords"})
      (response/ok {:coord latest-coord}))))

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


;;TODO - uradi anti forgery preko websockta, tako da ne moras da brinese o potpisivanju
