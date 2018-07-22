(ns bus-routes.ws
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.immutant :refer (get-sch-adapter)]
            [compojure.core :refer [defroutes GET POST]]))


(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )


(defroutes app-routes
  (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req))
  )


(defn broadcast
  "Broadcast data to all connected clients"
  [data]
  (doseq [uid (:any @connected-uids)]
    (chsk-send! uid data)))


(defonce shared-db (atom {:count 0}))

                                        ; Watch for changes to the shared DB and broadcast a diff to them.
(add-watch shared-db :watch
           (fn [k reference old-state new-state]
             (broadcast [:state/diff (- old-state new-state)])))


(defmulti event-msg-handler :id)

(defn event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
  (event-msg-handler ev-msg))

(defmethod event-msg-handler :default
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (println (format "Unhandled event: %s %s" event ?data)))

(defmethod event-msg-handler :chsk/ws-ping [ev-msg]
  )

(defmethod event-msg-handler :state/sync [ev-msg]
  (broadcast [:state/sync @shared-db]))

(defmethod event-msg-handler :counter/incr
                                        ; Increment the counter locally.
  ;;The watcher will push the state to clients.
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (reset! shared-db (update-in @shared-db [:count] + (:delta ?data))))
