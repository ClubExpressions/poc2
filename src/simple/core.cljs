(ns simple.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [goog.object :refer [getValueByKeys]]
            [webpack.bundle]
            [cljs.pprint :refer [pprint]]))

(enable-console-print!)

;; A detailed walk-through of this source code is provied in the docs:
;; https://github.com/Day8/re-frame/blob/master/docs/CodeWalkthrough.md

;; -- Domino 1 - Event Dispatch -----------------------------------------------

;; deleted timed dispatch

;; -- Domino 2 - Event Handlers -----------------------------------------------

(rf/reg-event-db              ;; sets up initial application state
  :initialize                 ;; usage:  (dispatch [:initialize])
  (fn [_ _]                   ;; the two parameters are not important here, so use _
    {:latex-src "(Somme 2 2)"
     :history-count 0}))

(rf/reg-event-fx
  :error
  (fn [_ [_ [kw error]]]
    (println (str "ERROR!!!1! in " kw))
    (println (with-out-str (pprint error)))))

(rf/reg-event-fx
  :latex-src-change
  (fn [{:keys [db]} [_ new-value]]
    {:db (assoc db :latex-src new-value)
     :kinto-log-user-attempt new-value}))

(rf/reg-event-fx
  :request-history-count
  (fn [{db :db} _]
    {:kinto-get-count nil
     :db db}))  ; we could have set a 'loading?' flag in app-db as in the docs

(rf/reg-event-fx
  :sync-history
  (fn [{db :db} _]
    {:sync-history nil
     :db db}))  ; we could have set a 'syncing?' flag in app-db as in the docs

(rf/reg-event-db
  :result-get-count
  (fn [db [_ value]]
    (let [data (:data (js->clj value :keywordize-keys true))]
      (assoc db :history-count (count data)))))


;; -- Domino 3 - Effects Handlers  --------------------------------------------

(def sync-options
  (let [b64 (js/window.btoa "user:pass")
        url "https://kinto.dev.mozaws.net/v1"]
    (clj->js {:remote url
              :headers {:Authorization (str "Basic " b64)}})))
(def collec
  (let [kinto (getValueByKeys js/window "deps" "kinto")
        k (new kinto)]
    (.collection k "history")))

(rf/reg-fx
   :kinto-log-user-attempt
   (fn [value]
     (.create collec (clj->js {:user-attempt value}))
     (println (str "kinto fx handler: " value))
     ))

(rf/reg-fx
   :kinto-get-count
   (fn []
     [(.. (.list collec)
          (then #(rf/dispatch [:result-get-count %]))
          (catch #(rf/dispatch [:error [:kinto-get-count %]])))]))

; https://stackoverflow.com/questions/32467299/clojurescript-convert-arbitrary-javascript-object-to-clojure-script-map
(defn jsx->clj
  [x]
  (into {} (for [k (.keys js/Object x)] [k (getValueByKeys x k)])))

(rf/reg-fx
   :sync-history
   (fn []
    [(.. (.sync collec sync-options)
         (then #(do (print "Sync returned:")
                    (pprint (jsx->clj %))))
         (catch #(rf/dispatch [:error [:kinto-sync %]])))]))


;; -- Domino 4 - Query  -------------------------------------------------------

(rf/reg-sub
  :latex-src
  (fn [db _]
    (:latex-src db)))

(rf/reg-sub
  :history-count
  (fn [db _]
    (:history-count db)))


;; -- Domino 5 - View Functions ----------------------------------------------

(defn expr
  [src]
  (let [react-mathjax (getValueByKeys js/window "deps" "react-mathjax")
        ctx (getValueByKeys react-mathjax "Context")
        node (getValueByKeys react-mathjax "Node")
        clubexpr (getValueByKeys js/window "deps" "clubexpr")
        renderLispAsLaTeX (.-renderLispAsLaTeX clubexpr)
        ]
    [:div
      [:> ctx [:> node {:inline true} (renderLispAsLaTeX src)]]]))

(defn src-input
  []
  [:div.color-input
   "Code Club: "
   [:input {:type "text"
            :value @(rf/subscribe [:latex-src])
            :on-change #(rf/dispatch [:latex-src-change (-> % .-target .-value)])}]])

(defn request-history-count-button
  []
  [:button
     {:on-click #(rf/dispatch [:request-history-count])}
     "Update history count"])

(defn sync-history-button
  []
  [:button
     {:on-click #(rf/dispatch [:sync-history])}
     "Sync history"])

(defn ui
  []
   [:div
    (when false [:pre (with-out-str (pprint @app-db))])
    [:h1 "POC Club des Expressions"]
    [src-input]
    [:div "Formatted expr: "
      [expr @(rf/subscribe [:latex-src])]]
    [:div "History count: "
      @(rf/subscribe [:history-count])]
    [request-history-count-button]
    [sync-history-button]
    ])

;; -- Entry Point -------------------------------------------------------------

(defn ^:export run
  []
  (rf/dispatch-sync [:initialize])     ;; puts a value into application state
  (reagent/render [ui]              ;; mount the application's ui into '<div id="app" />'
                  (js/document.getElementById "app")))

