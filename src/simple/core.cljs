(ns simple.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [goog.object :refer [getValueByKeys]]
            [webpack.bundle]
            [cljs.pprint :refer [pprint]]))

(def debug?
  ^boolean goog.DEBUG)
(enable-console-print!)

;; A detailed walk-through of this source code is provied in the docs:
;; https://github.com/Day8/re-frame/blob/master/docs/CodeWalkthrough.md

;; -- Domino 1 - Event Dispatch -----------------------------------------------

;; deleted timed dispatch

;; -- Domino 2 - Event Handlers -----------------------------------------------

(rf/reg-event-db              ;; sets up initial application state
  :initialize                 ;; usage:  (dispatch [:initialize])
  (fn [db _]                  ;; the two parameters are not important here, so use _
    (if (empty? db)
      {:latex-src "(Somme 2 2)"
       :history-count 0}
      db)))

(rf/reg-event-fx
  :error
  (fn [_ [_ [kw error]]]
    (println (str "ERROR!!!1! in " kw))
    (println (with-out-str (pprint error)))))

(rf/reg-event-fx
  :login
  (fn [{:keys [db]} []]
    {:db db :login nil}))

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

(rf/reg-event-fx
  :kinto-reset-sync-status
  (fn []
    {:kinto-reset-sync-status nil}))

(rf/reg-event-db
  :result-get-count
  (fn [db [_ value]]
    (let [data (:data (js->clj value :keywordize-keys true))]
      (assoc db :history-count (count data)))))


;; -- Domino 3 - Effects Handlers  --------------------------------------------

(def webauth
  (let [auth0 (getValueByKeys js/window "deps" "auth0")
        opts (clj->js {:domain "clubexpr.eu.auth0.com"
                       :clientID "QKq48jNZqQ84VQALSZEABkuUnM74uHUa"
                       :responseType "token id_token"
                       :redirectUri (str (.-location js/window))
                       })]
    (new auth0.WebAuth opts)))

(rf/reg-fx
   :login
   (fn [_]
     (.authorize webauth)))

(def sync-options
  (let [b64 (js/window.btoa "user:pass")
        url (if debug? "http://localhost:8887/v1"
                       "https://kinto.dev.mozaws.net/v1")] ; a real one soon
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

(def kinto-network-error "NetworkError when attempting to fetch resource.")
(def kinto-flushed-error "Server has been flushed.")

(rf/reg-fx
   :sync-history
   (fn []
    [(.. (.sync collec sync-options)
         (then #(do (print "Sync returned:")
                    (pprint (jsx->clj %))))
         (catch #(let [msg (.-message %)]
                   (cond
                     (= msg kinto-network-error)
                       (rf/dispatch [:error [:kinto-network-error %]])
                     (= msg kinto-flushed-error)
                       (rf/dispatch [:kinto-reset-sync-status])
                     :else (rf/dispatch [:error [:kinto-sync %]])))))]))

(rf/reg-fx
   :kinto-reset-sync-status
   (fn []
     [(.. (.resetSyncStatus collec)
          (then #(rf/dispatch [:sync-history]))
          (catch #(rf/dispatch [:error [:kinto-reset-sync-status %]])))]))


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

(def react-bootstrap (getValueByKeys js/window "deps" "react-bootstrap"))
(def bs-grid (getValueByKeys react-bootstrap "Grid"))
(def bs-row  (getValueByKeys react-bootstrap "Row"))
(def bs-col  (getValueByKeys react-bootstrap "Col"))

(defn login-link
  []
  [:a {:on-click #(rf/dispatch [:login])}
     "Login"])

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
   [:div.container-fluid
    (when false [:pre (with-out-str (pprint @app-db))])
    [:div.pull-right [login-link]]
    [:> bs-grid
      [:> bs-row
        [:> bs-col {:xs 6 :md 6}
          [:h1 "POC Club des Expressions"]
          [src-input]
          [:div "Formatted expr: "
            [expr @(rf/subscribe [:latex-src])]]
          [:div "History count: "
            @(rf/subscribe [:history-count])]
          [request-history-count-button]
          [sync-history-button]]
        [:> bs-col {:xs 6 :md 6}
          [:h2 "Instructions et commentaires"]
          [:p "Le lien « Login » amène l’utilisateur à la page de connexion. "
              "Au retour, rien de spécial ne se passe, à part dans l’URL."]
          [:p "Chaque modification du champs « Code Club » provoque :"]
          [:ol
           [:li "la mise à jour de l’expression mathématique "
                "(Formatted expr) ;"]
           [:li "un enregistrement de la valeur du champs dans le navigateur "
                "qui va constituer dans ce POC ce qu’on appelera "
                "« l’historique » ;"]]
          [:p "Le compteur « History count » ne mesure le nombre de lignes "
              "dans l’historique que si on clique sur le bouton « Update… »."]
          [:p "Le bouton « Update history count » permet de mettre à jour le "
              "compteur, en allant lire l’historique dans le navigateur."]
          [:p "Le bouton « Sync history » permet de fusionner l’historique "
              "local (dans le navigateur) et l’historique sur le serveur. "
              "C’est une fusion dans les deux sens."]]
    ]]])

;; -- Entry Point -------------------------------------------------------------

(defn ^:export run
  []
  (rf/dispatch-sync [:initialize])     ;; puts a value into application state
  (reagent/render [ui]              ;; mount the application's ui into '<div id="app" />'
                  (js/document.getElementById "app")))

