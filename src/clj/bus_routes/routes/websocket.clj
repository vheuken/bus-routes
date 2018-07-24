(ns bus-routes.routes.websocket
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [taoensso.sente :as sente]
   [taoensso.sente.server-adapters.immutant :refer (get-sch-adapter)]
   [mount.core :refer [defstate] :as mount]
   [clojure.core.async :as async :refer (<! <!! >! >!! put! chan go go-loop)]))


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


(defmethod -event-msg-handler :test/first
  [{:as ev-msg :keys [?reply-fn]}]
  (println (:?data ev-msg)))


(defn start-socket-server! [event-msg-handler]
  (let [{:keys [ch-recv send-fn connected-uids
                ajax-post-fn ajax-get-or-ws-handshake-fn]}
        (sente/make-channel-socket! (get-sch-adapter) {})]
    (def chsk-send! send-fn)
    (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
    (def ring-ajax-post ajax-post-fn)
    (def connected-uids connected-uids)
    {:ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn
     :ring-ajax-post-fn ajax-post-fn
     :connected-uids connected-uids
     :ch-recv ch-recv
     :send-fn send-fn
     :stop-fn (sente/start-chsk-router! ch-recv event-msg-handler)}))

(mount/defstate channel
  :start (start-socket-server! event-msg-handler)
  :stop (:stop-fn channel))


(defroutes websocket-routes
  (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req)))


;; (defn test-fast-server>user-pushes
;;   "Quickly pushes 100 events to all connected users. Note that this'll be
;;   fast+reliable even over Ajax!"
;;   []
;;   (doseq [uid (:any @connected-uids)]
;;     (doseq [i (range 10)]
;;       (chsk-send! uid [:fast-push/is-fast {:data (str "test: " i)}]))))

;; (comment (test-fast-server>user-pushes))
