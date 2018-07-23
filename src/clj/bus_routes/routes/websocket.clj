(ns bus-routes.routes.websocket
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [taoensso.sente :as sente]
   [taoensso.sente.server-adapters.immutant :refer (get-sch-adapter)]))


(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )


(defroutes websocket-routes
  (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req)))


(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg) ; Handle event-msgs on a single thread
  ;; (future (-event-msg-handler ev-msg)) ; Handle event-msgs on a thread pool
  )

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (println "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))


;; TODO Add your (defmethod -event-msg-handler <event-id> [ev-msg] <body>)s here...

(defmethod -event-msg-handler :test/first
  [{:as ev-msg :keys [?reply-fn]}]
  (println ev-msg))
