(ns multi-client-ws.core
  (:require [reagent.core :as reagent :refer [atom]]
            [multi-client-ws.websockets :as ws]))

(defonce messages (atom []))
(defonce username (atom nil))
(defonce current-channel (atom nil))
(defn channel-list []
  ;;render list of all channels
  ;; if current channel is set, render message list for that channel
  ;; when channel is clicked, set current-channel
  )

(defn message-list []
  [:ul
   (for [[i message] (map-indexed vector @messages)]
     ^{:key i}
     [:li (message "username") " says: " (message "message")])])

(defn input-keydown [val key-event]
  (let [key-code (.-keyCode key-event)]
    (when (= key-code 13)
      (ws/send-transit-msg! {:message @val :username @username})
      (reset! val nil))))

(defn message-input []
  (let [value (atom nil)]
    (fn []
      [:input.form-control
       {:type :text
        :placeholder "type in a message and press enter"
        :value @value
        :on-change (fn [event] (reset! value (aget event "target" "value")))
        :on-key-down (partial input-keydown value)
        }])))

(defn name-input []
  (let [value (atom nil)]
    (fn []
      [:input.form-control
       {:type :text
        :value @value
        :on-change (fn [e] (reset! value (aget e "target" "value")))
        :on-key-down (fn [e] (let [kc (.-keyCode e)]
                               (println "GOT MESSAGE: " kc)
                               (when (= kc 13)
                                 (println "hit it!")
                                 (reset! username @value)
                                 (println "username is: " @username))
                               ))}])))


(defn name-field []
  ;; if there is a name set, just render it?
  ;; otherwise show a form input?
  (fn [] 
    (when (nil?  @username)
      [name-input])))

(defn home-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:h2 "Welcome to chat, " @username]
     [name-field]]]
   [:div.row
    [:div.col-sm-6
     [message-list]]]
   [:div.row
    [:div.col-sm-6
     [message-input]]]])

(defn update-messages! [message]
  (println "updating message: " message)
  (swap! messages #(conj % message)))

(defn mount-components []
  (reagent/render-component [#'home-page] (.getElementById js/document "app")))

(defn init! []
  (ws/make-websocket! (str "ws://" (.-host js/location) "/ws") update-messages!)
  (mount-components))
