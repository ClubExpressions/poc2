(ns simple.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [webpack.bundle]
            [cljs.pprint :refer [pprint]]))

;; A detailed walk-through of this source code is provied in the docs:
;; https://github.com/Day8/re-frame/blob/master/docs/CodeWalkthrough.md

;; -- Domino 1 - Event Dispatch -----------------------------------------------

;; deleted timed dispatch

;; -- Domino 2 - Event Handlers -----------------------------------------------

(rf/reg-event-db              ;; sets up initial application state
  :initialize                 ;; usage:  (dispatch [:initialize])
  (fn [_ _]                   ;; the two parameters are not important here, so use _
    {:latex-src "(Somme 2 2)"}))

(rf/reg-event-fx
  :latex-src-change
  (fn [{:keys [db]} [_ new-value]]
    {:db (assoc db :latex-src new-value)
     :kinto-log-user-attempt new-value}))

;; -- Domino 3 - Effects Handlers  --------------------------------------------

(def collec
  (let [kinto (aget js/window "deps" "kinto")
        k (new kinto)]
    (.collection k "history")))

(rf/reg-fx
   :kinto-log-user-attempt
   (fn [value]
     (.create collec (clj->js {:user-attempt value}))
     (println (str "kinto fx handler: " value))
     ))


;; -- Domino 4 - Query  -------------------------------------------------------

(rf/reg-sub
  :latex-src
  (fn [db _]
    (:latex-src db)))


;; -- Domino 5 - View Functions ----------------------------------------------

(defn expr
  [src]
  (let [react-mathjax (aget js/window "deps" "react-mathjax")
        ctx (aget react-mathjax "Context")
        node (aget react-mathjax "Node")
        clubexpr (aget js/window "deps" "clubexpr")
        renderLispAsLaTeX (.-renderLispAsLaTeX clubexpr)
        ]
    [:> ctx [:> node {:inline true} (renderLispAsLaTeX src)]]))

(defn src-input
  []
  [:div
   "Code Club: "
   [:input {:type "text"
            :value @(rf/subscribe [:latex-src])
            :on-change #(rf/dispatch [:latex-src-change (-> % .-target .-value)])}]])

(defn ui
  []
   [:div
    (when false [:pre (with-out-str (pprint @app-db))])
    [:h1 "POC Club des Expressions"]
    [src-input]
    [:div "Formatted expr: "
      [expr @(rf/subscribe [:latex-src])]]])

;; -- Entry Point -------------------------------------------------------------

(defn ^:export run
  []
  (rf/dispatch-sync [:initialize])     ;; puts a value into application state
  (reagent/render [ui]              ;; mount the application's ui into '<div id="app" />'
                  (js/document.getElementById "app")))

