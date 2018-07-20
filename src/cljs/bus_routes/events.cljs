(ns bus-routes.events
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [bus-routes.db :as db]))

;;dispatchers
;; (reg-event-db
;;  :initialize-db
;;  (fn [_ _]
;;    db/default-db))


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
