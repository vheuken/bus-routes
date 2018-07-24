(ns bus-routes.websocket
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente  :as sente :refer (cb-success?)]
   [taoensso.encore :as encore :refer-macros (have have?)]))


(defn start-websocket []
  (let [{:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket-client! "/chsk" {:type :auto})]

    (defonce chskk chsk)
    (defonce ch-chsk ch-recv) ; ChannelSocket's receive channel
    (defonce chsk-send! send-fn) ; ChannelSocket's send API fn
    (defonce chsk-state state)   ; Watchable, read-only atom
    ))


(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event]}]
  (println "Unhandled event: %s" event))

(defmethod -event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (let [[old-state-map new-state-map] (have vector? ?data)]
    (if (:first-open? new-state-map)
      (println "Channel socket successfully established!: %s" new-state-map)
      (println "Channel socket state change: %s"              new-state-map))))

(defmethod -event-msg-handler :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (println "Push event from server: %s" ?data))

(defmethod -event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (println "Handshake: %s" ?data)))

(defmethod -event-msg-handler :bus-line/coord
  [{:as ev-msg :keys [?data]}]
  (println "Push coord from server: %s" ?data))


;; (chsk-send! [:test/first {:data (str "JEBEM TI MAJKUUU: 10x!!")}])

;; TODO Add your (defmethod -event-msg-handler <event-id> [ev-msg] <body>)s here...

;; (defmethod -event-msg-handler :test/first
;;   [{:as ev-msg :keys [?data]}]
;;   (let [[?uid ?csrf-token ?handshake-data] ?data]
;;     (println "TEST FIRST>>: %s" ?data)))



(defn start-ws []
  (start-websocket)
  (sente/start-client-chsk-router! ch-chsk event-msg-handler))
