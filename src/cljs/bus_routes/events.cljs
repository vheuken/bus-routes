(ns bus-routes.events
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [bus-routes.db :as db]
            [bus-routes.websocket :as ws]
            [taoensso.sente  :as sente :refer (cb-success?)]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

;;dispatchers

(reg-event-db
 :initialize-db
 (fn [_]
   db/default-db))


(reg-event-db
  :navigate
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))


(reg-event-db
 :set-coord
 (fn [db [_ coord]]
   (assoc db :coord coord)))

(reg-event-db
 :set-error
 (fn [db [_ error]]
   (assoc db :error error)))

(reg-event-db
 :remove-coord
 (fn [db [_ _]]
   (dissoc db :coord)))



(reg-event-db
 :ws
 (fn [db [_ data]]
   (ws/chsk-send! [:test/first data])
   (assoc db :sent "SENT")))

(reg-event-db
 :bus-line/sub-to
 (fn-traced [db [_ bus-line]]
   (ws/chsk-send! [:bus-line/sub-to {:bus-line bus-line}]
                  3000
                  (fn [reply]      ; Reply is arbitrary Clojure data
                    (if (sente/cb-success? reply) ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
                      (do
                        (println reply)
                        (dispatch [:bus-line/coord reply]))
                      (js/alert "No reply"))))
   (assoc db :bus-line/sub-to bus-line)))

(reg-event-db
 :bus-line/coord
 (fn-traced [db [_ coord]]
            (assoc db :bus-line/coord coord)))



;;subscriptions

(reg-sub
  :page
  (fn [db _]
    (:page db)))

(reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(reg-sub
 :coord
 (fn [db _]
   (:coord db)))

(reg-sub
 :error
 (fn [db _]
   (:error db)))

(reg-sub
 :bus-line/coord
 (fn [db _]
   (:bus-line/coord db)))
