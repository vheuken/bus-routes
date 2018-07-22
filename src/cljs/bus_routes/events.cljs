(ns bus-routes.events
  (:require [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [bus-routes.db :as db]
            [bus-routes.ws :as ws]))

;;dispatchers

(reg-event-db
 :initialize-db
 (fn [_ _]
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
 :increment-count
 (fn [db [_ delta]]
   (ws/chsk-send! [:counter/incr {:delta delta}])
   (update-in db [:shared :count] + delta)))


(reg-event-db
 :ws/connected
 (fn [db [_ connected?]]
   (if connected?
     (ws/chsk-send! [:state/sync]))
   (assoc db :ws/connected connected?)))


(reg-event-db
 :ws/send
 (fn [db [_  command & data]]
   (ws/chsk-send! [command data])))


(reg-event-db
 :state/sync
 (fn [db [_ new-db]]
   (assoc db :shared new-db)))


(reg-event-db
 :state/diff
 (fn [db [_ diff]]
   (assoc db :shared (- (:shared db) diff))))

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
 :name
 (fn [db]
   (:name db)))


(reg-sub
 :count
 (fn [db]
   (:count (:shared db))))

(reg-sub
 :ws/connected
 (fn [db]
   (:ws/connected db)))
