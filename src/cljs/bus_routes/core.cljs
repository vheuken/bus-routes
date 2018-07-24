(ns bus-routes.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [bus-routes.ajax :refer [load-interceptors!]]
            [bus-routes.events]
            [secretary.core :as secretary]
            [bus-routes.websocket :as ws])
  (:import goog.History))

(defn nav-link [uri title page]
  [:li.nav-item
   {:class (when (= page @(rf/subscribe [:page])) "active")}
   [:a.nav-link {:href uri} title]])

(defn navbar []
  [:nav.navbar.navbar-dark.bg-primary.navbar-expand-md
   {:role "navigation"}
   [:button.navbar-toggler.hidden-sm-up
    {:type "button"
     :data-toggle "collapse"
     :data-target "#collapsing-navbar"}
    [:span.navbar-toggler-icon]]
   [:a.navbar-brand {:href "#/"} "bus-routes"]
   [:div#collapsing-navbar.collapse.navbar-collapse
    [:ul.nav.navbar-nav.mr-auto
     [nav-link "#/" "Home" :home]
     [nav-link "#/about" "About" :about]]]])

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:img {:src "/img/warning_clojure.png"}]]]])

(defn home-page []
  [:div.container
   [:div.row>div.col-sm-12
    [:h2.alert.alert-info "Tip: try pressing CTRL+H to open re-frame tracing menu"]]
   (when-let [docs @(rf/subscribe [:docs])]
     [:div.row>div.col-sm-12
      [:div {:dangerouslySetInnerHTML
             {:__html (md->html docs)}}]])])



(defn get-route! [bus-line]
  (GET (str "/api/" bus-line)
       {:handler (fn [coord] (do
                               (rf/dispatch [:set-coord coord])
                               (rf/dispatch [:set-error nil])))
        :error-handler #(rf/dispatch [:set-error true])}))

(defn route-page []
  (let [selected (atom nil)]
    [:div
     [:div.container "Choose your route"
      (when-let [error (rf/subscribe [:error])]
        [:div (str "ERROR: " @error)])
      [:select {:on-change #(do
                              (reset! selected (-> % .-target .-value))
                              (rf/dispatch [:remove-coord]))}
       [:option {:value "string"} "string"]
       [:option {:value "string1"} "string1"]]
      [:button.btn.btn-primary {:on-click  #(get-route! @selected)} "send"]
      (when-let [coord (rf/subscribe [:coord])]
        [:div (str @coord)])]
     [:br]
     [:button {:type "button"
               :on-click #(rf/dispatch [:ws {:data "AAAA"}])} "chsk-send! [:test/first]"]]
    ))



(def pages
  {:home #'home-page
   :about #'about-page
   :route #'route-page})

(defn page []
  [:div
   [navbar]
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:navigate :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:navigate :about]))

(secretary/defroute "/route" []
  (rf/dispatch [:navigate :route]))


;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  ;; (rf/dispatch-sync [:navigate :home])
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (ws/start-ws)
  ;; (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
